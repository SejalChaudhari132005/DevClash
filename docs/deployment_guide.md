# DevClash – Complete Production Deployment & Networking Guide
**Distributed Computing MicroProject: Real-Time Multiplayer Tech Quiz Battle**  
**Architecture**: React (Vite) + Spring Boot Gateway + Standalone Java RMI Quiz Server + MySQL 8.0

---

## 1. Network Architecture Diagram

The diagram below illustrates the end-to-end distributed system flow, demonstrating the strict network isolation between public web traffic and the internal Java RMI distributed computing layer.

```mermaid
flowchart TD
    subgraph PUBLIC_INTERNET["Public Internet (Cross-Network / Multi-Device)"]
        Laptop1["💻 Laptop 1 (Host - Home Wi-Fi)"]
        Phone2["📱 Android Phone (Player 2 - 4G/5G Cellular)"]
        Laptop3["💻 Laptop 2 (Player 3 - College Campus Wi-Fi)"]
        Phone4["📱 iPhone (Player 4 - Coffee Shop Wi-Fi)"]
    end

    subgraph CLOUD_DMZ["Public Security DMZ (Port 80 / 443)"]
        Nginx["🌐 Nginx Reverse Proxy / TLS Terminator\n• Serves Production React SPA (dist/)\n• Terminates HTTPS & Secure WSS\n• Rate Limiting & Gzip"]
    end

    subgraph PRIVATE_VPC["Isolated Private Backend Network (No Direct Public Access)"]
        Gateway["⚙️ Spring Boot Gateway (Port 8085)\n• STOMP WebSocket Broker (/topic, /app)\n• REST API (/api/quiz/*, /api/health)\n• RMI Dynamic Client Proxy\n• RMI Callback Receiver (Port 1097)"]
        
        subgraph RMI_CLUSTER["Distributed Computing Tier"]
            RMIServer["⚡ Standalone Java RMI Server (Port 1099 & 1098)\n• RMI Registry (Port 1099)\n• Exported QuizRemote Stub (Port 1098)\n• Authoritative Match Engine & Timers\n• TechBattle Scoring & Tie-Breaker"]
        end
        
        subgraph DB_CLUSTER["Persistence Tier"]
            MySQL[("🗄️ MySQL 8.0 Database (Port 3306)\n• Questions & Answers Pool\n• Match History & Player Scores\n• Category Metadata")]
        end
    end

    Laptop1 -->|HTTPS/WSS:443| Nginx
    Phone2 -->|HTTPS/WSS:443| Nginx
    Laptop3 -->|HTTPS/WSS:443| Nginx
    Phone4 -->|HTTPS/WSS:443| Nginx

    Nginx -->|HTTP Proxy: /api/*| Gateway
    Nginx -->|Upgrade: /ws-devclash| Gateway

    Gateway -->|JRMP TCP: 1099 (Registry)| RMIServer
    Gateway -->|JRMP TCP: 1098 (Object Invocation)| RMIServer
    RMIServer -->|JRMP Callback: 1097| Gateway
    RMIServer -->|JDBC TCP: 3306| MySQL

    classDef public fill:#1e1b4b,stroke:#818cf8,stroke-width:2px,color:#ffffff;
    classDef dmz fill:#064e3b,stroke:#34d399,stroke-width:2px,color:#ffffff;
    classDef vpc fill:#3b0764,stroke:#c084fc,stroke-width:2px,color:#ffffff;
    classDef rmi fill:#7f1d1d,stroke:#f87171,stroke-width:2px,color:#ffffff;
    classDef db fill:#0c4a6e,stroke:#38bdf8,stroke-width:2px,color:#ffffff;

    class Laptop1,Phone2,Laptop3,Phone4 public;
    class Nginx dmz;
    class Gateway vpc;
    class RMIServer rmi;
    class MySQL db;
```

---

## 2. Platform Compatibility & RMI Hosting Limitations

### Why Serverless / Traditional PaaS Fails for Java RMI
Standard serverless platforms (e.g., **Vercel, Netlify, AWS Lambda**) and conventional PaaS with ephemeral routing (e.g., **Heroku, basic Render web services**) **cannot host the Java RMI Server**:
1. **Raw TCP / JRMP Protocol**: Java RMI does not use HTTP; it operates directly on raw TCP using Java Remote Method Protocol (JRMP). Serverless edge networks and HTTP-only proxies drop non-HTTP packets.
2. **Two-Port Protocol**: Java RMI requires at least two TCP ports:
   - Port `1099`: The RMI Registry lookup service.
   - Port `1098` (or dynamic OS port): The exported `UnicastRemoteObject` stub where actual method dispatch executes.
   PaaS platforms typically map a single dynamic `$PORT` for HTTP and terminate external TCP sockets.
