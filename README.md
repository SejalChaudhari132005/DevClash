# DevClash – Multiplayer Tech Quiz Battle

A real-time, distributed multiplayer quiz battle platform designed for competitive computer science and programming quizzes.

Built as an academic **Distributed Computing MicroProject**, DevClash demonstrates **Java RMI / RPC** remote procedure calls, multi-client real-time synchronization, concurrency control, and clean separation of concerns.

---

## ⚡ Architecture at a Glance

```
[ Frontend: React + Vite ] ──(WebSocket STOMP)──> [ Gateway: Spring Boot ] ──(Java RMI)──> [ Compute: RMI Server ] ──(JDBC)──> [ MySQL DB ]
```

- **Frontend:** React 19, Vite, Tailwind CSS, Framer Motion, Lucide React, STOMP/SockJS
- **Gateway:** Spring Boot 3.3.4 (WebSocket Broker, HTTP Gateway, RMI Proxy & Callback Handler)
- **RMI Server:** Standalone Java SE 21 Process (Port 1099, `UnicastRemoteObject`, `ConcurrentHashMap`, Thread Pools)
- **Database:** MySQL 8.0 with pre-seeded question bank across 6 computer science disciplines

---

## 🚀 Quick Start

### 1. Database
```bash
mysql -u root -p < database/schema.sql
```

### 2. RMI Server (Terminal 1)
```bash
cd rmi-server
mvn compile exec:java
```

### 3. Gateway (Terminal 2)
```bash
cd gateway
mvn spring-boot:run
```
*(Running on port `8085` to avoid Oracle DB port 8080 collisions)*

### 4. Frontend (Terminal 3)
```bash
cd frontend
npm install
npm run dev
```

Visit `http://localhost:5173` in multiple browser windows or devices on the same Wi-Fi network!

---

## 🧪 Java RMI Standalone Testing & Verification

DevClash includes automated RMI test suites demonstrating independent distributed process communication:

### A. Run 9-Phase Standalone RMI Test Suite
```powershell
cd rmi-server
mvn exec:java -Ptest-client
```
Verifies: Remote registry connection, stub lookup, ping, room creation, remote player joining, readiness toggle, synchronized room state, battle start, active question fetch, remote answer validation & scoring, leaderboard retrieval, and match finalization.

### B. Run Cross-Process Multi-Client Demonstration
```powershell
# From project root
powershell -ExecutionPolicy Bypass -File .\demo_dual_clients.ps1
```
Demonstrates:
1. Spring Boot Gateway creates room remotely via RMI stub.
2. Standalone Java test client joins that room from an independent JVM process via RMI.
3. Both clients receive identical synchronized state from the RMI server.

### C. Run Tech Battle Quiz Engine Independent Test Suite
```powershell
# Automated Maven JUnit 5 suite (9 tests)
mvn test -pl rmi-server

# Standalone interactive test runner with colored output
mvn compile exec:java -pl rmi-server -Pengine-test
```
Verifies:
1. All 6 categories populated with >=15 quality questions (96 total in bank).
2. Coverage across EASY, MEDIUM, and HARD difficulties.
3. Strict 4-option structure and exactly 1 correct answer per question.
4. Server-side scoring: 500 base points + speed bonus up to 500 points.
5. Incorrect and timeout answers award strictly 0 points.
6. Game modes (Classic: 20s, Speed Rush: 10s, Brain Battle: 30s).
7. Reflection security checks: `QuestionDTO` never exposes answer keys or explanations.
8. Random non-repeating question selection across consecutive matches.

---

## 📖 Documentation
- [Tech Battle Quiz Engine Specifications](docs/quiz_engine.md)
- [Architecture Specifications](docs/architecture.md)
- [Setup & Troubleshooting](docs/setup.md)
- [Database Schema & 96-Question Seed Bank](database/schema.sql)

