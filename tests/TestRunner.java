import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Simple course test runner (no JUnit, no libraries).
 *
 * It looks at the classes you have written so far and checks only what
 * you should have finished by now:
 *   PASS = works as expected
 *   FAIL = exists but is wrong (the build turns red)
 *   SKIP = you have not reached this step yet (that is fine!)
 *   WARN = a reminder, does not fail the build
 *
 * Run locally:  javac -d out src/*.java tests/*.java && java -cp out TestRunner
 */
public class TestRunner {

    private static final PrintStream REAL_OUT = System.out;
    private static final List<String[]> rows = new ArrayList<>();
    private static int failures = 0;

    // ------------------------------------------------------------ reporting
    private static void add(int step, String what, String result, String note) {
        rows.add(new String[] {String.valueOf(step), what, result, note});
    }
    private static void pass(int step, String what) { add(step, what, "PASS", ""); }
    private static void skip(int step, String what, String why) { add(step, what, "SKIP", why); }
    private static void warn(int step, String what, String why) { add(step, what, "WARN", why); }
    private static void fail(int step, String what, String why) { failures++; add(step, what, "FAIL", why); }
    private static void expect(int step, String what, boolean ok, String whyNot) {
        if (ok) pass(step, what); else fail(step, what, whyNot);
    }

    /** A check that may throw: any exception becomes a FAIL with a readable message. */
    private interface Check { void run() throws Throwable; }
    private static void run(int step, String what, Check check) {
        try {
            check.run();
        } catch (InvocationTargetException e) {
            fail(step, what, "threw " + e.getCause());
        } catch (NoSuchMethodException e) {
            fail(step, what, "missing method/constructor: " + e.getMessage());
        } catch (Throwable e) {
            fail(step, what, "problem: " + e);
        }
    }

    // ------------------------------------------------------------ reflection helpers
    private static Class<?> findClass(String name) {
        try { return Class.forName(name); } catch (ClassNotFoundException e) { return null; }
    }
    private static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    private static Object newPet(Class<?> c, String name, int age) throws Exception {
        return c.getConstructor(String.class, int.class).newInstance(name, age);
    }
    private static Object field(Object o, String name) throws Exception {
        Field f = findField(o.getClass(), name);
        f.setAccessible(true);
        return f.get(o);
    }
    private static Field findField(Class<?> c, String name) throws NoSuchFieldException {
        for (Class<?> k = c; k != null; k = k.getSuperclass()) {
            try { return k.getDeclaredField(name); } catch (NoSuchFieldException ignored) { }
        }
        throw new NoSuchFieldException(name);
    }
    private static Object call(Object o, String name, Class<?>[] types, Object... args) throws Exception {
        return o.getClass().getMethod(name, types).invoke(o, args);
    }
    private static Object call0(Object o, String name) throws Exception {
        return call(o, name, new Class<?>[0]);
    }
    private static String readSrc(String cls) {
        try { return Files.readString(Path.of("src", cls + ".java")); } catch (IOException e) { return ""; }
    }

    // ------------------------------------------------------------ output capture & running Main
    /** Output stream that keeps at most `cap` bytes (protects us from endless printing loops). */
    private static class Capped extends OutputStream {
        private final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        private final int cap;
        Capped(int cap) { this.cap = cap; }
        @Override public synchronized void write(int b) { if (buf.size() < cap) buf.write(b); }
        @Override public synchronized String toString() { return buf.toString(StandardCharsets.UTF_8); }
    }

    private static class RunResult {
        String out = "";
        boolean timedOut;
        Throwable error;
    }

