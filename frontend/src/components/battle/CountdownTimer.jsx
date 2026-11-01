import React from 'react';
import { Clock } from 'lucide-react';

export const CountdownTimer = ({ secondsRemaining, totalSeconds = 20, percentage: customPercentage }) => {
  const percentage = customPercentage !== undefined
    ? Math.max(0, Math.min(100, customPercentage))
    : Math.max(0, Math.min(100, (secondsRemaining / totalSeconds) * 100));
  const isUrgent = secondsRemaining <= 5;
  const isWarning = secondsRemaining <= 10 && secondsRemaining > 5;

  let barColor = 'bg-[#38E5D1] shadow-[0_0_8px_#38E5D1]';
  if (isUrgent) barColor = 'bg-[#FF62C7] animate-pulse shadow-[0_0_12px_#FF62C7]';
  else if (isWarning) barColor = 'bg-[#FFE45C] shadow-[0_0_8px_#FFE45C]';

  return (
    <div className="w-full select-none">
      <div className="flex items-center justify-between font-pixel text-xs mb-1.5">
        <span className="flex items-center gap-1.5 text-[#C9BDF5]">
          <Clock className={`w-4 h-4 ${isUrgent ? 'text-[#FF62C7] animate-spin' : 'text-[#FFE45C]'}`} />
          ROUND TIMER
        </span>
        <span
          className={`text-sm md:text-base font-black tracking-widest ${
            isUrgent ? 'text-[#FF62C7] scale-110 animate-bounce' : 'text-[#FFE45C]'
          }`}
        >
          {secondsRemaining}s
        </span>
      </div>

      {/* Segmented Pixel Bar Outer Border */}
      <div className="w-full h-5 bg-[#0a001a] border-3 border-[#160033] shadow-[3px_3px_0px_#160033] p-0.5 relative overflow-hidden">
        <div
          className={`h-full transition-[width] duration-100 ease-linear ${barColor}`}
          style={{ width: `${percentage}%` }}
        />
      </div>
    </div>
  );
};
