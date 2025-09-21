# ☞ quiz
This is a learning repository for implementing java task.

> [!IMPORTANT]
> To run this code, you need to have JRE 23+ installed on your PC.
> 
### 🚀 USAGE
```cmd
java -jar ./out/first.jar
```
or
```cmd
java -jar ./out/first.jar /your/path/to/question
```

### FORMAT FOR YOU QUESTIONS 📄
```json
[
  {
    "question": "Question1",
    "options": ["Answer❌", "Answer✅", "Answer❌", "Answer❌"],
    "answer": 1 /* this is the response index, no more than one */
  },
  {
    "question": "Question2",
    "options": ["Answer✅", "Answer❌" /* ...(not more than 12)] */ ]
    "answer": 0
  }
  /* ...(as much as you like) */
]
```