    /** Runs Main.main with the given keyboard input; gives up after 5 seconds. */
    private static RunResult runMain(String input) {
        RunResult res = new RunResult();
        Class<?> main = findClass("Main");
        Capped cap = new Capped(200_000);
        PrintStream ps = new PrintStream(cap, true, StandardCharsets.UTF_8);
        InputStream oldIn = System.in;
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        System.setOut(ps);
        System.setErr(ps);
        Thread t = new Thread(() -> {
            try {
                main.getMethod("main", String[].class).invoke(null, (Object) new String[0]);
            } catch (InvocationTargetException e) {
                res.error = e.getCause();
            } catch (Throwable e) {
                res.error = e;
            }
        });
        t.setDaemon(true);
        t.start();
        try { t.join(5000); } catch (InterruptedException ignored) { }
        res.timedOut = t.isAlive();
        res.out = cap.toString();
        if (!res.timedOut) {            // a runaway thread keeps the redirected streams
            System.setIn(oldIn);
            System.setOut(REAL_OUT);
            System.setErr(REAL_OUT);
        }
        return res;
    }

    /** Checks that a Main run finished normally; returns null if OK, else a message. */
    private static String problemWith(RunResult r) {
        if (r.timedOut) return "program did not finish within 5 seconds (infinite loop?)";
        if (r.error instanceof java.util.NoSuchElementException)
            return "the program kept asking for input after the test finished typing - does option 0 really end the loop?";
        if (r.error != null) return "program crashed: " + r.error;
        return null;
    }

    private static String capture(Check c) throws Throwable {
        Capped cap = new Capped(100_000);
        PrintStream old = System.out;
        System.setOut(new PrintStream(cap, true, StandardCharsets.UTF_8));
        try { c.run(); } finally { System.setOut(old); }
        return cap.toString();
    }

    // ------------------------------------------------------------ the checks
    private static void step1() {
        Class<?> main = findClass("Main");
        if (main == null) { skip(1, "Main prints Hello Shelter", "Main.java not created yet"); return; }
        run(1, "Main prints Hello Shelter", () -> {
            RunResult r = runMain("0\n");
            String p = problemWith(r);
            if (p != null) { fail(1, "Main prints Hello Shelter", p); return; }
            expect(1, "Main prints Hello Shelter", r.out.toLowerCase().contains("hello shelter"),
                   "output must contain \"Hello Shelter\"");
        });
    }

    private static void step2() {
        Class<?> pet = findClass("Pet");
        if (pet == null) { skip(2, "Pet class", "Pet.java not created yet"); return; }
        run(2, "Pet(name, age) sets fields, hunger starts at 5", () -> {
            Object p = newPet(pet, "Rex", 3);
            boolean ok = "Rex".equals(field(p, "name")) && Integer.valueOf(3).equals(field(p, "age"))
                    && Integer.valueOf(5).equals(field(p, "hunger"));
            expect(2, "Pet(name, age) sets fields, hunger starts at 5", ok,
                   "expected name=Rex, age=3, hunger=5");
        });
        run(2, "Pet(name, age, hunger) sets hunger", () -> {
            Object p = pet.getConstructor(String.class, int.class, int.class).newInstance("Mia", 5, 8);
            expect(2, "Pet(name, age, hunger) sets hunger", Integer.valueOf(8).equals(field(p, "hunger")),
                   "expected hunger=8");
        });
    }

