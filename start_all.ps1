# ==============================================================================
#   DEVCLASH - 1-CLICK COMPLETE FULL-STACK STARTUP SCRIPT
# ==============================================================================

$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $Root

$MavenPath = "$env:USERPROFILE\tools\apache-maven-3.9.9\bin\mvn.cmd"
if (-not (Test-Path $MavenPath)) {
    $MavenPath = "mvn"
}

Write-Host "==============================================================" -ForegroundColor Cyan
Write-Host "               DEVCLASH FULL-STACK LAUNCHER                  " -ForegroundColor Yellow
Write-Host "==============================================================" -ForegroundColor Cyan
Write-Host ""

# 1. Start Java RMI Server (Port 1099)
Write-Host "[1/4] Starting Java RMI Server on Port 1099..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$Root'; `$host.UI.RawUI.WindowTitle = 'DevClash - 1. RMI Server (1099)'; & '$MavenPath' exec:java -pl rmi-server"

Start-Sleep -Seconds 4

# 2. Start Spring Boot Gateway (Port 8085)
Write-Host "[2/4] Starting Spring Boot Gateway on Port 8085..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$Root'; `$host.UI.RawUI.WindowTitle = 'DevClash - 2. Gateway (8085)'; & '$MavenPath' spring-boot:run -pl gateway"

Start-Sleep -Seconds 4

# 3. Start Frontend Vite Server (Port 5173)
Write-Host "[3/4] Starting React Frontend on Port 5173..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$Root\frontend'; `$host.UI.RawUI.WindowTitle = 'DevClash - 3. React Frontend (5173)'; npm run dev"

Start-Sleep -Seconds 3

# 4. Start Cloudflare Tunnel
Write-Host "[4/4] Starting Cloudflare Public Internet Tunnel..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$Root'; `$host.UI.RawUI.WindowTitle = 'DevClash - 4. Public Internet Tunnel'; powershell -ExecutionPolicy Bypass -File .\start_public_tunnel.ps1"

Write-Host ""
Write-Host "==============================================================" -ForegroundColor Green
Write-Host "All 4 DevClash processes have been launched in separate windows!" -ForegroundColor Yellow
Write-Host "Look at the 'DevClash - 4. Public Internet Tunnel' window for your URL." -ForegroundColor Cyan
Write-Host "==============================================================" -ForegroundColor Green
