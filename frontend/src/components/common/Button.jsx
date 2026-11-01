import React from 'react';
import { sfx } from '../../utils/audio';

export const Button = ({
  children,
  onClick,
  variant = 'yellow', // yellow, cyan, pink, purple, violet, dark
  size = 'md', // sm, md, lg
  disabled = false,
  className = '',
  type = 'button',
  icon = null,
  ...props
}) => {
  const handleClick = (e) => {
    if (disabled) return;
    sfx.playClick();
    if (onClick) onClick(e);
  };

  const variants = {
    yellow: 'pixel-btn-yellow',
    cyan: 'pixel-btn-cyan',
    pink: 'pixel-btn-pink',
    purple: 'pixel-btn-purple',
    violet: 'pixel-btn-violet',
    dark: 'bg-[#26005C] text-[#F5EEFF] hover:bg-[#3B077E]',
  };

  const sizes = {
    sm: 'text-xs px-3 py-1.5',
    md: 'text-sm px-5 py-2.5',
    lg: 'text-base px-7 py-3.5',
  };

  return (
    <button
      type={type}
      disabled={disabled}
      onClick={handleClick}
      className={`pixel-btn ${variants[variant] || variants.yellow} ${sizes[size]} ${
        disabled ? 'opacity-50 cursor-not-allowed transform-none shadow-[2px_2px_0px_#160033]' : ''
      } ${className}`}
      {...props}
    >
      {icon && <span className="inline-flex shrink-0">{icon}</span>}
      <span>{children}</span>
    </button>
  );
};