3. **Persistent In-Memory State**: DevClash's match engine holds active game rooms, synchronized countdown timers, and player connection states in JVM memory. Serverless runtimes spin down on idle, which destroys active quiz rooms.

### Recommended Production Platforms
| Hosting Solution | Compatibility | Setup Complexity | Recommendation |
| :--- | :--- | :--- | :--- |
| **Cloud Linux VPS (AWS EC2, DigitalOcean, Hetzner, Linode)** | **100% Native** | Low (Docker Compose or Systemd) | ⭐ **Recommended**: Full control over ports 80, 443, and private network 1099/1098. |
| **Docker Compose on Single VM** | **100% Native** | Minimal | ⭐ **Fastest for MicroProject**: Nginx + Gateway + RMI Server + MySQL containerized together. |
| **AWS ECS / DigitalOcean Kubernetes** | **100% Native** | Medium | Suitable for scaled enterprise setups with private VPC networking. |
| **Cloudflare Tunnel (Local Server Demo)** | **100% Native** | Very Low | ⭐ **Best for Zero-Cost Multi-Device Testing**: Host on your laptop, expose securely via tunnel to public internet. |

---

## 3. Production Environment Configuration

Create `.env` based on the production templates:

### RMI Server (`rmi-server/.env`)
```bash
# Private IP / Hostname of the RMI Server machine (e.g., within VPC / Docker network)
RMI_HOST=rmi-server
RMI_PORT=1099
RMI_OBJECT_PORT=1098
RMI_SERVICE_NAME=DevClashQuizService

# MySQL Database
DB_HOST=database
DB_PORT=3306
DB_NAME=devclash_db
DB_USER=devclash_user
DB_PASSWORD=ProdSecurePassword_2026!
DB_POOL_SIZE=20
DB_MIN_IDLE=5
```

### Gateway (`gateway/.env`)
```bash
SERVER_PORT=8085

# RMI Server connection details
RMI_SERVER_HOST=rmi-server
RMI_SERVER_PORT=1099
RMI_SERVICE_NAME=DevClashQuizService

# RMI Callback Listener details
RMI_CALLBACK_PORT=1097
RMI_CALLBACK_HOST=gateway

# Origin Restrictions (comma-separated list of public HTTPS domains)
CORS_ALLOWED_ORIGINS=https://devclash.yourdomain.com,https://www.yourdomain.com

# Reverse Proxy headers
SERVER_FORWARD_HEADERS_STRATEGY=framework
```

### Frontend (`frontend/.env.production`)
```bash
# When Nginx serves the React bundle on the same domain, leave empty.
# GameContext automatically resolves https://<host> and wss://<host>/ws-devclash.
VITE_GATEWAY_URL=
VITE_GATEWAY_WS_URL=
```

---

## 4. Deployment Methods

### Option A: Complete Docker Compose Deployment (Recommended for Cloud VPS)

1. **Clone repository onto VPS** (e.g. Ubuntu 22.04 LTS on AWS EC2 or DigitalOcean):
   ```bash
   git clone https://github.com/your-username/DevClash.git /opt/devclash
   cd /opt/devclash
   ```

2. **Configure Environment**:
   ```bash
   cp .env.production.example .env
   nano .env # update passwords and domain names
   ```

3. **Install SSL Certificate via Certbot**:
   ```bash
   sudo apt update && sudo apt install -y certbot
   sudo certbot certonly --standalone -d devclash.yourdomain.com
   # Copy certificates to volume path:
   sudo mkdir -p /opt/devclash/nginx/ssl
   sudo cp /etc/letsencrypt/live/devclash.yourdomain.com/fullchain.pem /opt/devclash/nginx/ssl/
   sudo cp /etc/letsencrypt/live/devclash.yourdomain.com/privkey.pem /opt/devclash/nginx/ssl/
   ```

4. **Launch all services in isolated networks**:
   ```bash
   docker compose -f docker-compose.prod.yml up -d --build
   ```

5. **Verify running containers**:
   ```bash
   docker compose -f docker-compose.prod.yml ps
   ```

---

