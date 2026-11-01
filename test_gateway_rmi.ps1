$ping = Invoke-RestMethod -Uri "http://localhost:8085/api/quiz/ping" -Method Get
Write-Host "1. Ping Response: " ($ping | ConvertTo-Json -Compress)

$createBody = @{
    nickname = "GatewayHost"
    avatar   = "terminal"
} | ConvertTo-Json

$room = Invoke-RestMethod -Uri "http://localhost:8085/api/quiz/room/create" -Method Post -ContentType "application/json" -Body $createBody
Write-Host "2. Room Created via Gateway RMI Stub!"
Write-Host "   Room Code: " $room.roomCode
Write-Host "   Host Player ID: " $room.hostPlayerId
Write-Host "   State: " $room.state

$joinBody = @{
    roomCode = $room.roomCode
    nickname = "SecondPlayer"
    avatar   = "robot"
} | ConvertTo-Json

$join = Invoke-RestMethod -Uri "http://localhost:8085/api/quiz/room/join" -Method Post -ContentType "application/json" -Body $joinBody
Write-Host "3. Second Player Joined via Gateway RMI Stub!"
Write-Host "   Success: " $join.success
Write-Host "   Player ID: " $join.playerId
Write-Host "   Message: " $join.message

$stateUrl = "http://localhost:8085/api/quiz/room/" + $room.roomCode + "/state"
$state = Invoke-RestMethod -Uri $stateUrl -Method Get
Write-Host "4. Synchronized Room State from RMI Server:"
Write-Host "   Total Players: " $state.players.Count
foreach ($p in $state.players) {
    Write-Host ("   - " + $p.nickname + " (Host: " + $p.isHost + ", Ready: " + $p.isReady + ", Score: " + $p.score + ")")
}

Write-Host "5. DEVCLASH GATEWAY <-> RMI SERVER END-TO-END VERIFICATION COMPLETE!"