    private static void step3() {
        Class<?> pet = findClass("Pet");
        if (pet == null || !hasMethod(pet, "getName")) {
            skip(3, "Pet encapsulation, getters/setters, methods", "step 3 not reached yet");
            return;
        }
        run(3, "fields are private", () -> {
            boolean ok = true;
            for (String f : new String[] {"name", "age", "hunger"}) {
                ok &= Modifier.isPrivate(findField(pet, f).getModifiers());
            }
            expect(3, "fields are private", ok, "name, age and hunger must be private");
        });
        run(3, "getters work", () -> {
            Object p = newPet(pet, "Rex", 3);
            boolean ok = "Rex".equals(call0(p, "getName")) && Integer.valueOf(3).equals(call0(p, "getAge"))
                    && Integer.valueOf(5).equals(call0(p, "getHunger"));
            expect(3, "getters work", ok, "getName/getAge/getHunger should return the field values");
        });
        run(3, "setAge ignores negative numbers", () -> {
            Object p = newPet(pet, "Rex", 3);
            call(p, "setAge", new Class<?>[] {int.class}, -5);
            call(p, "setAge", new Class<?>[] {int.class}, 4);
            boolean ok4 = Integer.valueOf(4).equals(call0(p, "getAge"));
            call(p, "setAge", new Class<?>[] {int.class}, -1);
            expect(3, "setAge ignores negative numbers", ok4 && Integer.valueOf(4).equals(call0(p, "getAge")),
                   "setAge(-1) must not change the age; setAge(4) must");
        });
        run(3, "setHunger keeps 0..10", () -> {
            Object p = newPet(pet, "Rex", 3);
            call(p, "setHunger", new Class<?>[] {int.class}, 99);
            boolean hi = Integer.valueOf(10).equals(call0(p, "getHunger"));
            call(p, "setHunger", new Class<?>[] {int.class}, -4);
            boolean lo = Integer.valueOf(0).equals(call0(p, "getHunger"));
            expect(3, "setHunger keeps 0..10", hi && lo, "setHunger(99) -> 10 and setHunger(-4) -> 0");
        });
        run(3, "setName ignores empty names", () -> {
            Object p = newPet(pet, "Rex", 3);
            call(p, "setName", new Class<?>[] {String.class}, "  ");
            call(p, "setName", new Class<?>[] {String.class}, (Object) null);
            boolean same = "Rex".equals(call0(p, "getName"));
            call(p, "setName", new Class<?>[] {String.class}, "Max");
            expect(3, "setName ignores empty names", same && "Max".equals(call0(p, "getName")),
                   "blank/null names are ignored, real names are set");
        });
        run(3, "feed() lowers hunger by 3 (not below 0)", () -> {
            Object p = newPet(pet, "Rex", 3);
            call0(p, "feed");
            boolean a = Integer.valueOf(2).equals(call0(p, "getHunger"));
            call0(p, "feed");
            boolean b = Integer.valueOf(0).equals(call0(p, "getHunger"));
            expect(3, "feed() lowers hunger by 3 (not below 0)", a && b, "5 -> 2 -> 0");
        });
        run(3, "birthday() adds one year", () -> {
            Object p = newPet(pet, "Rex", 3);
            call0(p, "birthday");
            expect(3, "birthday() adds one year", Integer.valueOf(4).equals(call0(p, "getAge")), "3 -> 4");
        });
        run(3, "isHungry() is true from hunger 7", () -> {
            Object p = newPet(pet, "Rex", 3);
            call(p, "setHunger", new Class<?>[] {int.class}, 7);
            boolean a = Boolean.TRUE.equals(call0(p, "isHungry"));
            call(p, "setHunger", new Class<?>[] {int.class}, 6);
            boolean b = Boolean.FALSE.equals(call0(p, "isHungry"));
            expect(3, "isHungry() is true from hunger 7", a && b, "7 -> true, 6 -> false");
        });
        run(3, "toString format", () -> {
            Object p = newPet(pet, "Rex", 3);
            expect(3, "toString format", "Rex (age 3, hunger 5)".equals(p.toString()),
                   "expected \"Rex (age 3, hunger 5)\" but got \"" + p + "\"");
        });
    }

