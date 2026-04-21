# 🤖 Job Notifier AI

An automated job search bot powered by **Claude AI** + **Telegram**. It scrapes job listings, filters them by relevance, scores them using AI, and sends the best matches directly to your Telegram.

---

## 📦 Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.3 |
| Scraping | Selenium 4 + WebDriverManager |
| AI Scoring | Claude API (claude-sonnet) |
| Notifications | Telegram Bot API |
| Database | H2 (embedded, file-based) |
| Scheduler | Spring `@Scheduled` |

---

## 🔄 How It Works — Flow Diagram

```
┌─────────────────────────────────────────────────────────┐
│                    SCHEDULER (every 10 min)              │
│                      JobScheduler.java                   │
└───────────────────────┬─────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│                  1. SCRAPING                             │
│              JobScraperService.java                      │
│                                                         │
│  Chrome (Headless) ──► stepstone.de/jobs/java           │
│                         ├── title                       │
│                         ├── company                     │
│                         ├── location                    │
│                         └── link                        │
└───────────────────────┬─────────────────────────────────┘
                        │  List<Job>
                        ▼
┌─────────────────────────────────────────────────────────┐
│                2. KEYWORD FILTER                         │
│              JobFilterService.java                       │
│                                                         │
│  ✅ Keep:  java, spring, fullstack, backend, developer  │
│  ❌ Skip:  buchhalter, fahrer, pfleger, lehrer ...      │
└───────────────────────┬─────────────────────────────────┘
                        │  Relevant Jobs only
                        ▼
┌─────────────────────────────────────────────────────────┐
│              3. DUPLICATE CHECK                          │
│               JobRepository.java (H2)                   │
│                                                         │
│  uniqueKey = normalize(title + company)                 │
│  Already in DB? ──► SKIP                                │
└───────────────────────┬─────────────────────────────────┘
                        │  New Jobs only
                        ▼
┌─────────────────────────────────────────────────────────┐
│               4. AI SCORING                              │
│            AIJobScoringService.java                      │
│                                                         │
│  Sends job details to Claude API                        │
│  Claude returns score (1–10) + reason                   │
│                                                         │
│  9-10 ► Perfect match (Java, Spring Boot, Full-Stack)  │
│  6-8  ► Good match (Backend, Software, IT)              │
│  3-5  ► Acceptable (general IT)                         │
│  1-2  ► Not relevant                                    │
└───────────────────────┬─────────────────────────────────┘
                        │
              ┌─────────┴──────────┐
           Score >= 6           Score < 6
              │                    │
              ▼                    ▼
┌─────────────────────┐     ┌──────────────┐
│  5. SEND + SAVE     │     │     SKIP     │
│  TelegramService    │     └──────────────┘
│  JobRepository      │
│                     │
│  🚀 Neue Stelle!    │
│  ⭐⭐⭐ (8/10)       │
│  📌 Title           │
│  🏢 Company         │
│  📍 Location        │
│  🔗 Link            │
└─────────────────────┘
```

---

## 💬 Telegram Commands

The bot also listens for incoming messages in real time:

| Command | Response |
|---|---|
| `/start` | Welcome message + available commands |
| `/status` | Current bot status |
| `/help` | Usage instructions |
| Free text (e.g. `java berlin`) | AI interprets and replies in German |

---

## 🗂 Project Structure

```
job-notifier/
├── src/main/java/com/samer/jobs/
│   ├── JobNotifierApplication.java     ← Entry point
│   ├── scheduler/
│   │   └── JobScheduler.java           ← Orchestrates the full flow
│   ├── scraper/
│   │   └── JobScraperService.java      ← Selenium scraper
│   ├── filter/
│   │   └── JobFilterService.java       ← Keyword-based filter
│   ├── ai/
│   │   └── AIJobScoringService.java    ← Claude API integration
│   ├── telegram/
│   │   ├── TelegramService.java        ← Bot + message sender
│   │   └── TelegramCommandHandler.java ← Command router
│   └── model/
│       ├── Job.java                    ← JPA Entity
│       └── JobRepository.java          ← DB access
└── src/main/resources/
    └── application.properties          ← Config (tokens, keys)
```

---

## ⚙️ Configuration

Edit `src/main/resources/application.properties`:

```properties
# Telegram
telegram.bot.token=YOUR_BOT_TOKEN_HERE
telegram.bot.username=YOUR_BOT_USERNAME_HERE
telegram.chat.id=YOUR_CHAT_ID_HERE

# Claude API
claude.api.key=YOUR_CLAUDE_API_KEY_HERE

# Scheduler interval (milliseconds) — default: 10 minutes
scheduler.delay=600000
```

> ⚠️ Never commit real tokens to GitHub. Use environment variables or a `.env` file and add `application.properties` to `.gitignore`.

---

## 🚀 Getting Started

### Prerequisites
- Java 21+
- Maven 3.8+
- Google Chrome installed (for Selenium)

### Run

```bash
# Clone the repo
git clone https://github.com/YOUR_USERNAME/job-notifier.git
cd job-notifier

# Add your credentials to application.properties

# Build and run
mvn spring-boot:run
```

### Build JAR

```bash
mvn clean package
java -jar target/job-notifier-0.0.1-SNAPSHOT.jar
```

---

## 🛢 Database

The app uses an embedded **H2** file database — no setup needed. Data persists between restarts in `./jobsdb.mv.db`.

Access the H2 console at: `http://localhost:8080/h2-console`

- JDBC URL: `jdbc:h2:file:./jobsdb`
- Username: `sa`
- Password: *(empty)*

---

## 🔒 .gitignore Recommendation

```
application.properties
*.mv.db
*.trace.db
target/
```

---

## 📄 License

MIT License — free to use and modify.
