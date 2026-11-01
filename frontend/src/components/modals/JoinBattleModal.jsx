import React, { useState, useRef, useEffect } from 'react';
import { X, KeyRound, AlertTriangle, CheckCircle2 } from 'lucide-react';
import { Button } from '../common/Button';
import { Badge } from '../common/Badge';
import { PixelAvatarSprite, AVAILABLE_AVATARS } from '../pixel/PixelArtAssets';
import { sfx } from '../../utils/audio';

export const JoinBattleModal = ({
  isOpen,
  onClose,
  onJoinRoom,
  prefilledCode = '',
  errorMessage = null,
  isConnected = true,
}) => {
  const [nickname, setNickname] = useState('Challenger');
  const [selectedAvatar, setSelectedAvatar] = useState('wizard');
  const [digits, setDigits] = useState(['', '', '', '', '', '']);
  const [localError, setLocalError] = useState(null);

  const inputRefs = useRef([]);

  useEffect(() => {
    if (prefilledCode && prefilledCode.length === 6) {
      setDigits(prefilledCode.toUpperCase().split(''));
    }
  }, [prefilledCode]);

  useEffect(() => {
    if (isOpen && inputRefs.current[0]) {
      setTimeout(() => inputRefs.current[0]?.focus(), 100);
    }
    setLocalError(errorMessage);
  }, [isOpen, errorMessage]);

  if (!isOpen) return null;

  const handleDigitChange = (index, value) => {
    setLocalError(null);
    const char = value.slice(-1).toUpperCase();
    if (!/^[A-Z0-9]$/.test(char) && char !== '') return;

    sfx.playClick();
    const newDigits = [...digits];
    newDigits[index] = char;
    setDigits(newDigits);

    // Auto-advance to next box if char entered
    if (char && index < 5) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  const handleKeyDown = (index, e) => {
    if (e.key === 'Backspace' && !digits[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  };

  const handlePaste = (e) => {
    e.preventDefault();
    const paste = e.clipboardData.getData('text').trim().toUpperCase().replace(/[^A-Z0-9]/g, '');
    if (paste.length >= 6) {
      sfx.playClick();
      setDigits(paste.slice(0, 6).split(''));
      inputRefs.current[5]?.focus();
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const fullCode = digits.join('').trim().toUpperCase();

    if (!nickname.trim()) {
      setLocalError('Please enter a player nickname.');
      return;
    }
    if (fullCode.length !== 6) {
      setLocalError('Please enter all 6 digits of the room code.');
      sfx.playWrong();
      return;
    }

    sfx.playClick();
    onJoinRoom(fullCode, nickname.trim(), selectedAvatar);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-[#160033]/85 backdrop-blur-xs select-none">
      <div className="relative w-full max-w-lg bg-[#26005C] border-4 border-[#160033] shadow-[8px_8px_0px_#160033]">
        {/* Header */}
        <div className="px-4 py-3 bg-[#38E5D1] border-b-4 border-[#160033] flex items-center justify-between">
          <div className="flex items-center gap-2">
            <KeyRound className="w-5 h-5 text-[#160033]" />
            <span className="font-pixel text-sm md:text-base text-[#160033]">
              JOIN BATTLE ARENA
            </span>
          </div>
          <button
            onClick={() => {
              sfx.playClick();
              onClose();
            }}
            className="w-7 h-7 bg-[#FF62C7] border-2 border-[#160033] flex items-center justify-center text-[#160033] font-black hover:bg-[#FF85D6] cursor-pointer"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Modal Form */}
        <form onSubmit={handleSubmit} className="p-4 md:p-6 space-y-6">
          {/* Connection Status Badge */}
          <div className="flex items-center justify-between">
            <span className="font-arcade text-xs text-[#C9BDF5]">SYSTEM STATUS:</span>
            <Badge
              variant={isConnected ? 'cyan' : 'yellow'}
              className="text-[10px]"
            >
              {isConnected ? 'GATEWAY CONNECTED' : 'WAITING FOR GATEWAY'}
            </Badge>
          </div>

          {/* Nickname & Avatar Selection */}
          <div className="p-4 bg-[#160033] border-3 border-[#A72CFF] shadow-[3px_3px_0px_#160033]">
            <label className="block font-arcade text-xs text-[#FFE45C] font-bold mb-2">
              PLAYER NICKNAME
            </label>
            <div className="flex items-center gap-3 mb-3">
              <PixelAvatarSprite id={selectedAvatar} size={48} />
              <input
                type="text"
                maxLength={15}
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                placeholder="Enter nickname..."
                className="flex-1 bg-[#F5EEFF] border-3 border-[#160033] px-3 py-2 font-arcade text-sm text-[#160033] font-bold focus:outline-none focus:bg-[#FFE45C]"
                required
              />
            </div>

            {/* Avatar Row */}
            <div className="flex items-center justify-between gap-1.5">
              {AVAILABLE_AVATARS.map((av) => (
                <button
                  type="button"
                  key={av.id}
                  onClick={() => {
                    sfx.playClick();
                    setSelectedAvatar(av.id);
                  }}
                  className={`p-1 border-2 transition-all cursor-pointer ${
                    selectedAvatar === av.id
                      ? 'border-[#FFE45C] bg-[#FFE45C]/20 scale-110'
                      : 'border-transparent opacity-60 hover:opacity-100'
                  }`}
                  title={av.name}
                >
                  <PixelAvatarSprite id={av.id} size={28} />
                </button>
              ))}
            </div>
          </div>

          {/* Prominent 6-Digit Room Code Input Boxes */}
          <div>
            <label className="block font-arcade text-xs text-[#FFE45C] uppercase font-bold mb-3 text-center">
              ENTER 6-DIGIT ROOM CODE
            </label>

            <div className="flex justify-center items-center gap-2 sm:gap-3" onPaste={handlePaste}>
              {digits.map((digit, idx) => (
                <input
                  key={idx}
                  ref={(el) => (inputRefs.current[idx] = el)}
                  type="text"
                  maxLength={1}
                  value={digit}
                  onChange={(e) => handleDigitChange(idx, e.target.value)}
                  onKeyDown={(e) => handleKeyDown(idx, e.key)}
                  className="w-11 h-14 sm:w-14 sm:h-16 bg-[#F5EEFF] border-4 border-[#160033] shadow-[4px_4px_0px_#160033] text-center font-pixel text-lg sm:text-2xl text-[#160033] uppercase font-black focus:outline-none focus:bg-[#FFE45C] focus:-translate-y-1 transition-all"
                />
              ))}
            </div>
          </div>

          {/* Error Message Box */}
          {localError && (
            <div className="p-3 bg-[#FF62C7] border-3 border-[#160033] shadow-[3px_3px_0px_#160033] text-[#160033] flex items-center gap-2">
              <AlertTriangle className="w-5 h-5 shrink-0" />
              <span className="font-arcade text-xs font-bold leading-tight">
                {localError}
              </span>
            </div>
          )}

          {/* Action Buttons */}
          <div className="pt-2 border-t-3 border-[#160033] flex items-center justify-end gap-3">
            <Button
              type="button"
              variant="dark"
              onClick={onClose}
              size="md"
            >
              CANCEL
            </Button>
            <Button
              type="submit"
              variant="yellow"
              size="lg"
              disabled={digits.join('').length !== 6}
            >
              JOIN BATTLE
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};
