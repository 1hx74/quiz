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
How to deploy on Railway by Githab

Deploy Telegram Bot on Railway (Java/Maven)
Railway automatically builds Maven projects and runs JAR files, making it ideal for Java Telegram bots. Assumes you have a GitHub repo with pom.xml and bot code using telegrambots library.

Prerequisites
GitHub account with your bot repository pushed (public or private with Railway access).

Bot token from @BotFather.

Railway account (sign up at railway.app with GitHub login).

Step 1: Link GitHub Repo
Log in to railway.app.

Click "New Project" → "Deploy from GitHub repo".

Search and select your bot repository.

Click "Deploy Now" (or "Add Variables" first if preferred).

Railway detects pom.xml and runs mvn clean package automatically.

Step 2: Add Environment Variables
In your service settings → "Variables" tab.

Add: BOT_TOKEN = your_bot_token_here.

Optional: JAVA_OPTS = -Xmx512m for memory limits.

Click "Add" → Railway redeploys automatically.

Your bot code should read token via System.getenv("BOT_TOKEN").

Step 3: Monitor Deployment
Watch "Deploys" tab for build logs (mvn output, JAR creation).

"Logs" tab shows runtime: bot registers with Telegram API.

Status turns green when ready (1-3 minutes).

Test bot in Telegram with /start.

Updating Code (Auto-Deploy)
Edit code → git commit -m "Update" && git push.
Railway auto-triggers rebuild → new JAR → bot restarts with changes.
____

> **LICENSE:** All rights reserved
