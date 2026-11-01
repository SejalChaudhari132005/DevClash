import React from 'react';
import { Check, X, Zap } from 'lucide-react';
import { sfx } from '../../utils/audio';

const OPTION_COLORS = {
  A: { bg: '#FFE45C', text: '#160033' },
  B: { bg: '#38E5D1', text: '#160033' },
  C: { bg: '#FF62C7', text: '#160033' },
  D: { bg: '#A72CFF', text: '#F5EEFF' },
};

export const AnswerOption = ({
  letter,
  text,
  isSelected,
  isCorrect,
  isLocked,
  isRoundEnded,
  onClick,
}) => {
  const color = OPTION_COLORS[letter] || OPTION_COLORS.A;

  const handleClick = () => {
    if (isLocked) return;
    sfx.playClick();
    onClick(letter);
  };

  // State styling calculations
  let cardBg = 'bg-[#26005C] hover:bg-[#3B077E] border-[#160033]';
  let badgeContent = letter;
  let showLightning = false;

  if (isRoundEnded) {
    if (isCorrect) {
      cardBg = 'bg-[#38E5D1] border-[#160033] text-[#160033] shadow-[4px_4px_0px_#160033] scale-102';
      badgeContent = <Check className="w-5 h-5 stroke-[3] text-[#160033]" />;
      showLightning = true;
    } else if (isSelected && !isCorrect) {
      cardBg = 'bg-[#FF62C7] border-[#160033] text-[#160033] shadow-[4px_4px_0px_#160033]';
      badgeContent = <X className="w-5 h-5 stroke-[3] text-[#160033]" />;
    } else {
      cardBg = 'bg-[#160033]/60 border-[#26005C] text-[#C9BDF5]/50 opacity-60';
    }
  } else if (isSelected) {
    cardBg = 'bg-[#FFE45C] border-[#160033] text-[#160033] shadow-[4px_4px_0px_#160033] scale-102 ring-2 ring-[#FFE45C]';
  }

  return (
    <button
      type="button"
      onClick={handleClick}
      disabled={isLocked}
      className={`relative w-full p-4 md:p-5 border-4 text-left font-arcade transition-all cursor-pointer flex items-center gap-4 select-none ${cardBg} ${
        !isLocked ? 'hover:-translate-y-1' : ''
      }`}
    >
      {/* Option Letter Box */}
      <div
        className="w-10 h-10 shrink-0 border-3 border-[#160033] shadow-[2px_2px_0px_#160033] flex items-center justify-center font-pixel text-sm font-bold"
        style={{
          backgroundColor: isRoundEnded && isCorrect ? '#FFE45C' : color.bg,
          color: color.text,
        }}
      >
        {badgeContent}
      </div>

      {/* Option Text */}
      <span
        className={`flex-1 text-sm md:text-base font-bold leading-snug ${
          isRoundEnded && (isCorrect || isSelected) ? 'text-[#160033]' : isSelected ? 'text-[#160033]' : 'text-[#F5EEFF]'
        }`}
      >
        {text}
      </span>

      {/* Lightning Effect on Correct Answer */}
      {showLightning && (
        <div className="absolute -top-3 -right-3 animate-bounce">
          <div className="p-1.5 bg-[#FFE45C] border-2 border-[#160033] shadow-[2px_2px_0px_#160033]">
            <Zap className="w-5 h-5 fill-[#FFE45C] text-[#160033]" />
          </div>
        </div>
      )}
    </button>
  );
};
