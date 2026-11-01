Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   DEVCLASH - STARTING CLOUDFLARE PUBLIC INTERNET TUNNEL  " -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "Forwarding traffic from Cloudflare's public HTTPS edge to local frontend (http://localhost:5173)..." -ForegroundColor Green
Write-Host "Look for the public HTTPS URL ending in .trycloudflare.com below:" -ForegroundColor Yellow
Write-Host ""

if (Test-Path ".\cloudflared.exe") {
    .\cloudflared.exe tunnel --url http://127.0.0.1:5173
} elseif (Test-Path "$env:USERPROFILE\tools\cloudflared.exe") {
    & "$env:USERPROFILE\tools\cloudflared.exe" tunnel --url http://127.0.0.1:5173
} else {
    Write-Error "cloudflared.exe not found. Please install cloudflared or run scratch/download_cloudflared.ps1."
}
