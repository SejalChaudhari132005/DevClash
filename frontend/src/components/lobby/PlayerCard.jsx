import React from 'react';
import { Crown, Check, X, Wifi, UserX } from 'lucide-react';
import { PixelAvatarSprite } from '../pixel/PixelArtAssets';
import { sfx } from '../../utils/audio';

export const PlayerCard = ({
  player,
  isCurrentPlayer = false,
  isHostUser = false,
  onRemovePlayer = null,
}) => {
  const { playerId, nickname, avatar = 'terminal', ready, connected = true } = player;
  const isHost = Boolean(player.isHost || player.host);

  const handleKick = (e) => {
    e.stopPropagation();
    sfx.playWrong();
    if (onRemovePlayer) {
      onRemovePlayer(playerId);
    }
  };

  return (
    <div
      className={`relative p-4 border-4 transition-all select-none ${
        ready
          ? 'bg-[#26005C] border-[#38E5D1] shadow-[4px_4px_0px_#38E5D1]'
          : 'bg-[#160033] border-[#A72CFF] shadow-[4px_4px_0px_#160033]'
      } ${isCurrentPlayer ? 'ring-2 ring-[#FFE45C]' : ''}`}
    >
      {/* Top Badges (Host Crown, Fighter, Kick Button & Current Player) */}
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-2">
          {isHost ? (
            <span className="px-2 py-0.5 bg-[#FFE45C] text-[#160033] border-2 border-[#160033] font-arcade text-[10px] font-black flex items-center gap-1 shadow-[2px_2px_0px_#160033]">
              <Crown className="w-3 h-3 text-[#160033]" />
              HOST
            </span>
          ) : (
            <span className="px-2 py-0.5 bg-[#26005C] text-[#C9BDF5] border border-[#A72CFF] font-arcade text-[10px]">
              FIGHTER
            </span>
          )}

          {/* Host can remove other players */}
          {isHostUser && !isHost && onRemovePlayer && (
            <button
              type="button"
              onClick={handleKick}
              className="px-2 py-0.5 bg-[#FF62C7] hover:bg-[#FF85D6] text-[#160033] border-2 border-[#160033] font-arcade text-[10px] font-black flex items-center gap-1 cursor-pointer shadow-[2px_2px_0px_#160033] active:translate-y-0.5"
              title={`Remove ${nickname} from room`}
            >
              <UserX className="w-3 h-3 text-[#160033]" />
              KICK
            </button>
          )}
        </div>

        {isCurrentPlayer && (
          <span className="px-2 py-0.5 bg-[#FF62C7] text-[#160033] font-arcade text-[10px] font-black border border-[#160033]">
            YOU
          </span>
        )}
      </div>

      {/* Avatar & Player Name */}
      <div className="flex items-center gap-3 mb-4">
        <div className="relative">
          <PixelAvatarSprite id={avatar} size={54} />
          {/* Connection Dot */}
          <span
            className={`absolute -bottom-1 -right-1 w-3.5 h-3.5 rounded-full border-2 border-[#160033] ${
              connected ? 'bg-[#38E5D1]' : 'bg-[#FF62C7]'
            }`}
            title={connected ? 'Connected' : 'Disconnected'}
          />
        </div>
        <div className="min-w-0 flex-1">
          <div className="font-arcade text-base font-bold text-[#F5EEFF] truncate">
            {nickname}
          </div>
          <div className="text-[11px] text-[#C9BDF5] flex items-center gap-1 mt-0.5">
            <Wifi className={`w-3 h-3 ${connected ? 'text-[#38E5D1]' : 'text-[#FF62C7]'}`} />
            <span>{connected ? 'ONLINE' : 'DISCONNECTED'}</span>
          </div>
        </div>
      </div>

      {/* Ready Status Banner */}
      <div className="pt-2 border-t-2 border-[#160033]">
        {ready ? (
          <div className="py-1 px-2 bg-[#38E5D1] text-[#160033] border-2 border-[#160033] font-arcade text-xs font-black flex items-center justify-center gap-1.5 shadow-[2px_2px_0px_#160033]">
            <Check className="w-4 h-4 stroke-[3]" />
            <span>READY TO BATTLE</span>
          </div>
        ) : (
          <div className="py-1 px-2 bg-[#FF62C7] text-[#160033] border-2 border-[#160033] font-arcade text-xs font-black flex items-center justify-center gap-1.5 shadow-[2px_2px_0px_#160033]">
            <X className="w-4 h-4 stroke-[3]" />
            <span>NOT READY</span>
          </div>
        )}
      </div>
    </div>
  );
};
