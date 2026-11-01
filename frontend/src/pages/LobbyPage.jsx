import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Copy, Check, Play, LogOut, Users, Sparkles, Shield, Clock, AlertCircle } from 'lucide-react';
import { useGame } from '../context/GameContext';
import { PlayerCard } from '../components/lobby/PlayerCard';
import { Button } from '../components/common/Button';
import { Badge } from '../components/common/Badge';
import { PixelStar } from '../components/pixel/PixelArtAssets';
import { sfx } from '../utils/audio';

export const LobbyPage = () => {
  const { code } = useParams();
  const navigate = useNavigate();
  const {
    room,
    player,
    players,
    toggleReady,
    removePlayer,
    leaveRoom,
    startGame,
    currentQuestion,
    isGameStarted,
    refreshRoomState,
    errorMsg,
    clearError,
  } = useGame();

  const [copied, setCopied] = useState(false);
  const [startError, setStartError] = useState(null);

  // Navigate to /battle automatically when game starts for host and all connected players
  useEffect(() => {
    if (isGameStarted || currentQuestion) {
      navigate('/battle');
    }
  }, [isGameStarted, currentQuestion, navigate]);

  // If page reloads or user directly opens URL, refresh room state
  useEffect(() => {
    if (code) {
      refreshRoomState(code.toUpperCase());
    }
  }, [code]);

  // Determine current player
  const effectivePlayers = players && players.length > 0 ? players : [];
  const currentPlayer = effectivePlayers.find(
    (p) => p.playerId === player?.playerId || (player?.nickname && p.nickname === player?.nickname)
  ) || player;

  const isHost = Boolean(
    currentPlayer?.isHost ||
    currentPlayer?.host ||
    player?.isHost ||
    player?.host ||
    (room && currentPlayer?.playerId === room.hostPlayerId) ||
    (room && player?.playerId === room.hostPlayerId)
  );

  const isReady = Boolean(currentPlayer?.ready);
  const totalPlayersCount = effectivePlayers.length;
  const readyPlayersCount = effectivePlayers.filter((p) => p.ready).length;
  const allPlayersReady = totalPlayersCount >= 2 && effectivePlayers.every((p) => p.ready);
  const canStartMatch = isHost && totalPlayersCount >= 2 && allPlayersReady;

  const handleCopyCode = () => {
    sfx.playClick();
    navigator.clipboard.writeText(code || room?.roomCode || 'CLASH1');
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleToggleReady = () => {
    sfx.playClick();
    toggleReady(!isReady);
  };

  const handleStartBattle = async () => {
    setStartError(null);
    clearError();

    if (totalPlayersCount < 2) {
      setStartError('At least 2 players are required to start the battle.');
      sfx.playWrong();
      return;
    }

    if (!effectivePlayers.every((p) => p.ready)) {
      setStartError('All players in the lobby must be READY before starting.');
      sfx.playWrong();
      return;
    }

    try {
      sfx.playVictory();
      await startGame();
      navigate('/battle');
    } catch (err) {
      setStartError(err.message || 'Failed to start match.');
      sfx.playWrong();
    }
  };

  const handleRemovePlayer = (targetPlayerId) => {
    removePlayer(targetPlayerId);
  };

  const handleLeaveLobby = async () => {
    sfx.playClick();
    try {
      await leaveRoom(code, currentPlayer?.playerId || player?.playerId);
    } catch (e) {
      console.warn('Failed leaving room:', e);
    }
    navigate('/');
  };

  return (
    <div className="min-h-[calc(100vh-80px)] bg-gradient-to-b from-[#26005C] via-[#160033] to-[#160033] py-8 px-4 sm:px-6 lg:px-8 relative overflow-hidden select-none">
      {/* Background Pixel Ambience */}
      <div className="absolute top-12 right-12 animate-twinkle">
        <PixelStar size={20} />
      </div>

      <div className="max-w-6xl mx-auto relative z-10 space-y-8">
        {/* Top Header Card: Room Code Display */}
        <div className="p-6 md:p-8 bg-[#C9BDF5] border-4 border-[#160033] shadow-[8px_8px_0px_#160033]">
          <div className="flex flex-col md:flex-row items-center justify-between gap-6">
            <div>
              <div className="flex items-center gap-2 mb-2">
                <Badge variant="yellow" className="text-xs">
                  MULTIPLAYER LOBBY
                </Badge>
                <Badge variant="cyan" className="text-xs">
                  {room?.config?.gameMode || 'CLASSIC BATTLE'}
                </Badge>
                <Badge variant="pink" className="text-xs">
                  MAX 10 PLAYERS
                </Badge>
              </div>
              <h1 className="font-pixel text-xl sm:text-2xl md:text-3xl text-[#160033]">
                ARENA LOBBY: {code || room?.roomCode || 'CLASH1'}
              </h1>
              <p className="text-xs md:text-sm text-[#26005C] font-semibold mt-1">
                Share this 6-digit room code with other fighters to join the clash!
              </p>
            </div>

            {/* Room Code Box & Copy Button */}
            <div className="flex items-center gap-3 bg-[#160033] border-4 border-[#160033] p-3 shadow-[4px_4px_0px_#160033]">
              <div className="px-4 py-1.5 bg-[#FFE45C] border-2 border-[#160033]">
                <span className="font-pixel text-2xl sm:text-3xl text-[#160033] font-black tracking-widest">
                  {code || room?.roomCode || 'CLASH1'}
                </span>
              </div>
              <Button
                variant={copied ? 'cyan' : 'yellow'}
                size="md"
                onClick={handleCopyCode}
                icon={copied ? <Check className="w-4 h-4" /> : <Copy className="w-4 h-4" />}
              >
                {copied ? 'COPIED!' : 'COPY'}
              </Button>
            </div>
          </div>
        </div>

        {/* Start Error Banner */}
        {(startError || errorMsg) && (
          <div className="p-4 bg-[#FF62C7] border-4 border-[#160033] shadow-[4px_4px_0px_#160033] flex items-center gap-3 text-[#160033]">
            <AlertCircle className="w-6 h-6 shrink-0" />
            <div className="font-arcade text-xs sm:text-sm font-bold">
              {startError || errorMsg}
            </div>
          </div>
        )}

        {/* Middle Section: Players Grid & Battle Configuration */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
          {/* Left Column: Fighters Arena (8 cols) */}
          <div className="lg:col-span-8 space-y-4">
            <div className="flex items-center justify-between pb-2 border-b-2 border-[#A72CFF]">
              <div className="flex items-center gap-2 font-pixel text-sm md:text-base text-[#FFE45C]">
                <Users className="w-5 h-5" />
                <span>CONNECTED FIGHTERS ({totalPlayersCount} / 10)</span>
              </div>
              <span className="font-arcade text-xs text-[#38E5D1] font-bold">
                {readyPlayersCount} OF {totalPlayersCount} READY
              </span>
            </div>

            {/* Player Cards Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {effectivePlayers.map((p) => (
                <PlayerCard
                  key={p.playerId}
                  player={p}
                  isCurrentPlayer={p.playerId === currentPlayer?.playerId}
                  isHostUser={isHost}
                  onRemovePlayer={handleRemovePlayer}
                />
              ))}

              {/* Empty Fighter Slots (Up to 10 max) */}
              {Array.from({ length: Math.min(6, Math.max(0, 10 - totalPlayersCount)) }).map((_, i) => (
                <div
                  key={`empty-${i}`}
                  className="p-5 border-3 border-dashed border-[#26005C] bg-[#160033]/50 flex flex-col items-center justify-center text-center text-[#C9BDF5]/40"
                >
                  <span className="font-pixel text-[11px]">SLOT {totalPlayersCount + i + 1} / 10</span>
                  <span className="font-arcade text-[10px] mt-1">WAITING FOR FIGHTER...</span>
                </div>
              ))}
            </div>
          </div>

          {/* Right Column: Match Configuration & Controls (4 cols) */}
          <div className="lg:col-span-4 space-y-6">
            {/* Match Rules Card */}
            <div className="p-5 bg-[#26005C] border-4 border-[#160033] shadow-[6px_6px_0px_#160033] space-y-4">
              <h3 className="font-pixel text-xs text-[#FFE45C] pb-2 border-b-2 border-[#160033]">
                ARENA SETTINGS
              </h3>

              <div className="space-y-3 font-arcade text-xs">
                <div className="flex justify-between items-center text-[#F5EEFF]">
                  <span className="text-[#C9BDF5]">CATEGORY:</span>
                  <span className="font-bold text-[#38E5D1] truncate max-w-[150px]">
                    {room?.config?.categoryName || 'Programming'}
                  </span>
                </div>
                <div className="flex justify-between items-center text-[#F5EEFF]">
                  <span className="text-[#C9BDF5]">DIFFICULTY:</span>
                  <span className="font-bold text-[#FFE45C]">
                    {room?.config?.difficulty || 'MEDIUM'}
                  </span>
                </div>
                <div className="flex justify-between items-center text-[#F5EEFF]">
                  <span className="text-[#C9BDF5]">GAME MODE:</span>
                  <span className="font-bold text-[#FF62C7]">
                    {room?.config?.gameMode || 'CLASSIC'}
                  </span>
                </div>
                <div className="flex justify-between items-center text-[#F5EEFF]">
                  <span className="text-[#C9BDF5]">QUESTIONS:</span>
                  <span className="font-bold text-[#F5EEFF]">
                    {room?.config?.totalQuestions || 10} ROUNDS
                  </span>
                </div>
                <div className="flex justify-between items-center text-[#F5EEFF]">
                  <span className="text-[#C9BDF5]">TIME PER ROUND:</span>
                  <span className="font-bold text-[#F5EEFF]">
                    {room?.config?.questionTimeLimitSec || room?.config?.timeLimitSec || 20} SECONDS
                  </span>
                </div>
              </div>
            </div>

            {/* Player Action Buttons */}
            <div className="space-y-3">
              {/* Ready Toggle Button (Host is automatically ready, but can toggle if desired) */}
              <Button
                variant={isReady ? 'pink' : 'cyan'}
                size="lg"
                onClick={handleToggleReady}
                className="w-full text-center"
              >
                {isReady ? '✖ CANCEL READY' : '✔ I AM READY!'}
              </Button>

              {/* Start Battle (Host Only) */}
              {isHost ? (
                <div>
                  <Button
                    variant="yellow"
                    size="lg"
                    onClick={handleStartBattle}
                    disabled={!canStartMatch}
                    icon={<Play className="w-5 h-5 fill-[#160033]" />}
                    className={`w-full text-center ${
                      canStartMatch
                        ? 'animate-pulse shadow-[0_0_15px_#FFE45C]'
                        : 'opacity-60 cursor-not-allowed'
                    }`}
                  >
                    START BATTLE NOW
                  </Button>
                  {!canStartMatch && (
                    <p className="font-arcade text-[10px] text-[#FFE45C] text-center mt-2 leading-tight">
                      {totalPlayersCount < 2
                        ? '⚠ Requires at least 2 fighters to start match'
                        : '⚠ Waiting for all fighters to be READY'}
                    </p>
                  )}
                </div>
              ) : (
                <div className="p-3 bg-[#160033] border-2 border-[#26005C] text-center">
                  <span className="font-arcade text-xs text-[#C9BDF5] animate-pulse">
                    ⏳ WAITING FOR HOST TO START BATTLE...
                  </span>
                </div>
              )}

              {/* Leave Room Button */}
              <Button
                variant="dark"
                size="md"
                onClick={handleLeaveLobby}
                icon={<LogOut className="w-4 h-4" />}
                className="w-full text-center"
              >
                LEAVE ROOM
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
