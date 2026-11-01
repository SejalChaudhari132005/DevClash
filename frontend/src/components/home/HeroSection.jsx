import React, { useState } from 'react';
import { Play, Plus, KeyRound } from 'lucide-react';
import { Button } from '../common/Button';
import { Badge } from '../common/Badge';
import { PixelStar, PixelDotStar } from '../pixel/PixelArtAssets';
import { sfx } from '../../utils/audio';

// Rich constellation of retro 4-point stars across the arcade sky
const SKY_STARS = [
  { top: '4%', left: '8%', size: 16, delay: '0.2s', color: '#FFE45C' },
  { top: '7%', left: '20%', size: 12, delay: '1.4s', color: '#38E5D1' },
  { top: '12%', left: '32%', size: 22, delay: '0.6s', color: '#FFE45C' },
  { top: '5%', left: '46%', size: 10, delay: '2.1s', color: '#FFFFFF' },
  { top: '10%', left: '60%', size: 18, delay: '0.9s', color: '#FFE45C' },
  { top: '6%', left: '75%', size: 14, delay: '1.7s', color: '#FF62C7' },
  { top: '8%', left: '88%', size: 24, delay: '0.4s', color: '#FFE45C' },
  { top: '15%', left: '96%', size: 12, delay: '1.8s', color: '#38E5D1' },

  { top: '22%', left: '5%', size: 18, delay: '1.1s', color: '#38E5D1' },
  { top: '26%', left: '16%', size: 10, delay: '2.5s', color: '#FFFFFF' },
  { top: '24%', left: '84%', size: 16, delay: '0.8s', color: '#FFE45C' },
  { top: '30%', left: '93%', size: 12, delay: '1.9s', color: '#38E5D1' },

  { top: '42%', left: '3%', size: 20, delay: '1.3s', color: '#FFE45C' },
  { top: '48%', left: '12%', size: 10, delay: '0.5s', color: '#FF62C7' },
  { top: '44%', left: '87%', size: 22, delay: '1.6s', color: '#FFE45C' },
  { top: '52%', left: '95%', size: 12, delay: '2.2s', color: '#FFFFFF' },

  { top: '64%', left: '6%', size: 14, delay: '0.7s', color: '#38E5D1' },
  { top: '70%', left: '15%', size: 10, delay: '1.8s', color: '#FFE45C' },
  { top: '62%', left: '83%', size: 12, delay: '2.0s', color: '#FFFFFF' },
  { top: '72%', left: '92%', size: 18, delay: '1.2s', color: '#FFE45C' },

  { top: '80%', left: '22%', size: 12, delay: '0.9s', color: '#38E5D1' },
  { top: '84%', left: '74%', size: 14, delay: '1.5s', color: '#FF62C7' },
  { top: '82%', left: '48%', size: 16, delay: '2.4s', color: '#FFE45C' },
  { top: '88%', left: '8%', size: 10, delay: '1.0s', color: '#FFFFFF' },
  { top: '86%', left: '90%', size: 12, delay: '0.3s', color: '#38E5D1' },
];

