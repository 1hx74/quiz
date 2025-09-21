# ☞ quiz

___
This is a learning repository for implementing java task.

> [!IMPORTANT]
> To run this code, you need to have JRE 23+ installed on your PC.
> 
### 🚀 USAGE
###### bash
```bash
java -jar ./out/artifacts/first_jar/first.jar
```
### or
###### bash
```bash
java -jar ./out/artifacts/first_jar/first.jar /your/path/to/question
```

### FORMAT FOR YOU QUESTIONS 📄

###### JSON Example
```jsonc
[
  {
    "question": "Question1",
    "options": ["Answer❌", "Answer✅", "Answer❌", "Answer❌"],
    "answer": 1 /* this is the response index, no more than one */
  },
  {
    "question": "Question2",
    "options": ["Answer✅", "Answer❌" /* ...(not more than 12) */ ],
    "answer": 0
  }
  /* ...(as much as you like) */
]
```
___

> **LICENSE:** All rights reserved
