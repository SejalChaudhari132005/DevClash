import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import confetti from 'canvas-confetti';
import {
  RotateCcw,
  Home,
  Sparkles,
  Award,
  Target,
  Zap,
  Clock,
  CheckCircle2,
  XCircle,
  Trophy,
  Flame,
} from 'lucide-react';
import { useGame } from '../context/GameContext';
import { BattleLeaderboard } from '../components/battle/BattleLeaderboard';
import { Button } from '../components/common/Button';
import { Badge } from '../components/common/Badge';
import { PixelTrophy, PixelAvatarSprite, PixelStar } from '../components/pixel/PixelArtAssets';
import { sfx } from '../utils/audio';

export const ResultsPage = () => {
  const navigate = useNavigate();
  const { gameSummary, room, player, players, leaveRoom, resetGame } = useGame();

  // Multi-burst celebration confetti and victory theme sound
  useEffect(() => {
    sfx.playVictory();

    const duration = 4 * 1000;
    const end = Date.now() + duration;

    const frame = () => {
      confetti({
        particleCount: 6,
        angle: 60,
        spread: 70,
        origin: { x: 0, y: 0.7 },
        colors: ['#FFE45C', '#38E5D1', '#FF62C7', '#A72CFF', '#C9BDF5'],
      });
      confetti({
        particleCount: 6,
        angle: 120,
        spread: 70,
        origin: { x: 1, y: 0.7 },
        colors: ['#FFE45C', '#38E5D1', '#FF62C7', '#A72CFF', '#C9BDF5'],
      });

      if (Date.now() < end) {
        requestAnimationFrame(frame);
      }
    };
    frame();
  }, []);

  const effectivePlayers = players || [];

  // Merge all possible sources so no player is omitted and the highest score is preserved
  const playerMap = new Map();
  const allCandidates = [
    ...(gameSummary?.fullRankings || []),
    ...(gameSummary?.finalPodium || []),
    ...effectivePlayers,
    ...(player ? [player] : []),
  ];

  for (const p of allCandidates) {
    if (!p) continue;
    const key = p.playerId || p.nickname;
    if (!key) continue;
    const existing = playerMap.get(key);
    if (!existing) {
      playerMap.set(key, p);
    } else {
      playerMap.set(key, {
        ...existing,
        ...p,
        score: Math.max(
          Number(existing.score ?? existing.points ?? 0),
          Number(p.score ?? p.points ?? 0)
        ),
        correctAnswers: Math.max(
          Number(existing.correctAnswers ?? 0),
          Number(p.correctAnswers ?? 0)
        ),
        wrongAnswers: Math.max(
          Number(existing.wrongAnswers ?? 0),
          Number(p.wrongAnswers ?? 0)
        ),
      });
    }
  }

  const rawList = Array.from(playerMap.values());

  // Authoritative sort: Player with the HIGHEST POINTS is ALWAYS 1st Place Grand Champion!
  const standings = [...rawList]
    .map((p) => {
      const correct = Number(p.correctAnswers ?? 0);
      const wrong = Number(p.wrongAnswers ?? 0);
      const totalAns = p.totalAnswered ?? (correct + wrong);
      const acc = totalAns > 0
        ? Math.round((correct / totalAns) * 100)
        : (p.accuracyPercentage !== undefined ? p.accuracyPercentage : 0);
      const avgTime = p.avgResponseTimeMs
        ? Number(p.avgResponseTimeMs)
        : (p.cumulativeResponseTimeMs && correct ? Math.round(p.cumulativeResponseTimeMs / correct) : 2500);

      const scoreVal = Number(p.score ?? p.points ?? p.totalPoints ?? p.finalScore ?? 0);

      return {
        playerId: p.playerId,
        nickname: p.nickname || 'Fighter',
        score: scoreVal,
        avatar: p.avatar || 'terminal',
        correctAnswers: correct,
        wrongAnswers: wrong,
        totalAnswered: totalAns,
        accuracyPercentage: acc,
        avgResponseTimeMs: avgTime,
        streak: p.currentStreak || p.streak || 0,
      };
    })
    .sort((a, b) => {
      // 1. Total score descending (higher points ALWAYS wins!)
      if (b.score !== a.score) {
        return b.score - a.score;
      }
      // 2. Tie-break: Total correct answers descending
      if (b.correctAnswers !== a.correctAnswers) {
        return b.correctAnswers - a.correctAnswers;
      }
      // 3. Tie-break: Faster average response time ascending
      return a.avgResponseTimeMs - b.avgResponseTimeMs;
    })
    .map((p, idx) => ({
      ...p,
      rank: idx + 1, // #1 for highest points, #2 for second, etc.
    }));

  // Top 3 positions
  const firstPlace = standings[0] || null;
  const secondPlace = standings[1] || null;
  const thirdPlace = standings[2] || null;

  // Local player's performance entry
  const myStats =
    standings.find(
      (p) => p.playerId === player?.playerId || p.nickname === player?.nickname
    ) || firstPlace;

  const isCurrentWinner = player?.nickname && firstPlace?.nickname === player.nickname;

  // Match details
  const category = gameSummary?.categoryName || room?.config?.categoryName || 'PROGRAMMING';
  const difficulty = gameSummary?.difficulty || room?.config?.difficulty || 'MEDIUM';
  const gameMode = gameSummary?.gameMode || room?.config?.gameMode || 'CLASSIC BATTLE';
  const totalDurationSec = gameSummary?.totalDurationSec || 65;
  const durationMinutes = Math.floor(totalDurationSec / 60);
  const durationSeconds = totalDurationSec % 60;
  const durationText = `${durationMinutes > 0 ? `${durationMinutes}m ` : ''}${durationSeconds}s`;

  // Calculated stats for display
  const myTotalScore = myStats?.score ?? 0;
  const myCorrect = myStats?.correctAnswers ?? 0;
  const myWrong = myStats?.wrongAnswers ?? 0;
  const myAccuracy = myStats?.accuracyPercentage ?? 0;
  const myAvgTime = myStats?.avgResponseTimeMs
    ? (myStats.avgResponseTimeMs / 1000).toFixed(1)
    : '2.5';

  const handlePlayAgain = async () => {
    sfx.playClick();
    if (resetGame) resetGame();
    if (leaveRoom) await leaveRoom();
    navigate('/');
  };

  const handleReturnHome = async () => {
    sfx.playClick();
    if (resetGame) resetGame();
    if (leaveRoom) await leaveRoom();
    navigate('/');
  };

  return (
    <div className="min-h-[calc(100vh-80px)] bg-gradient-to-b from-[#26005C] via-[#160033] to-[#160033] py-10 px-4 sm:px-6 lg:px-8 select-none relative overflow-hidden">
      {/* Decorative Arcade Background Stars */}
      <div className="absolute top-8 left-10 animate-twinkle pointer-events-none">
        <PixelStar size={24} color="#FFE45C" />
      </div>
      <div className="absolute top-16 right-16 animate-twinkle pointer-events-none" style={{ animationDelay: '1s' }}>
        <PixelStar size={20} color="#38E5D1" />
      </div>
      <div className="absolute bottom-12 left-1/4 animate-twinkle pointer-events-none" style={{ animationDelay: '1.8s' }}>
        <PixelStar size={18} color="#FF62C7" />
      </div>

      <div className="max-w-4xl mx-auto space-y-8 relative z-10 text-center">
        {/* BANNER: MATCH CONCLUDED */}
        <div>
          <Badge variant="yellow" className="text-xs mb-3 animate-pulse">
            🏆 MATCH CONCLUDED • DISTRIBUTED RMI VERIFIED
          </Badge>
          <h1 className="font-pixel text-2xl sm:text-4xl md:text-5xl text-[#FFE45C] drop-shadow-[4px_4px_0px_#160033] mb-3 leading-tight">
            FINAL RESULTS & AWARDS
          </h1>
          <p className="font-arcade text-xs sm:text-base text-[#C9BDF5] tracking-widest uppercase">
            CONGRATULATIONS TO ALL FIGHTERS FOR AN EPIC CLASH!
          </p>
        </div>

        {/* MATCH CONFIG BADGES */}
        <div className="flex flex-wrap items-center justify-center gap-3">
          <Badge variant="purple" className="text-xs sm:text-sm">
            CATEGORY: {category}
          </Badge>
          <Badge variant="yellow" className="text-xs sm:text-sm">
            DIFFICULTY: {difficulty}
          </Badge>
          <Badge variant="cyan" className="text-xs sm:text-sm">
            MODE: {gameMode}
          </Badge>
          <div className="flex items-center gap-1.5 px-3 py-1 bg-[#160033] border-2 border-[#160033] text-[#F5EEFF] font-arcade text-xs font-bold shadow-[2px_2px_0px_#160033]">
            <Clock className="w-3.5 h-3.5 text-[#38E5D1]" />
            <span>DURATION: {durationText}</span>
          </div>
        </div>

        {/* WINNER SPOTLIGHT CELEBRATION CARD */}
        <div className="p-6 md:p-8 bg-[#C9BDF5] border-4 border-[#160033] shadow-[8px_8px_0px_#160033] text-[#160033] relative">
          <div className="flex flex-col items-center">
            {/* Grand Animated Pixel Trophy */}
            <div className="relative mb-3 animate-bounce">
              <PixelTrophy size={80} />
            </div>

            <span className="font-arcade text-xs md:text-sm font-black px-4 py-1 bg-[#FFE45C] border-2 border-[#160033] shadow-[3px_3px_0px_#160033] mb-2 uppercase">
              👑 1ST PLACE • GRAND CHAMPION
            </span>

            <div className="flex items-center gap-3 mb-1">
              <PixelAvatarSprite id={firstPlace?.avatar || 'terminal'} size={48} />
              <h2 className="font-pixel text-2xl sm:text-3xl md:text-4xl font-extrabold text-[#160033]">
                {firstPlace?.nickname || 'Champion'}
              </h2>
            </div>

            <p className="font-pixel text-lg sm:text-xl text-[#26005C] font-black mb-6">
              {firstPlace?.score || 0} POINTS
            </p>

            {/* Podium Announcement Cards */}
            <div className={`w-full grid gap-3 mb-6 ${standings.length === 2 ? 'grid-cols-1 sm:grid-cols-2 max-w-xl mx-auto' : 'grid-cols-1 sm:grid-cols-3'}`}>
              {/* 1st Place Card */}
              <div className="p-3 bg-[#FFE45C] border-3 border-[#160033] shadow-[3px_3px_0px_#160033] flex flex-col items-center">
                <span className="font-pixel text-xs font-black text-[#160033] mb-1">
                  🥇 1ST POSITION
                </span>
                <span className="font-arcade text-sm font-black text-[#160033] truncate max-w-full">
                  {firstPlace?.nickname || '---'}
                </span>
                <span className="font-pixel text-xs text-[#26005C] font-black mt-0.5">
                  {firstPlace?.score ?? 0} PTS
                </span>
              </div>

              {/* 2nd Place Card */}
              <div className="p-3 bg-[#94A3B8] border-3 border-[#160033] shadow-[3px_3px_0px_#160033] flex flex-col items-center">
                <span className="font-pixel text-xs font-black text-[#160033] mb-1">
                  🥈 2ND POSITION
                </span>
                <span className="font-arcade text-sm font-black text-[#160033] truncate max-w-full">
                  {secondPlace?.nickname || '---'}
                </span>
                <span className="font-pixel text-xs text-[#160033] font-black mt-0.5">
                  {secondPlace?.score ?? 0} PTS
                </span>
              </div>

              {/* 3rd Place Card (only if 3+ players) */}
              {standings.length >= 3 && (
                <div className="p-3 bg-[#D9824C] border-3 border-[#160033] shadow-[3px_3px_0px_#160033] flex flex-col items-center">
                  <span className="font-pixel text-xs font-black text-[#160033] mb-1">
                    🥉 3RD POSITION
                  </span>
                  <span className="font-arcade text-sm font-black text-[#160033] truncate max-w-full">
                    {thirdPlace?.nickname || '---'}
                  </span>
                  <span className="font-pixel text-xs text-[#160033] font-black mt-0.5">
                    {thirdPlace?.score ?? 0} PTS
                  </span>
                </div>
              )}
            </div>

            {/* YOUR PERFORMANCE METRICS GRID */}
            <div className="w-full border-t-3 border-[#160033] pt-5">
              <div className="flex items-center justify-between mb-3">
                <span className="font-pixel text-xs sm:text-sm text-[#160033] font-black flex items-center gap-1.5">
                  <Target className="w-4 h-4 text-[#A72CFF]" />
                  YOUR PERFORMANCE STATS ({player?.nickname || myStats?.nickname || 'FIGHTER'})
                </span>
                <span className="font-arcade text-xs text-[#26005C] font-bold">
                  RANK: #{myStats?.rank || 1} OF {standings.length}
                </span>
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-5 gap-2.5 text-center">
                {/* Total Score */}
                <div className="p-3 bg-[#F5EEFF] border-2 border-[#160033] shadow-[2px_2px_0px_#160033]">
                  <div className="text-[10px] font-arcade text-[#26005C] font-bold">TOTAL POINTS</div>
                  <div className="font-pixel text-sm sm:text-base text-[#160033] font-black mt-1">
                    {myTotalScore}
                  </div>
                </div>

                {/* Correct vs Incorrect */}
                <div className="p-3 bg-[#F5EEFF] border-2 border-[#160033] shadow-[2px_2px_0px_#160033]">
                  <div className="text-[10px] font-arcade text-[#26005C] font-bold">CORRECT / WRONG</div>
                  <div className="font-pixel text-xs sm:text-sm font-black mt-1 flex items-center justify-center gap-1.5">
                    <span className="text-[#16a34a]">✓ {myCorrect}</span>
                    <span className="text-[#160033]">|</span>
                    <span className="text-[#dc2626]">✗ {myWrong}</span>
                  </div>
                </div>

                {/* Accuracy */}
                <div className="p-3 bg-[#F5EEFF] border-2 border-[#160033] shadow-[2px_2px_0px_#160033]">
                  <div className="text-[10px] font-arcade text-[#26005C] font-bold">ACCURACY</div>
                  <div className="font-pixel text-sm sm:text-base text-[#0d9488] font-black mt-1">
                    {myAccuracy}%
                  </div>
                </div>

                {/* Average Response Time */}
                <div className="p-3 bg-[#F5EEFF] border-2 border-[#160033] shadow-[2px_2px_0px_#160033]">
                  <div className="text-[10px] font-arcade text-[#26005C] font-bold">AVG RESPONSE</div>
                  <div className="font-pixel text-sm sm:text-base text-[#7c3aed] font-black mt-1">
                    {myAvgTime}s
                  </div>
                </div>

                {/* Match Duration */}
                <div className="col-span-2 sm:col-span-1 p-3 bg-[#F5EEFF] border-2 border-[#160033] shadow-[2px_2px_0px_#160033]">
                  <div className="text-[10px] font-arcade text-[#26005C] font-bold">DURATION</div>
                  <div className="font-pixel text-sm sm:text-base text-[#160033] font-black mt-1">
                    {durationText}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* FINAL PODIUM & STANDINGS TABLE COMPONENT */}
        <BattleLeaderboard
          rankings={standings}
          isFinal={true}
          currentPlayerId={player?.playerId}
        />

        {/* ACTION BUTTONS: PLAY AGAIN OR RETURN HOME */}
        <div className="flex flex-wrap items-center justify-center gap-4 pt-2 pb-6">
          <Button
            variant="yellow"
            size="lg"
            onClick={handlePlayAgain}
            icon={<RotateCcw className="w-5 h-5 text-[#160033]" />}
          >
            PLAY AGAIN
          </Button>

          <Button
            variant="cyan"
            size="lg"
            onClick={handleReturnHome}
            icon={<Home className="w-5 h-5 text-[#160033]" />}
          >
            RETURN TO ARCADE
          </Button>
        </div>
      </div>
    </div>
  );
};
