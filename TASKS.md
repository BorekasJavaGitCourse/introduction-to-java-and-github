<div dir="rtl">

# רשימת משימות

מסמנים V (`[x]`) בכל משימה שסיימתם. אפשר לערוך את הקובץ ולעשות עליו commit.

## שלב 0: הכנות

- [ ] JDK 21 (או חדש יותר) מותקן: `java -version`
- [ ] Git מותקן ומוגדר: `git config --global user.name` ו-`user.email`
- [ ] חשבון GitHub, והצטרפתי לארגון דרך הזמנת המייל
- [ ] יצרתי מהתבנית ריפו פרטי משלי בארגון (Use this template)
- [ ] עשיתי `git clone` ופתחתי את הפרויקט ב-IDE

---

## שלב 1: Hello Shelter

**ענף:** `main` (בשלב הזה עובדים ישר על main; מהשלב הבא ענף לכל משימה)  
**מטרה:** התוכנית הראשונה ו-commit ראשון

### Java
- [ ] ליצור `src/Main.java` עם מחלקה `Main` ומתודת `main`
- [ ] להדפיס `Hello Shelter` עם `System.out.println`
- [ ] להגדיר משתנים: `String shelterName`, `int capacity` ולהדפיס אותם
- [ ] להריץ ב-IDE וגם בטרמינל

### Git
- [ ] `git status` לפני ואחרי כל שינוי
- [ ] `git add src/Main.java`
- [ ] `git commit -m "Add Main with Hello Shelter"`
- [ ] `git push`

### קריטריוני הצלחה (מה ה-CI בודק)
- `Main` קיימת ומדפיסה שורה שמכילה `Hello Shelter`
- ה-CI ירוק

בסיום: `Closes #1`

---

## שלב 2: מחלקת Pet

**ענף:** `feature/pet-class`  
**מטרה:** להגדיר מחלקה עם שדות ובנאים

### Java
- [ ] ליצור `src/Pet.java` עם השדות `name` (String), `age` (int), `hunger` (int, 0 = שבע ... 10 = רעב מאוד)
- [ ] בנאי `Pet(String name, int age)` שמגדיר `hunger = 5`
- [ ] בנאי `Pet(String name, int age, int hunger)`
- [ ] ב-`Main`: ליצור שני אובייקטים ולהדפיס את השדות שלהם

### Git
- [ ] `git switch -c feature/pet-class`
- [ ] commit אחד או יותר על הענף
- [ ] `git push -u origin feature/pet-class`
- [ ] לראות את הענף ב-GitHub (תפריט Branches)

### קריטריוני הצלחה (מה ה-CI בודק)
- לבנאים יש את הפרמטרים המדויקים
- ערכי ההתחלה נכונים (`hunger` = 5 בבנאי הקצר)

בסיום: `Closes #2`

---

## שלב 3: מתודות, getters/setters ומיזוג

**ענף:** `feature/pet-methods`  
**מטרה:** כימוס, התנהגות של אובייקט, ומיזוג ל-main

### Java
- [ ] להפוך את השדות ל-`private`
- [ ] getters: `getName`, `getAge`, `getHunger`
- [ ] setters: `setName` (מתעלם מ-null/ריק), `setAge` (מתעלם ממספר שלילי), `setHunger` (מגביל ל-0..10)
- [ ] `feed()` מוריד 3 מה-hunger (לא מתחת ל-0)
- [ ] `birthday()` מוסיף שנה
- [ ] `isHungry()` מחזירה true כש-hunger >= 7 (השתמשו ב-if/else)
- [ ] `toString()` מחזיר בדיוק: `Rex (age 3, hunger 5)`

### Git
- [ ] `git switch main` ואז `git merge feature/pet-methods`
- [ ] `git push`
- [ ] `git branch -d feature/pet-methods`

### קריטריוני הצלחה (מה ה-CI בודק)
- השדות private
- כל המתודות מתנהגות כמתואר
- הפורמט של `toString()` מדויק

בסיום: `Closes #3`

---

## שלב 4: Shelter ו-ArrayList

**ענף:** `feature/shelter`  
**מטרה:** מחלקה שמנהלת רשימת חיות

### Java
- [ ] `Shelter` עם שדה private מסוג `ArrayList<Pet>`
- [ ] `addPet(Pet)`, `int getCount()`
- [ ] `Pet findByName(String)` (לא רגיש לאותיות גדולות/קטנות, מחזיר `null` אם אין)
- [ ] `boolean removePet(String name)`
- [ ] `void feedAll()`
- [ ] `void printAll()`: מדפיסה `No pets yet.` כשהרשימה ריקה, אחרת שורה לכל חיה עם מספור מ-1

### Git
- [ ] לערוך את `README.md` ישירות באתר GitHub ולעשות commit שם (מדמה חבר צוות)
- [ ] אצלכם: `git pull`
- [ ] `git log --oneline --graph`
- [ ] למזג ל-`main`

### קריטריוני הצלחה (מה ה-CI בודק)
- כל המתודות קיימות ופועלות
- `printAll` מדפיסה כמתואר

בסיום: `Closes #4`

---

