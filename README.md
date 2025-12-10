# ☞ quiz

This is a learning repository for implementing java task.

> [!IMPORTANT]
> To run this code, you need to have JRE 23+ installed on your PC.
>

## 🎮 Application Modes

The quiz currently features **two convenient startup modes**:

### 1. 🪄 **Topic Generation (AI Mode)**
Simply enter any topic you're interested in, and the artificial intelligence will instantly create a personalized quiz!
> *Example:* Enter `"space exploration"` or `"history of Ancient Rome"` to receive a unique set of questions.

### 2. 📚 **Prepared Topics (Classic Mode)**
Select one of the ready-made, carefully curated topics for a quick start.
> **Available topics include:** `World Countries`, `World Capitals`, `Space`, and `General Knowledge`.

---

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
You can create your own set of questions and run a quiz on it!
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
