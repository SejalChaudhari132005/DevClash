import React from 'react';
import { PixelCard } from './PixelCard';

export const GlassCard = ({ children, className = '', ...props }) => {
  return (
    <PixelCard variant="purple" className={className} {...props}>
      {children}
    </PixelCard>
  );
};
