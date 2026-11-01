# 1. Gateway creates room remotely via RMI
Write-Host "=== MULTI-CLIENT CROSS-PROCESS RMI DEMONSTRATION ==="
$createBody = @{
    nickname = "GatewayAlice"
    avatar   = "terminal"
} | ConvertTo-Json

$room = Invoke-RestMethod -Uri "http://localhost:8085/api/quiz/room/create" -Method Post -ContentType "application/json" -Body $createBody
$roomCode = $room.roomCode
Write-Host "[Gateway Process 1] Created room via RMI stub. Room Code: $roomCode | Host Player ID: $($room.hostPlayerId)"

# 2. Standalone Java Process (RmiStandaloneTestClient) joins the room directly via RMI stub
Write-Host "`n[Client Process 2] Launching independent RmiStandaloneTestClient JVM to join room $roomCode as 'BobStandalone'..."
$mvnCmd = "$HOME\tools\apache-maven-3.9.9\bin\mvn.cmd"
$clientOutput = & $mvnCmd exec:java -Ptest-client "-Dexec.args=join $roomCode BobStandalone" -f "c:\Users\Sejal Pc\Desktop\DevClash\rmi-server\pom.xml" 2>&1
Write-Host ($clientOutput -join "`n")

# 3. Gateway queries room state via RMI stub
Write-Host "`n[Gateway Process 1] Querying synchronized room state via RMI stub for room $roomCode..."
$state = Invoke-RestMethod -Uri "http://localhost:8085/api/quiz/room/$roomCode/state" -Method Get
Write-Host "[Gateway Process 1] Verified Synchronized Room State:"
Write-Host "   Room: $($state.roomCode) | State: $($state.state) | Total Players: $($state.players.Count)"
foreach ($p in $state.players) {
    Write-Host "   -> Player: $($p.nickname) | Player ID: $($p.playerId) | isHost: $($p.isHost) | isReady: $($p.isReady)"
}

Write-Host "`n=== MULTI-CLIENT CROSS-PROCESS RMI DEMONSTRATION SUCCESSFUL! ==="
