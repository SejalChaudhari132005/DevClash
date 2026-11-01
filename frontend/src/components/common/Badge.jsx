import React from 'react';

export const Badge = ({
  children,
  variant = 'purple', // lavender, purple, cyan, yellow, pink, dark
  className = '',
  icon = null,
}) => {
  const variants = {
    lavender: 'bg-[#C9BDF5] text-[#160033]',
    purple: 'bg-[#D9B8F4] text-[#160033]',
    cyan: 'bg-[#38E5D1] text-[#160033]',
    yellow: 'bg-[#FFE45C] text-[#160033]',
    pink: 'bg-[#FF62C7] text-[#160033]',
    dark: 'bg-[#160033] text-[#F5EEFF] border-[#D9B8F4]',
  };

  return (
    <span className={`pixel-pill ${variants[variant] || variants.purple} ${className}`}>
      {icon && <span className="inline-block">{icon}</span>}
      {children}
    </span>
  );
};
