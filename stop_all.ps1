# ==============================================================================
#   DEVCLASH - 1-CLICK CLEAN SHUTDOWN SCRIPT
# ==============================================================================

Write-Host "Stopping DevClash processes..." -ForegroundColor Yellow

# Kill cloudflared
Get-Process cloudflared -ErrorAction SilentlyContinue | Stop-Process -Force

# Kill processes listening on DevClash ports:
# 1099 (RMI Registry), 8085 (Gateway Tomcat), 5173 (Frontend Vite)
$ports = @(1099, 8085, 5173)
foreach ($p in $ports) {
    $connections = Get-NetTCPConnection -LocalPort $p -ErrorAction SilentlyContinue
    if ($connections) {
        $pids = $connections | Select-Object -ExpandProperty OwningProcess -Unique
        foreach ($procId in $pids) {
            if ($procId -gt 4) {
                Write-Host "Terminating process $procId on port $p..." -ForegroundColor Red
                Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
            }
        }
    }
}

Write-Host "All DevClash processes stopped cleanly." -ForegroundColor Green
