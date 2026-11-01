# DevClash System Architecture & Technical Specifications

## 1. Architectural Overview
DevClash is a distributed, real-time multiplayer tech quiz battle application built for academic demonstration of Distributed Computing principles:
- **Presentation Tier (React + Vite):** Web clients running in browsers across multiple physical devices.
- **Gateway Tier (Spring Boot: Port 8080):** Handles HTTP REST and WebSocket STOMP connections. Acts as an RMI client proxy and exports an RMI Callback listener.
- **Distributed Compute Tier (Java RMI Server: Port 1099):** Standalone JVM process running `QuizGameServiceImpl` exported as a `UnicastRemoteObject`. Manages in-memory room states, countdown timers, and answer evaluation.
- **Persistence Tier (MySQL: Port 3306):** Normalized database storing question banks, historical matches, and player stats.

## 2. Process Boundaries & Port Mapping
| Process | Technology | Port | Protocol | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| **Frontend** | React / Vite | `5173` | HTTP / WS | User interface, animations, audio |
| **Gateway** | Spring Boot | `8080` | HTTP / STOMP | WebSocket broker, RMI proxy |
| **RMI Registry & Server** | Java SE | `1099` | JRMP (Java RMI) | Distributed game logic, rooms |
| **Database** | MySQL Server | `3306` | TCP/IP | Questions & match logs |

## 3. Distributed Communication Flow
```
[React Browser Client] 
       ↕ WebSocket / STOMP
[Spring Boot Gateway :8080]
       ↕ Java RMI (JRMP RPC & Remote Callbacks)
[Java RMI Server :1099]
       ↕ JDBC / HikariCP Pool
[MySQL Database :3306]
```

## 4. Concurrency & Synchronization
1. **Thread-Safe Room Management:** Rooms are stored in a thread-safe `ConcurrentHashMap`. Each room is isolated from other rooms.
2. **Double-Answer Prevention:** Players can only submit one answer per question round; submissions are tracked atomically.
3. **Timer Synchronization:** Bounded `ScheduledThreadPoolExecutor` triggers 1-second countdown ticks and broadcasts state updates to all connected players simultaneously.
4. **Reconnection Handling:** Disconnected players can re-join the active match using a persistent `sessionToken` without corrupting active game state.
