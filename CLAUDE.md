# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Purpose

Scrapes the public ice-rink schedule at https://tondirabaicehall.ee/veebikalender/ and records slots of interest (`Vabajää`, `MTÜ Eesti Uisuliit`, `Ilu. vabajää`, `Iluuisutamisklubi Talveunistus`) as JSON files under `.days/`, one file per day (named `DD.MM.YYYY.json`). A GitHub Actions workflow runs the scraper on a cron schedule and auto-commits any changed JSON files back to `main`, so the git history itself is the change log of rink bookings.

## Commands

- `./gradlew run` — scrape live site and update `.days/*.json` (requires a local Chrome; Selenide runs headless).
- `./gradlew check` / `./gradlew test` — run JUnit 5 tests.
- `./gradlew test --tests ParserTest.check` — run a single test.
- JDK 21 required (configured via Kotlin `jvmToolchain(21)`).

## Architecture

Three Kotlin files in `src/main/kotlin/` plus one fixture-based test:

- `Main.kt` — entry point (`MainKt`). Opens the calendar page with Selenide headless, hands the page source to `Parser`, then diffs each returned `Day` against the existing `.days/<date>.json` file. Writes only when the file is missing or the parsed `Day` differs from the stored one — this is what makes the git log meaningful. After the loop it sends a single batched Telegram message via `Notifier` summarising added/removed slots per changed day.
- `Parser.kt` — uses jsoup to walk `.CCALdayCol` → `.CCALhour .CCALinterval .CCALevent`, extracting `data-name` / `data-start` / `data-resource`, and filters by `TYPES_OF_INTEREST`. Change this constant to track different slot types.
- `Day.kt` — `Day(date, slots)` and `Slot(type, start, resource)`, both `@Serializable` with `kotlinx.serialization`. `Json { prettyPrint = true }` is deliberate so diffs in git are readable.
- `Notifier.kt` — posts plain-text messages to the Telegram Bot API using `java.net.http.HttpClient`. Reads `TELEGRAM_BOT_TOKEN` and `TELEGRAM_CHAT_ID` from env and is a silent no-op if either is unset, so local runs and CI without secrets don't fail. Messages are truncated to 4000 chars (Telegram's limit is 4096).
- `Quotes.kt` + `src/main/resources/quotes.txt` — loads a list of Russian one-liners at startup. `quotes.txt` is one quote per line; blank lines and `#`-prefixed lines are ignored. `Main.kt` appends `Quotes.random()` to every Telegram notification. To add/remove quotes, just edit the text file — no code change needed.

`ParserTest` asserts against `src/test/resources/source.html` (a captured snapshot of the live page). If the site's HTML structure changes, refresh that fixture before adjusting selectors.

## CI

- `.github/workflows/run.yml` — cron-triggered scrape + auto-commit of `.days/*.json` via `stefanzweifel/git-auto-commit-action`. The cron is `* * * * *` in the file but GitHub clamps scheduled workflows to ~5-minute minimum cadence.
- `.github/workflows/test.yml` — runs `./gradlew check` on push/PR and auto-merges Dependabot minor updates.
