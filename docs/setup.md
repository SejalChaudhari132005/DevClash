# DevClash Setup & Run Guide

## Prerequisites
- **Java Development Kit (JDK):** Version 21 or higher
- **Apache Maven:** Version 3.8+ (Configured in `$HOME\tools\apache-maven-3.9.9\bin` or on system PATH)
- **Node.js & npm:** Node.js v20+ and npm v10+
- **MySQL Server:** Version 8.0+ running on port 3306

---

## 1. Database Setup
1. Ensure the MySQL service is running.
2. Initialize the schema and seed questions:
```bash
mysql -u root -p < database/schema.sql
```
*(Enter your MySQL root password when prompted)*

---

## 2. Environment Configuration
Copy `.env.example` to `.env` in each module if needed:
- `rmi-server/.env`: Configure `DB_PASSWORD` and `RMI_PORT=1099`.
- `gateway/.env`: Configure `SERVER_PORT=8085` and `RMI_SERVER_PORT=1099`.
- `frontend/.env`: Configure `VITE_GATEWAY_WS_URL=http://localhost:8085/ws-devclash`.

---

## 3. Starting the Components (In Order)

### Step 1: Start the RMI Quiz Server (Terminal 1)
```powershell
cd rmi-server
mvn compile exec:java
```
*Expected output: `[RMI Server] Remote Object bound successfully! rmi://localhost:1099/DevClashQuizService`*

### Step 2: Start the Spring Boot Gateway (Terminal 2)
```powershell
cd gateway
mvn spring-boot:run
```
*Expected output: `Started GatewayApplication on port 8085`*

### Step 3: Start the React Frontend (Terminal 3)
```powershell
cd frontend
npm run dev
```
*Open http://localhost:5173 in multiple browsers or physical devices on the local network.*

---

## 4. Standalone RMI Test Suite Execution

### Running the End-to-End Test Client:
```powershell
cd rmi-server
mvn exec:java -Ptest-client
```

### Running the Cross-Process Dual Client Test:
```powershell
powershell -ExecutionPolicy Bypass -File .\demo_dual_clients.ps1
```