### Option B: Bare-Metal / Native Systemd Deployment (Multi-Server Setup)

If deploying across separate physical servers:
- **Server 1 (Public DMZ)**: Nginx (Port 80/443) + Spring Boot Gateway (Port 8085).
- **Server 2 (Internal Backend)**: Java RMI Server (Ports 1099 & 1098) + MySQL (Port 3306).

#### 1. Setup RMI Server (Server 2)
1. Install Java 21 & MySQL 8.0:
   ```bash
   sudo apt install -y openjdk-21-jdk mysql-server
   ```
2. Set Firewall Rules (UFW):
   ```bash
   sudo ufw default deny incoming
   sudo ufw allow 22/tcp
   # Allow RMI and MySQL ONLY from Server 1 Private IP:
   sudo ufw allow from 10.0.1.10 to any port 1099 proto tcp comment 'RMI Registry from Gateway'
   sudo ufw allow from 10.0.1.10 to any port 1098 proto tcp comment 'RMI Object from Gateway'
   sudo ufw allow from 10.0.1.10 to any port 3306 proto tcp comment 'MySQL from Gateway/RMI'
   sudo ufw enable
   ```
3. Run RMI Server as a Systemd Service (`/etc/systemd/system/devclash-rmi.service`):
   ```ini
   [Unit]
   Description=DevClash Java RMI Quiz Server
   After=network.target mysql.service

   [Service]
   Type=simple
   User=devclash
   WorkingDirectory=/opt/devclash/rmi-server
   Environment="RMI_HOST=10.0.1.30"
   Environment="RMI_PORT=1099"
   Environment="RMI_OBJECT_PORT=1098"
   Environment="RMI_SERVICE_NAME=DevClashQuizService"
   Environment="DB_HOST=localhost"
   Environment="DB_PORT=3306"
   Environment="DB_NAME=devclash_db"
   Environment="DB_USER=devclash_user"
   Environment="DB_PASSWORD=YourSecurePass"
   ExecStart=/usr/bin/java -cp target/devclash-rmi-server-1.0.0-SNAPSHOT.jar:../common/target/devclash-common-1.0.0-SNAPSHOT.jar com.devclash.rmiserver.RmiServerApplication
   Restart=always
   RestartSec=5

   [Install]
   WantedBy=multi-user.target
   ```