    private static void step4() {
        Class<?> shelter = findClass("Shelter");
        Class<?> pet = findClass("Pet");
        if (shelter == null || pet == null) { skip(4, "Shelter with ArrayList", "Shelter.java not created yet"); return; }
        run(4, "addPet and getCount", () -> {
            Object s = shelter.getConstructor().newInstance();
            call(s, "addPet", new Class<?>[] {pet}, newPet(pet, "Rex", 3));
            call(s, "addPet", new Class<?>[] {pet}, newPet(pet, "Mia", 5));
            expect(4, "addPet and getCount", Integer.valueOf(2).equals(call0(s, "getCount")), "two pets added");
        });
        run(4, "findByName ignores case, null if missing", () -> {
            Object s = shelter.getConstructor().newInstance();
            Object rex = newPet(pet, "Rex", 3);
            call(s, "addPet", new Class<?>[] {pet}, rex);
            Object found = call(s, "findByName", new Class<?>[] {String.class}, "rEX");
            Object none = call(s, "findByName", new Class<?>[] {String.class}, "Ghost");
            expect(4, "findByName ignores case, null if missing", found == rex && none == null,
                   "findByName(\"rEX\") should return Rex; unknown name -> null");
        });
        run(4, "removePet", () -> {
            Object s = shelter.getConstructor().newInstance();
            call(s, "addPet", new Class<?>[] {pet}, newPet(pet, "Rex", 3));
            Object a = call(s, "removePet", new Class<?>[] {String.class}, "Rex");
            Object b = call(s, "removePet", new Class<?>[] {String.class}, "Rex");
            expect(4, "removePet", Boolean.TRUE.equals(a) && Boolean.FALSE.equals(b)
                    && Integer.valueOf(0).equals(call0(s, "getCount")), "true the first time, false the second");
        });
        run(4, "feedAll feeds everyone", () -> {
            Object s = shelter.getConstructor().newInstance();
            Object rex = newPet(pet, "Rex", 3);
            call(s, "addPet", new Class<?>[] {pet}, rex);
            call0(s, "feedAll");
            expect(4, "feedAll feeds everyone", Integer.valueOf(2).equals(field(rex, "hunger")), "hunger 5 -> 2");
        });
        run(4, "printAll output", () -> {
            Object s = shelter.getConstructor().newInstance();
            String empty = capture(() -> call0(s, "printAll"));
            call(s, "addPet", new Class<?>[] {pet}, newPet(pet, "Rex", 3));
            String one = capture(() -> call0(s, "printAll"));
            expect(4, "printAll output", empty.contains("No pets yet.") && one.contains("Rex") && one.contains("1"),
                   "empty -> \"No pets yet.\"; otherwise numbered lines with the pet");
        });
    }

    private static void step5() {
        String src = readSrc("Main");
        if (!src.contains("Scanner") || findClass("Shelter") == null) {
            skip(5, "Menu loop", "step 5 not reached yet");
            return;
        }
        run(5, "Menu: add + list, then exit with 0", () -> {
            RunResult r = runMain("1\nRex\n3\n2\n0\n");
            String p = problemWith(r);
            if (p != null) { fail(5, "Menu: add + list, then exit with 0", p); return; }
            expect(5, "Menu: add + list, then exit with 0", r.out.contains("Rex"),
                   "after adding Rex (option 1) and listing (option 2), the output must contain Rex");
        });
        run(5, "Menu: invalid choice", () -> {
            RunResult r = runMain("9\n0\n");
            String p = problemWith(r);
            if (p != null) { fail(5, "Menu: invalid choice", p); return; }
            expect(5, "Menu: invalid choice", r.out.contains("Invalid choice."), "print \"Invalid choice.\"");
        });
        run(5, "Menu: feeding an unknown pet", () -> {
            RunResult r = runMain("3\nGhost\n0\n");
            String p = problemWith(r);
            if (p != null) { fail(5, "Menu: feeding an unknown pet", p); return; }
            expect(5, "Menu: feeding an unknown pet", r.out.contains("Not found."), "print \"Not found.\"");
        });
    }

