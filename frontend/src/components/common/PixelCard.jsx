import React from 'react';

export const PixelCard = ({
  children,
  variant = 'purple', // lavender, purple, dark, yellow
  className = '',
  border = 'default', // sm, default, lg
  title = null,
  headerAction = null,
  ...props
}) => {
  const variants = {
    lavender: 'pixel-card-lavender',
    purple: 'pixel-card-purple',
    dark: 'pixel-card-dark',
    yellow: 'pixel-card-yellow',
  };

  const borders = {
    sm: 'pixel-border-sm',
    default: 'pixel-border',
    lg: 'pixel-border-lg',
  };

  return (
    <div
      className={`${variants[variant] || variants.purple} ${borders[border]} p-4 md:p-6 relative select-none ${className}`}
      {...props}
    >
      {title && (
        <div className="flex items-center justify-between pb-3 mb-4 border-b-2 border-[#160033]/20">
          <h3 className="font-arcade text-base md:text-lg font-bold tracking-wider">{title}</h3>
          {headerAction}
        </div>
      )}
      {children}
    </div>
  );
};

// Retro Windows / Terminal Dialog Box with Title Bar and Window Controls
export const PixelWindow = ({
  title,
  children,
  className = '',
  accentColor = '#C9BDF5',
}) => {
  return (
    <div className={`border-4 border-[#160033] shadow-[6px_6px_0px_#160033] bg-[#26005C] ${className}`}>
      {/* Title Bar */}
      <div
        className="px-3 py-2 flex items-center justify-between border-b-4 border-[#160033]"
        style={{ backgroundColor: accentColor }}
      >
        <div className="flex items-center gap-2">
          <span className="inline-block w-3 h-3 bg-[#160033]" />
          <span className="font-arcade text-xs md:text-sm font-black text-[#160033] uppercase">
            {title}
          </span>
        </div>
        {/* Retro Window Minimize / Close buttons */}
        <div className="flex items-center gap-1.5">
          <span className="w-4 h-4 bg-[#FFE45C] border-2 border-[#160033] flex items-center justify-center text-[9px] font-bold text-[#160033] leading-none">
            _
          </span>
          <span className="w-4 h-4 bg-[#FF62C7] border-2 border-[#160033] flex items-center justify-center text-[9px] font-bold text-[#160033] leading-none">
            ×
          </span>
        </div>
      </div>
      {/* Window Body */}
      <div className="p-4 md:p-6 bg-[#160033] text-[#F5EEFF]">
        {children}
      </div>
    </div>
  );
};
