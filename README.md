# Polling System

[![Java CI](https://github.com/Tanzeel0Hussain/Polling-System/actions/workflows/ci.yml/badge.svg)](https://github.com/Tanzeel0Hussain/Polling-System/actions/workflows/ci.yml)
[![Live Demo](https://img.shields.io/badge/Live-Demo-2357D8)](https://tanzeel0hussain.github.io/Polling-System/)
[![Download](https://img.shields.io/badge/Download-Latest_JAR-16845B)](https://github.com/Tanzeel0Hussain/Polling-System/releases/latest)
![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-Persistent_Data-003B57?logo=sqlite&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?logo=apachemaven&logoColor=white)

A secure desktop polling application built with **Java Swing**, **SQLite**, **Maven**, and **BCrypt**. The project began as a university GUI assignment and has been rebuilt into a cleaner portfolio-ready application with persistent data, role-based access, poll management, duplicate-vote protection, automated tests, CI, an interactive browser demo, and a downloadable desktop release.

> This is a general-purpose educational polling application. It is not intended to replace a certified public-election system.

## Try It

### Live browser demo

**https://tanzeel0hussain.github.io/Polling-System/**

The `docs/` demo lets visitors test the main workflow before installing anything:

- switch between voter and administrator preview modes
- create demo polls with multiple options
- open and close polls
- submit one demo vote per poll
- view vote totals and percentages
- reset browser demo data

The web demo stores sample data only in the visitor's browser using `localStorage`. It is a UI/workflow preview, not the Java runtime.

### Download desktop application

**https://github.com/Tanzeel0Hussain/Polling-System/releases/latest**

Download `polling-system-2.0.0.jar`, then run:

```bash
java -jar polling-system-2.0.0.jar
```

Java 17 or newer is required. The full desktop app uses SQLite persistence and BCrypt password hashing.

## Highlights

- Persistent SQLite database for users, polls, candidates/options, and votes
- BCrypt password hashing instead of plaintext password storage
- First-run administrator setup — no admin password is committed to the repository
- Separate voter and administrator authentication flows
- Voter registration with validation and unique usernames
- Admin dashboard with poll creation, open/close controls, statistics, and results
- Multiple candidates/options per poll
- Database-enforced **one account, one vote per poll** rule
- Vote confirmation before submission
- Results with vote totals and percentages
- Modern reusable Swing theme and responsive desktop layouts
- Maven build that creates a runnable shaded JAR
- JUnit 5 tests and GitHub Actions CI
- GitHub Pages interactive preview in `docs/`
- Automated GitHub Release workflow for the runnable JAR

## Application Flow

```text
First Run
   |
   +-- Create Administrator
   |
Welcome Screen
   |
   +-- Voter Login ------> Open Polls ------> Select Option ------> Submit Vote
   |
   +-- Create Account ---> Register Voter
   |
   +-- Admin Login ------> Create Poll ------> Open / Close ------> View Results
```

## Security Improvements

The original academic version stored users and votes in Java `HashMap` objects and contained a hard-coded administrator credential. Version 2 replaces that approach with:

- BCrypt password hashes
- SQLite persistence
- SQL prepared statements
- role checks for voter/admin authentication
- unique usernames using case-insensitive database constraints
- foreign-key relationships between polls, candidates, users, and votes
- a unique `(election_id, user_id)` database constraint to prevent duplicate voting
- first-run administrator creation instead of repository-stored credentials

## Technology Stack

| Layer | Technology |
| --- | --- |
| Desktop UI | Java Swing |
| Runtime | Java 17+ |
| Database | SQLite |
| Authentication | BCrypt |
| Build | Maven |
| Testing | JUnit 5 |
| CI/CD | GitHub Actions |
| Live preview | HTML, CSS, JavaScript, GitHub Pages |

## Project Structure

```text
Polling-System/
├── .github/workflows/
│   ├── ci.yml
│   ├── pages.yml
│   └── release.yml
├── docs/
│   ├── index.html
│   └── assets/
│       ├── style.css
│       └── app.js
├── src/
│   ├── main/java/com/tanzeel/polling/
│   │   ├── App.java
│   │   ├── data/Database.java
│   │   ├── model/User.java
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   └── PollService.java
│   │   └── ui/
│   │       ├── Theme.java
│   │       ├── WelcomeFrame.java
│   │       ├── AuthFrame.java
│   │       ├── UserDashboardFrame.java
│   │       └── AdminDashboardFrame.java
│   └── test/java/com/tanzeel/polling/
│       ├── AuthServiceTest.java
│       └── PollServiceTest.java
├── pom.xml
└── README.md
```

Runtime database files are created inside `data/` and are ignored by Git.

## Requirements

- JDK 17 or newer
- Maven 3.9+ recommended when building from source

Check your installation:

```bash
java -version
mvn -version
```

## Run from Source

```bash
git clone https://github.com/Tanzeel0Hussain/Polling-System.git
cd Polling-System
mvn clean test
mvn clean package
java -jar target/polling-system-2.0.0.jar
```

On the first launch, the application asks you to create an administrator account. No default administrator password is stored in the source code.

## Using the Application

### Administrator

1. Create the administrator account on first run.
2. Sign in through **Administrator**.
3. Select **Create Poll**.
4. Enter a title, optional description, and one candidate/option per line.
5. The poll opens immediately.
6. Close or reopen the poll from the dashboard.
7. Review live vote totals and percentages.

### Voter

1. Create a voter account.
2. Sign in through **Voter Login**.
3. Select an open poll.
4. Choose one candidate/option.
5. Confirm the vote.
6. The account cannot vote in that same poll again.

## Database Schema

```text
users
  id, username, password_hash, role, created_at

elections
  id, title, description, status, created_at

candidates
  id, election_id, name

votes
  id, election_id, user_id, candidate_id, created_at
```

The `votes` table has a unique constraint on `election_id + user_id`, which provides the final duplicate-vote protection even if a UI check is bypassed.

## Tests

Automated tests cover voter registration/authentication, incorrect-password rejection, duplicate usernames, first-admin creation, poll creation, successful voting, duplicate-vote rejection, result percentages, and closed-poll rejection.

GitHub Actions runs the test suite and Maven package build on every push and pull request to `main`.

## Original Academic Material

The repository keeps the original project report, presentation, and legacy source for historical/documentation purposes. The maintained application is the current `src/` implementation.

## Developer

**Tanzeel Hussain**  
BS Computer Science — Iqra University Islamabad

GitHub: [@Tanzeel0Hussain](https://github.com/Tanzeel0Hussain)
