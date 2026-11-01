import React from 'react';
import { Trophy, Flame, ArrowUp, ArrowDown, Minus, Target, CheckCircle2, Award } from 'lucide-react';
import { PixelAvatarSprite, PixelTrophy, PixelStar } from '../pixel/PixelArtAssets';
import { Badge } from '../common/Badge';
import { useGame } from '../../context/GameContext';

export const BattleLeaderboard = ({ rankings = [], isFinal = false, currentPlayerId = null }) => {
  const { player } = useGame();
  const currentId = currentPlayerId || player?.playerId;

  // The rankings from RMI Server are already authoritatively sorted by:
  // 1. Score (descending)
  // 2. Correct answers (descending)
  // 3. Cumulative answer time (ascending)
  // Authoritative sort: Player with the HIGHEST POINTS is ALWAYS Ranked #1!
  const sorted = [...rankings]
    .map((p) => {
      const score = Number(p.score ?? p.points ?? p.totalPoints ?? p.finalScore ?? 0);
      const correct = Number(p.correctAnswers ?? 0);
      const wrong = Number(p.wrongAnswers ?? 0);
      const totalAns = p.totalAnswered ?? (correct + wrong);
      const acc = totalAns > 0 ? Math.round((correct / totalAns) * 100) : (p.accuracyPercentage ?? 0);
      return {
        ...p,
        score,
        correctAnswers: correct,
        wrongAnswers: wrong,
        totalAnswered: totalAns,
        accuracyPercentage: acc,
      };
    })
    .sort((a, b) => {
      // 1. Total score/points descending (HIGHER POINTS ALWAYS RANKS FIRST)
      if (b.score !== a.score) {
        return b.score - a.score;
      }
      // 2. Tie-break: Total correct answers descending
      if (b.correctAnswers !== a.correctAnswers) {
        return b.correctAnswers - a.correctAnswers;
      }
      // 3. Tie-break: Faster response time
      const timeA = Number(a.avgResponseTimeMs || a.cumulativeAnswerTimeMs || 0);
      const timeB = Number(b.avgResponseTimeMs || b.cumulativeAnswerTimeMs || 0);
      return timeA - timeB;
    })
    .map((item, idx) => ({
      ...item,
      rank: idx + 1, // Authoritatively assign rank 1, 2, 3...
    }));

  const top1 = sorted[0];
  const top2 = sorted[1];
  const top3 = sorted[2];

  return (
    <div className="w-full bg-[#26005C] border-4 border-[#160033] shadow-[8px_8px_0px_#160033] p-4 sm:p-6 select-none relative overflow-hidden">
      {/* Arcade Header */}
      <div className="flex flex-wrap items-center justify-between pb-3 mb-5 border-b-4 border-[#160033] gap-2">
        <div className="flex items-center gap-2.5">
          <div className="p-1.5 bg-[#FFE45C] border-2 border-[#160033] shadow-[2px_2px_0px_#160033]">
            <Trophy className="w-5 h-5 text-[#160033]" />
          </div>
          <div>
            <h2 className="font-pixel text-sm sm:text-lg text-[#FFE45C] tracking-wider drop-shadow-[2px_2px_0px_#160033]">
              {isFinal ? 'FINAL CHAMPIONSHIP STANDINGS' : 'LIVE BATTLE LEADERBOARD'}
            </h2>
            <p className="font-arcade text-[10px] sm:text-xs text-[#C9BDF5] tracking-widest uppercase">
              {isFinal ? 'OFFICIAL VERIFIED RESULTS' : 'UPDATED AFTER EVERY QUESTION • REAL-TIME RMI SYNC'}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {isFinal ? (
            <Badge variant="yellow" className="text-[10px] animate-pulse">
              🏆 PODIUM COMPLETE
            </Badge>
          ) : (
            <Badge variant="cyan" className="text-[10px]">
              ⚡ LIVE STAGE
            </Badge>
          )}
        </div>
      </div>

      {/* TOP 3 ANIMATED ARCADE PODIUM */}
      <div className="grid grid-cols-3 gap-2 sm:gap-4 items-end mb-6 pt-3 px-1 sm:px-4">
        {/* 2nd Place (Silver Pedestal - Left) */}
        <div className="flex flex-col items-center text-center">
          {top2 ? (
            <div className="w-full flex flex-col items-center">
              <div className="relative mb-2 transition-transform hover:scale-110">
                <PixelAvatarSprite id={top2.avatar || 'wizard'} size={44} />
                <div className="absolute -top-2 -right-2 px-1.5 py-0.2 bg-[#94A3B8] border border-[#160033] text-[#160033] font-pixel text-[9px] font-black shadow-[1px_1px_0px_#160033]">
                  #2
                </div>
              </div>

              <div className="font-arcade text-xs font-black text-[#F5EEFF] truncate max-w-full px-1">
                {top2.nickname}
                {top2.playerId === currentId && (
                  <span className="ml-1 text-[8px] text-[#FFE45C]">(YOU)</span>
                )}
              </div>

              <div className="font-pixel text-[11px] sm:text-xs text-[#38E5D1] font-black my-1">
                {top2.score || 0} PTS
              </div>

              {top2.accuracyPercentage !== undefined && (
                <div className="text-[9px] font-arcade text-[#C9BDF5] font-bold mb-1">
                  ✓ {top2.correctAnswers ?? 0} ({top2.accuracyPercentage ?? 0}%)
                </div>
              )}

              {/* Pedestal Block */}
              <div className="w-full h-20 sm:h-24 bg-gradient-to-t from-[#64748B] to-[#94A3B8] border-3 border-[#160033] flex flex-col items-center justify-center shadow-[4px_4px_0px_#160033] relative">
                <span className="font-pixel text-base sm:text-xl font-black text-[#160033]">2ND</span>
                <span className="text-[9px] sm:text-[10px] font-arcade text-[#160033] font-black tracking-widest">
                  SILVER
                </span>
              </div>
            </div>
          ) : (
            <div className="w-full h-20 sm:h-24 bg-[#160033]/60 border-2 border-dashed border-[#A72CFF]/40 flex items-center justify-center">
              <span className="font-pixel text-[9px] text-[#C9BDF5]/40">EMPTY</span>
            </div>
          )}
        </div>

        {/* 1st Place (Gold Champion Pedestal - Center) */}
        <div className="flex flex-col items-center text-center -mt-4">
          {top1 ? (
            <div className="w-full flex flex-col items-center">
              {/* Crown / Trophy Floating Animation */}
              <div className="relative mb-2 transition-transform hover:scale-110">
                <div className="animate-bounce">
                  <PixelAvatarSprite id={top1.avatar || 'terminal'} size={56} />
                </div>
                <div className="absolute -top-3.5 -right-3">
                  <PixelTrophy size={30} />
                </div>
              </div>

              <div className="font-arcade text-xs sm:text-sm font-black text-[#FFE45C] truncate max-w-full px-1 flex items-center gap-1 justify-center">
                <span>{top1.nickname}</span>
                {top1.playerId === currentId && (
                  <span className="text-[8px] text-[#FFE45C] bg-[#160033] px-1 py-0.2 border border-[#FFE45C]">
                    YOU
                  </span>
                )}
              </div>

              <div className="font-pixel text-xs sm:text-sm text-[#FFE45C] font-black my-1 drop-shadow-[1px_1px_0px_#160033]">
                {top1.score || 0} PTS
              </div>

              {top1.accuracyPercentage !== undefined && (
                <div className="text-[9px] sm:text-[10px] font-arcade text-[#38E5D1] font-black mb-1">
                  ✓ {top1.correctAnswers ?? 0} ({top1.accuracyPercentage ?? 0}%)
                </div>
              )}

              {/* Champion Pedestal Block (Tallest) */}
              <div className="w-full h-28 sm:h-32 bg-gradient-to-t from-[#F59E0B] via-[#FBBF24] to-[#FFE45C] border-3 border-[#160033] flex flex-col items-center justify-center shadow-[6px_6px_0px_#160033] relative">
                <span className="font-pixel text-lg sm:text-2xl font-black text-[#160033]">1ST</span>
                <span className="text-[10px] sm:text-xs font-arcade text-[#160033] font-black tracking-widest">
                  CHAMPION
                </span>
                <div className="absolute -bottom-1 -right-1">
                  <PixelStar size={14} color="#160033" />
                </div>
              </div>
            </div>
          ) : (
            <div className="w-full h-28 sm:h-32 bg-[#160033]/60 border-2 border-dashed border-[#FFE45C]/40 flex items-center justify-center">
              <span className="font-pixel text-[9px] text-[#FFE45C]/40">EMPTY</span>
            </div>
          )}
        </div>

        {/* 3rd Place (Bronze Pedestal - Right) */}
        <div className="flex flex-col items-center text-center">
          {top3 ? (
            <div className="w-full flex flex-col items-center">
              <div className="relative mb-2 transition-transform hover:scale-110">
                <PixelAvatarSprite id={top3.avatar || 'knight'} size={40} />
                <div className="absolute -top-2 -right-2 px-1.5 py-0.2 bg-[#D9824C] border border-[#160033] text-[#160033] font-pixel text-[9px] font-black shadow-[1px_1px_0px_#160033]">
                  #3
                </div>
              </div>

              <div className="font-arcade text-xs font-black text-[#F5EEFF] truncate max-w-full px-1">
                {top3.nickname}
                {top3.playerId === currentId && (
                  <span className="ml-1 text-[8px] text-[#FFE45C]">(YOU)</span>
                )}
              </div>

              <div className="font-pixel text-[11px] sm:text-xs text-[#FF62C7] font-black my-1">
                {top3.score || 0} PTS
              </div>

              {top3.accuracyPercentage !== undefined && (
                <div className="text-[9px] font-arcade text-[#C9BDF5] font-bold mb-1">
                  ✓ {top3.correctAnswers ?? 0} ({top3.accuracyPercentage ?? 0}%)
                </div>
              )}

              {/* Pedestal Block */}
              <div className="w-full h-16 sm:h-20 bg-gradient-to-t from-[#B45309] to-[#D9824C] border-3 border-[#160033] flex flex-col items-center justify-center shadow-[4px_4px_0px_#160033] relative">
                <span className="font-pixel text-sm sm:text-lg font-black text-[#160033]">3RD</span>
                <span className="text-[9px] sm:text-[10px] font-arcade text-[#160033] font-black tracking-widest">
                  BRONZE
                </span>
              </div>
            </div>
          ) : (
            <div className="w-full h-16 sm:h-20 bg-[#160033]/60 border-2 border-dashed border-[#A72CFF]/40 flex items-center justify-center">
              <span className="font-pixel text-[9px] text-[#C9BDF5]/40">EMPTY</span>
            </div>
          )}
        </div>
      </div>

      {/* FULL STANDINGS TABLE WITH RANK MOVEMENT & ACCURACY */}
      <div className="bg-[#160033] border-3 border-[#160033] shadow-[4px_4px_0px_#160033] overflow-hidden">
        {/* Table Header */}
        <div className="px-3 py-2.5 bg-[#A72CFF] border-b-2 border-[#160033] font-arcade text-[10px] sm:text-xs font-black text-[#F5EEFF] grid grid-cols-12 gap-2 tracking-wider">
          <span className="col-span-2 text-left">RANK</span>
          <span className="col-span-5 sm:col-span-4 text-left">FIGHTER</span>
          <span className="col-span-3 sm:col-span-3 text-center sm:text-left">ACCURACY</span>
          <span className="hidden sm:inline sm:col-span-1 text-center">STREAK</span>
          <span className="col-span-2 text-right">POINTS</span>
        </div>

        {/* Player Rows */}
        <div className="divide-y-2 divide-[#26005C] font-arcade text-xs">
          {sorted.map((item, idx) => {
            const rank = item.rank || idx + 1;
            const isMe = item.playerId === currentId || item.nickname === player?.nickname;
            const rankDelta = item.rankChange !== undefined ? item.rankChange : 0;
            const correctCount = item.correctAnswers ?? 0;
            const totalCount = item.totalAnswered ?? (item.wrongAnswers !== undefined ? correctCount + item.wrongAnswers : 0);
            const accuracy = item.accuracyPercentage !== undefined
              ? item.accuracyPercentage
              : totalCount > 0 ? Math.round((correctCount / totalCount) * 100) : 0;

            return (
              <div
                key={item.playerId || idx}
                className={`px-3 py-2.5 grid grid-cols-12 gap-2 items-center transition-all ${
                  isMe
                    ? 'bg-[#FFE45C]/15 border-l-4 border-l-[#FFE45C] text-[#FFE45C] font-black'
                    : idx === 0
                    ? 'bg-[#FFE45C]/5 text-[#F5EEFF]'
                    : 'text-[#F5EEFF] hover:bg-[#26005C]/40'
                }`}
              >
                {/* RANK + MOVEMENT INDICATOR */}
                <div className="col-span-2 flex items-center gap-1.5">
                  <span className={`font-pixel text-[11px] sm:text-xs font-black ${
                    rank === 1 ? 'text-[#FFE45C]' : rank === 2 ? 'text-[#94A3B8]' : rank === 3 ? 'text-[#D9824C]' : 'text-[#C9BDF5]'
                  }`}>
                    #{rank}
                  </span>

                  {/* Rank Delta Highlight Badge */}
                  {rankDelta > 0 ? (
                    <span
                      title={`Moved up ${rankDelta} spots`}
                      className="px-1 py-0.2 bg-[#22c55e]/25 border border-[#22c55e] text-[#22c55e] font-pixel text-[8px] flex items-center"
                    >
                      <ArrowUp className="w-2.5 h-2.5" />+{rankDelta}
                    </span>
                  ) : rankDelta < 0 ? (
                    <span
                      title={`Moved down ${Math.abs(rankDelta)} spots`}
                      className="px-1 py-0.2 bg-[#f43f5e]/25 border border-[#f43f5e] text-[#f43f5e] font-pixel text-[8px] flex items-center"
                    >
                      <ArrowDown className="w-2.5 h-2.5" />{rankDelta}
                    </span>
                  ) : (
                    <span
                      title="Ranking unchanged"
                      className="text-[#C9BDF5]/40 font-pixel text-[8px]"
                    >
                      <Minus className="w-2 h-2 inline" />
                    </span>
                  )}
                </div>

                {/* FIGHTER INFO */}
                <div className="col-span-5 sm:col-span-4 flex items-center gap-2 truncate">
                  <div className="shrink-0">
                    <PixelAvatarSprite id={item.avatar || 'terminal'} size={24} />
                  </div>
                  <div className="truncate flex items-center gap-1">
                    <span className="font-bold truncate text-xs sm:text-sm">
                      {item.nickname}
                    </span>
                    {isMe && (
                      <span className="shrink-0 px-1 py-0.2 bg-[#FFE45C] text-[#160033] font-pixel text-[8px] font-black border border-[#160033]">
                        YOU
                      </span>
                    )}
                  </div>
                </div>

                {/* ACCURACY & CORRECT RATIO */}
                <div className="col-span-3 sm:col-span-3 flex flex-col justify-center">
                  <div className="flex items-center gap-1 text-[10px] sm:text-xs font-arcade">
                    <span className="text-[#38E5D1] font-bold">
                      ✓ {correctCount}{totalCount > 0 ? `/${totalCount}` : ''}
                    </span>
                    <span className="text-[#C9BDF5] text-[9px] sm:text-[10px]">
                      ({accuracy}%)
                    </span>
                  </div>
                  {/* Micro Progress Bar */}
                  <div className="w-full max-w-[90px] h-1.5 bg-[#26005C] border border-[#160033] mt-0.5 overflow-hidden">
                    <div
                      className="h-full bg-[#38E5D1]"
                      style={{ width: `${Math.min(100, Math.max(0, accuracy))}%` }}
                    />
                  </div>
                </div>

                {/* STREAK */}
                <div className="hidden sm:flex sm:col-span-1 items-center justify-center gap-0.5 text-[#FF62C7] font-bold text-xs">
                  <Flame className="w-3.5 h-3.5" />
                  <span>{item.streak || item.currentStreak || 0}x</span>
                </div>

                {/* TOTAL POINTS */}
                <div className="col-span-2 text-right">
                  <span className="font-pixel text-[11px] sm:text-xs font-black text-[#FFE45C]">
                    {item.score || 0}
                  </span>
                  <span className="hidden sm:inline text-[9px] text-[#C9BDF5] ml-1">PTS</span>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Tie-Breaker Logic Note */}
      <div className="mt-3 text-center">
        <p className="font-arcade text-[9px] text-[#C9BDF5]/60 tracking-wider">
          ⚖️ TIE-BREAKER: EQUAL SCORES RESOLVED BY TOTAL CORRECT ANSWERS ➔ CUMULATIVE RESPONSE TIME
        </p>
      </div>
    </div>
  );
};
