import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Volume2, VolumeX, Swords, Plus, KeyRound } from 'lucide-react';
import { sfx } from '../../utils/audio';
import { Button } from './Button';
import { Badge } from './Badge';

export const Navbar = ({ isConnected, onOpenCreate, onOpenJoin }) => {
  const [muted, setMuted] = useState(false);
  const navigate = useNavigate();

  const handleToggleSound = () => {
    const isNowMuted = sfx.toggleMute();
    setMuted(isNowMuted);
    if (!isNowMuted) {
      sfx.playClick();
    }
  };

  return (
    <header className="sticky top-0 z-50 bg-[#160033] border-b-4 border-[#160033] shadow-[0_4px_0_#26005C]">
      {/* Top Colorful Arcade Pixel Strip */}
      <div className="pixel-strip" />

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-18 flex items-center justify-between">
        {/* Brand Logo & Arcade Title */}
        <Link
          to="/"
          className="flex items-center gap-3 group"
          onClick={() => sfx.playClick()}
        >
          <div className="w-10 h-10 bg-[#FFE45C] border-3 border-[#160033] shadow-[3px_3px_0px_#160033] flex items-center justify-center transform group-hover:rotate-6 transition-transform">
            <Swords className="w-6 h-6 text-[#160033]" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-pixel text-lg md:text-xl text-[#F5EEFF] tracking-wider group-hover:text-[#FFE45C] transition-colors">
                DEVCLASH
              </span>
              <span className="hidden sm:inline-block px-1.5 py-0.5 bg-[#FF62C7] text-[#160033] font-arcade text-[10px] font-black border border-[#160033] animate-pulse">
                ARCADE
              </span>
            </div>
            <p className="font-arcade text-[10px] text-[#C9BDF5] tracking-widest hidden md:block">
              CODE YOUR SKILLS. CLASH YOUR KNOWLEDGE.
            </p>
          </div>
        </Link>

        {/* Center / Right Controls & Quick Actions */}
        <div className="flex items-center gap-3">
          {/* Gateway RMI Status Badge */}
          <div className="hidden lg:flex items-center">
            <Badge
              variant={isConnected ? 'cyan' : 'yellow'}
              className="text-[11px]"
              icon={
                <span
                  className={`w-2 h-2 inline-block border border-[#160033] ${
                    isConnected ? 'bg-[#38E5D1] animate-ping' : 'bg-[#FF62C7]'
                  }`}
                />
              }
            >
              {isConnected ? 'RMI ONLINE: 1099' : 'GATEWAY CONNECTING'}
            </Badge>
          </div>

          {/* Sound FX Toggle Button */}
          <button
            onClick={handleToggleSound}
            className={`p-2 border-2 border-[#160033] shadow-[2px_2px_0px_#160033] transition-all cursor-pointer ${
              muted
                ? 'bg-[#26005C] text-[#C9BDF5]'
                : 'bg-[#FFE45C] text-[#160033] hover:bg-[#FFF099]'
            }`}
            title={muted ? 'Unmute Arcade Sound FX' : 'Mute Sound FX'}
          >
            {muted ? <VolumeX className="w-4 h-4" /> : <Volume2 className="w-4 h-4" />}
          </button>

          {/* Quick Join Battle Button */}
          {onOpenJoin && (
            <Button
              variant="purple"
              size="sm"
              onClick={onOpenJoin}
              icon={<KeyRound className="w-3.5 h-3.5" />}
              className="hidden sm:inline-flex"
            >
              JOIN ROOM
            </Button>
          )}

          {/* Quick Create Battle Button */}
          {onOpenCreate && (
            <Button
              variant="yellow"
              size="sm"
              onClick={onOpenCreate}
              icon={<Plus className="w-3.5 h-3.5" />}
            >
              CREATE
            </Button>
          )}
        </div>
      </div>
    </header>
  );
};