4. Start RMI Server:
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable --now devclash-rmi
   ```

#### 2. Setup Gateway & Frontend (Server 1)
1. Build Frontend:
   ```bash
   cd /opt/devclash/frontend
   npm ci && npm run build
   # Copy build output to Nginx root:
   sudo cp -r dist/* /var/www/devclash/
   ```
2. Run Gateway as a Systemd Service (`/etc/systemd/system/devclash-gateway.service`):
   ```ini
   [Unit]
   Description=DevClash Spring Boot Gateway
   After=network.target

   [Service]
   Type=simple
   User=devclash
   WorkingDirectory=/opt/devclash/gateway
   Environment="SERVER_PORT=8085"
   Environment="RMI_SERVER_HOST=10.0.1.30"
   Environment="RMI_SERVER_PORT=1099"
   Environment="RMI_SERVICE_NAME=DevClashQuizService"
   Environment="RMI_CALLBACK_HOST=10.0.1.10"
   Environment="RMI_CALLBACK_PORT=1097"
   Environment="CORS_ALLOWED_ORIGINS=https://devclash.yourdomain.com"
   ExecStart=/usr/bin/java -jar target/devclash-gateway-1.0.0-SNAPSHOT.jar
   Restart=always
   RestartSec=5

   [Install]
   WantedBy=multi-user.target
   ```
3. Start Gateway & Nginx:
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable --now devclash-gateway
   sudo systemctl restart nginx
   ```

---

## 5. Testing with Mobile Data and Separate Wi-Fi Networks

To test with different laptops and mobile phones connected via 4G/5G mobile data and external Wi-Fi networks **without purchasing a public domain or cloud VM**:

### Method: Cloudflare Tunnels (Zero-Cost, Secure HTTPS/WSS Public URL)
Cloudflare Tunnel connects your local machine to Cloudflare's global edge network, creating a secure public `https://` and `wss://` URL accessible from any device anywhere in the world without port-forwarding on your home router.

1. **Download Cloudflared CLI**:
   - Windows: `winget install --id Cloudflare.cloudflared` or download from [Cloudflare Releases](https://github.com/cloudflare/cloudflared/releases).
   - Linux: `sudo apt install cloudflared`
   - macOS: `brew install cloudflared`

2. **Start DevClash Services Locally**:
   - RMI Server on port 1099
   - Gateway on port 8085
   - Frontend on port 5173 (or served via local Nginx)

3. **Expose Gateway and Frontend**:
   Run a quick tunnel for the frontend:
   ```bash
   cloudflared tunnel --url http://localhost:5173
   ```
   *Cloudflared outputs an official public HTTPS URL (e.g. `https://random-words.trycloudflare.com`).*

4. **Run Cross-Network Test**:
   - **Laptop 1 (Host)**: Open `https://random-words.trycloudflare.com` on Home Wi-Fi. Create room.
   - **Phone 1 (Player 2)**: Turn off Wi-Fi on Android / iPhone. Enable **Mobile Data (4G/5G)**. Open `https://random-words.trycloudflare.com`. Enter Room Code and join.
   - **Laptop 2 (Player 3)**: Connect to Mobile Hotspot or Neighbor/College Wi-Fi. Join room.
   - **Verification**:
     - All devices appear in lobby simultaneously.
     - Host starts match.
     - Authoritative timer ticks in sync.
     - Independent answers are recorded and scores calculated by the RMI server.
     - Final podium displayed identically on all screens.

---

## 6. How to Verify Remote RMI Communication in Production

To prove that communication is actively using **Java RMI (JRMP)** and not silently falling back:

1. **Verify Gateway RMI Health Endpoint**:
   ```bash
   curl -s https://devclash.yourdomain.com/api/health | jq .
   ```
   Expected response:
   ```json
   {
     "gatewayStatus": "UP",
     "service": "DevClash Gateway",
     "timestamp": 1793529000000,
     "rmiServerStatus": "UP"
   }
   ```

2. **Verify Remote RMI Stub Ping**:
   ```bash
   curl -s https://devclash.yourdomain.com/api/quiz/ping | jq .
   ```
   Expected response:
   ```json
   {
     "success": true,
     "rmiConnected": true,
     "message": "Successfully invoked ping() on remote RMI Quiz Server!"
   }
   ```

3. **Inspect RMI Registry Bindings directly using `jdk.rmiregistry` tools**:
   On the server running the Gateway or inside the Docker network:
   ```bash
   # Query RMI Registry on the RMI Server host
   $JAVA_HOME/bin/java -cp common/target/devclash-common-1.0.0-SNAPSHOT.jar \
     -e "System.out.println(java.util.Arrays.toString(java.rmi.registry.LocateRegistry.getRegistry('rmi-server', 1099).list()));"
   ```
   Output:
   ```
   [DevClashQuizService, QuizGameService]
   ```

4. **Inspect Active TCP Sockets**:
   ```bash
   # On Gateway: verify established TCP connection to RMI Server port 1099/1098
   netstat -ant | grep -E '1099|1098'
   ```
   Shows `ESTABLISHED` TCP connections between the Gateway and RMI Server processes.

5. **Stop RMI Server to Verify Gateway Graceful Degradation**:
   ```bash
   # Temporarily stop RMI Server
   docker compose -f docker-compose.prod.yml stop rmi-server
   
   # Check health endpoint:
   curl -s http://localhost:8085/api/health
   # Returns: {"gatewayStatus":"UP","rmiServerStatus":"DOWN"}
   
   # Restart RMI Server:
   docker compose -f docker-compose.prod.yml start rmi-server
   # Gateway dynamic proxy automatically recovers and reconnects on next request!
   ```

---

## 7. Service Restart Instructions

### Docker Compose Restart
```bash
# Restart all services gracefully:
docker compose -f docker-compose.prod.yml restart

# Restart only the RMI Server:
docker compose -f docker-compose.prod.yml restart rmi-server

# Restart only the Gateway:
docker compose -f docker-compose.prod.yml restart gateway

# Full zero-downtime rolling rebuild:
docker compose -f docker-compose.prod.yml up -d --no-deps --build gateway
```

### Systemd Linux Service Restart
```bash
# Restart MySQL:
sudo systemctl restart mysql

# Restart RMI Server:
sudo systemctl restart devclash-rmi

# Restart Gateway:
sudo systemctl restart devclash-gateway

# Reload Nginx:
sudo systemctl reload nginx

# Check status:
sudo systemctl status devclash-rmi devclash-gateway
```
