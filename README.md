# 🏠 Apartments Scanner — Telegram Bot

**Telegram bot that scans multiple Berlin housing providers and notifies users about new rental offers.**  
Implements a modular provider-based scraper (Buwog, Vonovia, WBM, HOWOGE, Degewo, ...) and sends notifications to Telegram chats. Designed as a Spring Boot application — easy to run, extend and test.

---

## ✨ Key features

- Aggregates rental listings from multiple providers (Buwog, Vonovia, WBM, HOWOGE, Degewo).
- Sends instant Telegram notifications with: address, price, size, rooms and a direct link.
- Per-user filters: price range, room range (configurable via Telegram commands).
- Providers are pluggable and can be enabled/disabled via configuration.
- No secrets stored in the repository — use environment variables or CI secrets.

---

## 🧭 Project layout (high level)

```
com.example.ApartmentsSearchWithTelegramReporting
├─ bot/ # Telegram bot entrypoint (MyTelegramBot)
├─ model/ # Flat, UserWithFilter, enums
├─ provider/ # Provider implementations (Buwog, Vonovia, WBM, Howoge, Degewo, ...)
├─ repository/ # File-backed repositories (FlatRepositoryImpl, UserWithFilterRepositoryImpl)
├─ service/ # Core services (RestService, UrlMonitorService, FilterService, NotificationService, FlatServiceImpl, CommandService)
├─ utils/ # small utilities (FileUtils)
└─ ApartmentsSearchWithTelegramReportingApplication.java
```
---

## 🔧 Tech stack

- Java 17+
- Spring Boot
- Jsoup (HTML parsing)
- Jackson (JSON handling)
- Telegram Bots API (telegrambots)
- Lombok
- JUnit 5 + Mockito for tests
- Log4j2 for logging

---

## ⚙️ Configure

Create `src/main/resources/application.properties` **locally**. Example provided below is `application-example.properties`.

## 🚀 Run locally
Build with Maven and run
```bash
./mvnw clean package
./mvnw spring-boot:run
```

## 🧪 Tests
Run all unit tests:

```bash
./mvnw test
```
JUnit5 + Mockito is used. Provider tests mock RestService and either supply JsonNode (via ObjectMapper.readTree(...)) or real Elements (Jsoup) for deterministic parsing.

## 💬 Telegram bot commands (user-facing)
/start — register and receive a welcome message + last listings

/set_price_filter <min> <max> — set price range, e.g. /set_price_filter 500 1500

/set_room_filter <min> <max> — set rooms range, e.g. /set_room_filter 1 3

/except <text> — (placeholder) exclude listings with text (not implemented fully)

## 🧩 Architecture notes & extensibility
Providers extend AbstractFlatProvider and implement loadFlats(), buildFlat() and parseId(). New provider → add class + annotate (optionally @ConditionalOnProperty).

UrlMonitorService aggregates provider results periodically and calls FilterService.sendMessageToAll() for new flats.

RestService is a small HTTP/HTML/JSON helper with in-memory caches — consider adding TTL/size limits.

NotificationService wraps Telegram client interaction — in tests it is mocked, in runtime it uses OkHttpTelegramClient.

## 📫 Contact / Notes
If you want help adding a provider, hardening the scraper (rate-limiting, backoff), or setting up CI secrets & GitHub Actions for safe publish — open an issue or drop a PR and I can help.