export const HeroSection = ({ onStartPlaying, onCreateBattle, onJoinBattle }) => {
  const [quickRoomCode, setQuickRoomCode] = useState('');

  const handleQuickJoinSubmit = (e) => {
    e.preventDefault();
    if (!quickRoomCode.trim()) return;
    sfx.playClick();
    onJoinBattle(quickRoomCode.trim().toUpperCase());
  };

  return (
    <div className="relative overflow-hidden bg-gradient-to-b from-[#C9BDF5] via-[#D9B8F4] to-[#26005C] pt-8 md:pt-16 pb-0 border-b-8 border-[#160033]">
      {/* Dynamic Field of Twinkling Pixel Stars Across the Night Sky */}
      {SKY_STARS.map((star, idx) => (
        <div
          key={`star-${idx}`}
          className="absolute animate-twinkle pointer-events-none select-none"
          style={{
            top: star.top,
            left: star.left,
            animationDelay: star.delay,
          }}
        >
          {star.size >= 14 ? (
            <PixelStar size={star.size} color={star.color} />
          ) : (
            <PixelDotStar size={star.size} color={star.color} />
          )}
        </div>
      ))}

      {/* Top Banner Tag (#TEAMDEVCLASH / DATE style from reference) */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mb-6 relative z-10 flex items-center justify-between">
        <Badge variant="dark" className="text-xs">
          #TEAMDEVCLASH • ARCADE V1.0
        </Badge>
        <Badge variant="yellow" className="text-xs">
          JAVA RMI MULTIPLAYER
        </Badge>
      </div>

      {/* Main Title Area */}
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 text-center relative z-10">
        {/* Pill Subtitle */}
        <div className="inline-flex items-center gap-2 mb-4">
          <span className="pixel-pill bg-[#FFE45C] text-[#160033] border-3 border-[#160033] text-xs md:text-sm px-4 py-1.5 shadow-[3px_3px_0px_#160033]">
            ★ MULTIPLAYER TECH QUIZ BATTLE ★
          </span>
        </div>

        {/* Big Pixel DevClash Title */}
        <h1 className="font-pixel text-4xl sm:text-5xl md:text-7xl font-extrabold text-[#160033] tracking-tight leading-none drop-shadow-[4px_4px_0px_#FFE45C] mb-4">
          DEVCLASH
        </h1>

        {/* Tagline */}
        <div className="font-arcade text-base sm:text-xl md:text-2xl text-[#160033] font-bold tracking-widest uppercase mb-4">
          CODE YOUR SKILLS. CLASH YOUR KNOWLEDGE.
        </div>

        {/* Short Description */}
        <p className="max-w-2xl mx-auto text-sm sm:text-base md:text-lg text-[#26005C] font-semibold mb-8">
          Enter the arena. Challenge your friends in real-time. Answer computer science & programming questions, rack up combo streaks, and win glory on the podium!
        </p>

        {/* CTA Buttons Row */}
        <div className="flex flex-wrap items-center justify-center gap-4 mb-10">
          <Button
            variant="yellow"
            size="lg"
            onClick={onStartPlaying}
            icon={<Play className="w-5 h-5 fill-[#160033]" />}
            className="text-base"
          >
            START PLAYING
          </Button>

          <Button
            variant="cyan"
            size="lg"
            onClick={onCreateBattle}
            icon={<Plus className="w-5 h-5" />}
            className="text-base"
          >
            CREATE A BATTLE
          </Button>
        </div>

        {/* Quick Room Code Box (Retro Dialog Box) */}
        <div className="max-w-md mx-auto mb-12">
          <form
            onSubmit={handleQuickJoinSubmit}
            className="p-3 bg-[#C9BDF5] border-3 border-[#160033] shadow-[4px_4px_0px_#160033] flex items-center gap-2"
          >
            <div className="pl-2 flex items-center gap-2 text-[#160033]">
              <KeyRound className="w-5 h-5" />
              <span className="font-arcade text-xs font-bold whitespace-nowrap">ROOM CODE:</span>
            </div>
            <input
              type="text"
              maxLength={6}
              value={quickRoomCode}
              onChange={(e) => setQuickRoomCode(e.target.value.toUpperCase())}
              placeholder="e.g. 7YLFPU"
              className="flex-1 min-w-0 bg-[#F5EEFF] border-2 border-[#160033] px-3 py-1.5 font-pixel text-xs text-[#160033] uppercase placeholder:normal-case placeholder:text-slate-400 focus:outline-none focus:bg-[#FFE45C]"
            />
            <Button
              type="submit"
              variant="pink"
              size="sm"
              disabled={!quickRoomCode.trim()}
            >
              JOIN ↵
            </Button>
          </form>
        </div>
      </div>

      {/* Decorative Pixel Pavement Platform at the Bottom */}
      <div className="w-full h-8 sm:h-10 pixel-pavement mt-4" />
    </div>
  );
};