    private static void step6() {
        Class<?> pet = findClass("Pet");
        Class<?> dog = findClass("Dog");
        Class<?> cat = findClass("Cat");
        if (pet == null || (dog == null && cat == null)) { skip(6, "Inheritance Dog/Cat", "step 6 not reached yet"); return; }
        String[][] kinds = {{"Dog", "Woof!"}, {"Cat", "Meow!"}};
        for (String[] k : kinds) {
            Class<?> c = findClass(k[0]);
            if (c == null) { skip(6, k[0] + " class", k[0] + ".java not created yet"); continue; }
            run(6, k[0] + " extends Pet, sound and toString", () -> {
                boolean ext = c.getSuperclass() == pet;
                Object o = newPet(c, "Rex", 3);
                boolean sound = k[1].equals(call0(o, "makeSound"));
                boolean str = o.toString().equals(k[0] + " Rex (age 3, hunger 5)");
                expect(6, k[0] + " extends Pet, sound and toString", ext && sound && str,
                       "extends Pet=" + ext + ", makeSound()=" + k[1] + ":" + sound + ", toString: \"" + o + "\"");
            });
        }
        run(6, "Pet.makeSound() default", () -> {
            expect(6, "Pet.makeSound() default", "...".equals(call0(newPet(pet, "X", 1), "makeSound")),
                   "default sound is \"...\"");
        });
        String src = readSrc("Main");
        if (src.contains("new Dog") && src.contains("new Cat")) {
            run(6, "Menu: add dog (5) and cat (6)", () -> {
                RunResult r = runMain("5\nRex\n2\n6\nTom\n3\n2\n0\n");
                String p = problemWith(r);
                if (p != null) { fail(6, "Menu: add dog (5) and cat (6)", p); return; }
                expect(6, "Menu: add dog (5) and cat (6)", r.out.contains("Woof!") && r.out.contains("Meow!"),
                       "the list should show Woof! and Meow!");
            });
        } else {
            skip(6, "Menu: add dog (5) and cat (6)", "waiting until both branches are merged");
        }
        for (String f : new File("src").list((d, n) -> n.endsWith(".java"))) {
            String text = readSrc(f.replace(".java", ""));
            if (text.contains("<<<<<<<") || text.contains(">>>>>>>")) {
                fail(6, "no conflict markers in " + f, "remove <<<<<<<, =======, >>>>>>> lines");
            }
        }
    }

    private static void step7() {
        String readme;
        try { readme = Files.readString(Path.of("README.md")); } catch (IOException e) { readme = ""; }
        boolean placeholder = readme.contains("YOUR NAME HERE");
        String head = System.getenv("GITHUB_HEAD_REF");      // set on pull requests
        String ref = System.getenv("GITHUB_REF_NAME");       // branch name on pushes
        boolean finalBranch = "feature/final-polish".equals(head) || "feature/final-polish".equals(ref);
        if (!placeholder) pass(7, "README: your name is filled in");
        else if (finalBranch) fail(7, "README: your name is filled in", "replace YOUR NAME HERE with your name");
        else warn(7, "README: your name is filled in", "reminder for step 7: replace YOUR NAME HERE");
    }

    // ------------------------------------------------------------ main
    public static void main(String[] args) throws Exception {
        step1(); step2(); step3(); step4(); step5(); step6(); step7();

        StringBuilder md = new StringBuilder();
        md.append("## Test results\n\n| Step | Check | Result | Note |\n|---|---|---|---|\n");
        for (String[] r : rows) {
            String icon = switch (r[2]) {
                case "PASS" -> "PASS";
                case "FAIL" -> "FAIL";
                case "WARN" -> "WARN";
                default -> "SKIP";
            };
            md.append("| ").append(r[0]).append(" | ").append(r[1]).append(" | ").append(icon)
              .append(" | ").append(r[3].replace("|", "/")).append(" |\n");
        }
        md.append("\n").append(failures == 0 ? "No failures." : failures + " failure(s) - read the Note column.").append("\n");

        REAL_OUT.println(md);
        String summary = System.getenv("GITHUB_STEP_SUMMARY");
        if (summary != null && !summary.isEmpty()) {
            Files.writeString(Path.of(summary), md.toString(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        }
        REAL_OUT.flush();
        Runtime.getRuntime().halt(failures == 0 ? 0 : 1);   // halt: a runaway thread cannot keep us alive
    }
}