## שלב 5: תפריט, לולאות ו-switch, וטעות מכוונת

**ענף:** `feature/menu`  
**מטרה:** תוכנית אינטראקטיבית, ואיך מבטלים טעות עם revert

### Java
- [ ] `Scanner` שקורא **הכול** עם `nextLine()` והמרה עם `Integer.parseInt`
- [ ] הדפסת כותרת שמכילה `Hello Shelter`
- [ ] לולאת `while` עם משתנה `running`
- [ ] `switch` על הבחירה
- [ ] תפריט: `1` הוספת חיה (שואל שם בשורה אחת, גיל בשורה הבאה), `2` הצגה, `3` האכלה (שם), `4` יום הולדת (שם), `0` יציאה
- [ ] בחירה לא חוקית מדפיסה `Invalid choice.`; חיה שלא נמצאה מדפיסה `Not found.`

### Git
- [ ] commit לתפריט התקין ו-push (CI ירוק)
- [ ] **הטעות:** ב-`case 0` לשנות `running = false;` ל-`running = true;`, commit + push, ולראות CI אדום
- [ ] `git log --oneline` ולמצוא את ה-hash של ה-commit השגוי
- [ ] `git revert <hash>` ואז `git push`
- [ ] לוודא ש-CI ירוק שוב ושההיסטוריה מכילה גם את הטעות וגם את ה-revert
- [ ] למזג ל-`main`

### קריטריוני הצלחה (מה ה-CI בודק)
- אפשרות 0 מסיימת את התוכנית
- `Invalid choice.` ו-`Not found.` מוצגים במקום הנכון

בסיום: `Closes #5`

---

## שלב 6: ירושה וקונפליקט מיזוג

**ענף:** `feature/dog, feature/cat`  
**מטרה:** מחלקות יורשות, ופתרון קונפליקט

### Java
- [ ] ב-`Pet`: `public String makeSound()` שמחזירה `"..."`
- [ ] `Dog extends Pet`: בנאי `(String name, int age)` עם `super`, `makeSound()` מחזיר `"Woof!"`, `toString()` מוסיף `Dog ` בתחילה
- [ ] `Cat extends Pet`: כנ"ל עם `"Meow!"` ו-`Cat `
- [ ] `printAll` ב-`Shelter` מציגה גם את הצליל של כל חיה

### Git
- [ ] ליצור `feature/inheritance` עם `Pet.makeSound`, `Dog`, `Cat` ושינוי ב-`printAll`, ולמזג אותו ל-`main` (בסיס משותף)
- [ ] ליצור את `feature/dog` ואת `feature/cat` **שניהם מאותו commit ב-main**
- [ ] בכל ענף: להוסיף ל-`Main` שורת תפריט `5. Add dog` / `5. Add cat` ו-`case 5:` באותו מקום
- [ ] למזג את `feature/dog` ל-main (עובר חלק)
- [ ] למזג את `feature/cat`: **קונפליקט**! לפתור: לשמור את שתי האפשרויות, לשנות את החתול ל-`6`
- [ ] `git add Main.java` ואז `git commit`
- [ ] `git log --oneline --graph` כדי לראות את ה-merge commit

### קריטריוני הצלחה (מה ה-CI בודק)
- `Dog` ו-`Cat` יורשות מ-`Pet` עם הצלילים וה-`toString` הנכונים
- בתפריט: 5 מוסיף כלב ו-6 מוסיף חתול
- אין סימני קונפליקט (`<<<<<<<`) בקוד

> טיפ: אם התבלבלתם באמצע, `git merge --abort` מחזיר הכול למצב שלפני המיזוג.

בסיום: `Closes #6`

---

## שלב 7: ליטוש סופי ו-Pull Request

**ענף:** `feature/final-polish`  
**מטרה:** סיום מסודר כמו בצוות אמיתי

### Java
- [ ] לנקות קוד: שמות ברורים, הערות קצרות, בלי קוד מיותר
- [ ] להחליף ב-`README.md` את `YOUR NAME HERE` בשם שלכם
- [ ] לוודא שהכול מתקמפל ועובר את הבדיקות

### Git
- [ ] `git switch -c feature/final-polish` ולעשות commit
- [ ] `git push -u origin feature/final-polish` ולפתוח **Pull Request** ל-`main` עם `Closes #7`
- [ ] לחכות ל-CI ירוק ולמזג את ה-PR
- [ ] `git switch main` ואז `git pull`
- [ ] למחוק ענפים ממוזגים: `git branch -d ...`, `git push origin --delete ...`, `git fetch --prune`
- [ ] `git branch -a` מציג רק `main`

### קריטריוני הצלחה (מה ה-CI בודק)
- ב-`README.md` אין `YOUR NAME HERE`
- ה-CI ירוק על ה-PR

בסיום: `Closes #7`

---

## צ'קליסט סיום

- [ ] כל 7 האישיוז סגורים
- [ ] ב-`main` יש את כל הקוד, ו-CI ירוק
- [ ] בהיסטוריה אפשר לראות: commit עם הטעות, `Revert ...`, ו-merge commit של הקונפליקט
- [ ] נשאר רק הענף `main`

</div>
