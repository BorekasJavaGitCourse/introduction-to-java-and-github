<div dir="rtl">

# מקלט החיות מבוא ל-Java ול-GitHub

ברוכים הבאים! בקורס הזה תבנו אפליקציית קונסולה ב-Java לניהול מקלט לחיות, צעד אחר צעד, ותלמדו תוך כדי עבודה עם Git ו-GitHub כמו בצוות פיתוח אמיתי.

## איך עובדים

כל שלב הוא **ענף (branch)** נפרד ששמו מתחיל ב-`feature/`. בסוף השלב ממזגים אותו ל-`main`.

| שלב | Java | Git |
|---|---|---|
| 0 | התקנות | יצירת ריפו מתבנית, clone |
| 1 | `Main`, Hello Shelter | status, add, commit, push |
| 2 | מחלקת `Pet` | branch, switch, commit על ענף |
| 3 | מתודות, getters/setters | merge ל-main, מחיקת ענף |
| 4 | `Shelter` + `ArrayList` | pull, log, שינויים מרוחקים |
| 5 | תפריט עם `Scanner`, לולאות, `switch` | טעות מכוונת ו-revert |
| 6 | ירושה `Dog`/`Cat` | קונפליקט מיזוג מתוכנן ופתרונו |
| 7 | ליטוש סופי | Pull Request, ניקוי ענפים |

- לכל שלב יש **Issue** באותו מספר (Issue #3 = שלב 3). רשימת המשימות המלאה נמצאת ב-[`TASKS.md`](TASKS.md).
- בסיום שלב כותבים `Closes #N` בהודעת ה-commit או ב-Pull Request, והאישיו ייסגר לבד כשהשינוי נכנס ל-`main`.
- אחרי כל `git push` נכנסים ללשונית **Actions**: V ירוק = הכול טוב, X אדום = יש שגיאת קומפילציה או בדיקה שנכשלה. לחצו על הריצה כדי לקרוא מה קרה.
- הבדיקות מכירות רק את מה שכבר כתבתם: שלב שלא הגעתם אליו מסומן SKIP ואין בו כשל.

## הרצה מקומית

```bash
javac -d out src/*.java tests/*.java
java -cp out Main            # הרצת התוכנית
java -cp out TestRunner      # הרצת הבדיקות
```

<!-- בשלב 7 מחליפים את YOUR NAME HERE בשם שלכם -->

## התקדמות

- [ ] שלב 0: התקנה, הצטרפות, clone
- [ ] שלב 1: Hello Shelter
- [ ] שלב 2: Pet
- [ ] שלב 3: מתודות וכימוס
- [ ] שלב 4: Shelter ו-ArrayList
- [ ] שלב 5: תפריט + revert
- [ ] שלב 6: ירושה + קונפליקט
- [ ] שלב 7: Pull Request וניקוי

</div>
