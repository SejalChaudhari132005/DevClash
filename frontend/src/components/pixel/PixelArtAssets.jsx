import React from 'react';

/**
 * 1. Pixel Cloud: Multi-toned retro sunset cloud matching reference image
 */
export const PixelCloud = ({ className = '', size = 1 }) => {
  return (
    <svg
      width={120 * size}
      height={48 * size}
      viewBox="0 0 120 48"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={`select-none pointer-events-none ${className}`}
    >
      {/* Dark Purple Base Shadow */}
      <rect x="24" y="32" width="72" height="12" fill="#26005C" />
      <rect x="16" y="24" width="88" height="12" fill="#26005C" />
      
      {/* Light Purple / Violet Cloud Body */}
      <rect x="20" y="20" width="80" height="12" fill="#A72CFF" />
      <rect x="12" y="16" width="96" height="12" fill="#D9B8F4" />
      <rect x="28" y="8" width="48" height="12" fill="#D9B8F4" />
      <rect x="40" y="4" width="24" height="8" fill="#D9B8F4" />

      {/* Warm Sunset Highlights (Pink & Golden Apricot) */}
      <rect x="36" y="4" width="16" height="4" fill="#FFE45C" />
      <rect x="24" y="8" width="24" height="4" fill="#FF85D6" />
      <rect x="68" y="12" width="20" height="4" fill="#FFE45C" />
      <rect x="12" y="16" width="16" height="4" fill="#FF62C7" />
      <rect x="76" y="16" width="24" height="4" fill="#FF85D6" />

      {/* Crisp 2px Dark Border outline accents */}
      <rect x="40" y="0" width="24" height="4" fill="#160033" />
      <rect x="24" y="4" width="16" height="4" fill="#160033" />
      <rect x="64" y="4" width="16" height="4" fill="#160033" />
      <rect x="8" y="12" width="16" height="4" fill="#160033" />
      <rect x="80" y="8" width="24" height="4" fill="#160033" />
      <rect x="0" y="20" width="12" height="12" fill="#160033" />
      <rect x="108" y="20" width="12" height="12" fill="#160033" />
      <rect x="16" y="44" width="88" height="4" fill="#160033" />
    </svg>
  );
};

/**
 * 2. Pixel 4-Point Star / Sparkle in #FFE45C
 */
export const PixelStar = ({ className = '', size = 20, color = '#FFE45C' }) => {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 20 20"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={`select-none ${className}`}
    >
      <rect x="8" y="0" width="4" height="20" fill={color} />
      <rect x="0" y="8" width="20" height="4" fill={color} />
      <rect x="6" y="6" width="8" height="8" fill="#FFF099" />
      <rect x="8" y="8" width="4" height="4" fill="#FFFFFF" />
    </svg>
  );
};

export const PixelDotStar = ({ className = '', size = 10, color = '#FFE45C' }) => {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 10 10"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={`select-none ${className}`}
    >
      <rect x="4" y="0" width="2" height="10" fill={color} />
      <rect x="0" y="4" width="10" height="2" fill={color} />
      <rect x="3" y="3" width="4" height="4" fill="#FFFFFF" />
    </svg>
  );
};

/**
 * 3. Pixel Moon with floating sunset clouds (from reference image)
 */
export const PixelMoon = ({ className = '', size = 120 }) => {
  return (
    <div className={`relative ${className}`} style={{ width: size, height: size }}>
      <svg width={size} height={size} viewBox="0 0 100 100" fill="none">
        {/* Glowing Full Moon */}
        <circle cx="50" cy="50" r="44" fill="#FFE45C" />
        <circle cx="50" cy="50" r="40" fill="#FFF099" />
        {/* Pixel Moon Craters */}
        <rect x="30" y="32" width="12" height="8" fill="#F0CF43" opacity="0.6" />
        <rect x="58" y="24" width="16" height="10" fill="#F0CF43" opacity="0.6" />
        <rect x="42" y="62" width="20" height="8" fill="#F0CF43" opacity="0.6" />
      </svg>
      {/* Floating Cloud Across Moon */}
      <div className="absolute -bottom-2 -left-4">
        <PixelCloud size={0.7} />
      </div>
    </div>
  );
};

/**
 * 4. Street Lamp with Yellow Light Cone + Wooden Bench + Pixel Cat (homage to reference image)
 */
export const PixelStreetBenchCat = ({ className = '' }) => {
  return (
    <svg
      width="180"
      height="140"
      viewBox="0 0 180 140"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
    >
      {/* Yellow Cone of Light */}
      <polygon
        points="30,28 0,132 76,132"
        fill="#FFE45C"
        fillOpacity="0.3"
      />
      
      {/* Lamp Post */}
      <rect x="28" y="24" width="4" height="108" fill="#26005C" />
      <rect x="24" y="20" width="12" height="4" fill="#26005C" />
      {/* Lamp Head & Light */}
      <path d="M18 24 L42 24 L34 14 L26 14 Z" fill="#FF62C7" />
      <rect x="24" y="24" width="12" height="8" fill="#FFE45C" />
      
      {/* Wooden Park Bench */}
      {/* Bench Legs */}
      <rect x="80" y="98" width="6" height="34" fill="#160033" />
      <rect x="144" y="98" width="6" height="34" fill="#160033" />
      {/* Bench Backrest Slats */}
      <rect x="74" y="70" width="80" height="6" fill="#803D1E" />
      <rect x="74" y="78" width="80" height="6" fill="#A8572A" />
      <rect x="74" y="86" width="80" height="6" fill="#803D1E" />
      {/* Bench Seat Slat */}
      <rect x="70" y="94" width="88" height="8" fill="#D9824C" />
      <rect x="70" y="100" width="88" height="4" fill="#803D1E" />

      {/* Cute Pixel Cat sitting on bench */}
      {/* Cat Body */}
      <rect x="100" y="78" width="22" height="14" fill="#F5EEFF" />
      {/* Cat Head */}
      <rect x="94" y="72" width="14" height="12" fill="#F5EEFF" />
      {/* Ears (Pink inside) */}
      <rect x="94" y="68" width="4" height="4" fill="#F5EEFF" />
      <rect x="104" y="68" width="4" height="4" fill="#F5EEFF" />
      <rect x="95" y="69" width="2" height="2" fill="#FF62C7" />
      <rect x="105" y="69" width="2" height="2" fill="#FF62C7" />
      {/* Eyes & Whiskers */}
      <rect x="96" y="76" width="3" height="3" fill="#160033" />
      <rect x="102" y="76" width="3" height="3" fill="#160033" />
      <rect x="99" y="80" width="2" height="2" fill="#FF62C7" />
      {/* Tail curled up */}
      <rect x="122" y="80" width="8" height="4" fill="#F5EEFF" />
      <rect x="126" y="72" width="4" height="8" fill="#F5EEFF" />

      {/* Little green pixel bushes */}
      <rect x="156" y="104" width="18" height="28" fill="#38E5D1" />
      <rect x="160" y="100" width="12" height="12" fill="#6CF2E2" />
      <rect x="164" y="108" width="4" height="4" fill="#FF62C7" />
    </svg>
  );
};

/**
 * 5. Retro Arcade Storefront (matching the neon building from reference image)
 */
export const PixelArcadeStore = ({ className = '' }) => {
  return (
    <svg
      width="200"
      height="160"
      viewBox="0 0 200 160"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
    >
      {/* Roof & Signboard */}
      <rect x="20" y="10" width="160" height="24" fill="#160033" />
      <rect x="24" y="14" width="152" height="16" fill="#A72CFF" />
      {/* Neon Sign "DEVCLASH" */}
      <text
        x="100"
        y="26"
        fill="#38E5D1"
        fontFamily="'Press Start 2P', monospace"
        fontSize="9"
        textAnchor="middle"
        letterSpacing="2"
      >
        DEVCLASH
      </text>

      {/* Striped Awning (Turquoise and Hot Pink) */}
      <g>
        <rect x="16" y="34" width="168" height="14" fill="#38E5D1" />
        <rect x="26" y="34" width="14" height="14" fill="#FF62C7" />
        <rect x="54" y="34" width="14" height="14" fill="#FF62C7" />
        <rect x="82" y="34" width="14" height="14" fill="#FF62C7" />
        <rect x="110" y="34" width="14" height="14" fill="#FF62C7" />
        <rect x="138" y="34" width="14" height="14" fill="#FF62C7" />
        <rect x="166" y="34" width="14" height="14" fill="#FF62C7" />
      </g>

      {/* Building Body */}
      <rect x="22" y="48" width="156" height="96" fill="#26005C" />
      <rect x="22" y="48" width="156" height="4" fill="#160033" />

      {/* Glowing Arcade Display Windows */}
      <rect x="34" y="60" width="60" height="42" fill="#D9B8F4" />
      <rect x="38" y="64" width="24" height="34" fill="#FFE45C" />
      <rect x="66" y="64" width="24" height="34" fill="#38E5D1" />
      {/* Window mullions */}
      <rect x="62" y="60" width="4" height="42" fill="#26005C" />
      <rect x="34" y="78" width="60" height="4" fill="#26005C" />

      {/* Neon Arcade Door */}
      <rect x="110" y="58" width="48" height="86" fill="#160033" />
      <rect x="116" y="64" width="36" height="74" fill="#FF62C7" />
      <rect x="122" y="70" width="24" height="36" fill="#FFE45C" />
      <circle cx="144" cy="106" r="3" fill="#160033" />

      {/* Pixel Flower Box underneath window */}
      <rect x="30" y="106" width="68" height="12" fill="#160033" />
      <rect x="34" y="102" width="60" height="4" fill="#38E5D1" />
      <rect x="42" y="98" width="6" height="6" fill="#FF62C7" />
      <rect x="58" y="98" width="6" height="6" fill="#FFE45C" />
      <rect x="74" y="98" width="6" height="6" fill="#38E5D1" />
    </svg>
  );
};

/**
 * 6. Cyberpunk Night Skyline Skyscrapers (from reference image)
 */
export const PixelSkyline = ({ className = '' }) => {
  return (
    <svg
      width="180"
      height="220"
      viewBox="0 0 180 220"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
    >
      {/* Tower 1 (Spire Tower) */}
      <rect x="30" y="8" width="4" height="32" fill="#38E5D1" />
      <circle cx="32" cy="6" r="3" fill="#FF62C7" />
      <rect x="22" y="40" width="20" height="16" fill="#26005C" />
      <rect x="14" y="56" width="36" height="154" fill="#160033" />
      {/* Illuminated Windows Column */}
      <g fill="#38E5D1">
        <rect x="18" y="64" width="6" height="4" />
        <rect x="28" y="64" width="6" height="4" fill="#FF62C7" />
        <rect x="38" y="64" width="6" height="4" />
        <rect x="18" y="74" width="6" height="4" />
        <rect x="28" y="74" width="6" height="4" fill="#FFE45C" />
        <rect x="18" y="84" width="6" height="4" />
        <rect x="28" y="84" width="6" height="4" fill="#38E5D1" />
        <rect x="38" y="84" width="6" height="4" fill="#FF62C7" />
        <rect x="18" y="94" width="6" height="4" />
        <rect x="28" y="94" width="6" height="4" />
        <rect x="18" y="104" width="6" height="4" fill="#FFE45C" />
        <rect x="28" y="104" width="6" height="4" fill="#FF62C7" />
        <rect x="38" y="104" width="6" height="4" />
        <rect x="18" y="124" width="6" height="4" />
        <rect x="28" y="124" width="6" height="4" fill="#38E5D1" />
        <rect x="18" y="134" width="6" height="4" fill="#FF62C7" />
        <rect x="28" y="134" width="6" height="4" />
        <rect x="38" y="134" width="6" height="4" fill="#FFE45C" />
        <rect x="18" y="154" width="6" height="4" />
        <rect x="28" y="154" width="6" height="4" fill="#38E5D1" />
      </g>

      {/* Tower 2 (Angled Stepped Skyscraper) */}
      <rect x="68" y="70" width="48" height="140" fill="#26005C" />
      <polygon points="68,70 116,94 116,70" fill="#160033" />
      {/* Neon Windows */}
      <g fill="#FF85D6">
        <rect x="74" y="96" width="10" height="3" fill="#38E5D1" />
        <rect x="90" y="96" width="10" height="3" fill="#FFE45C" />
        <rect x="74" y="106" width="10" height="3" fill="#FF62C7" />
        <rect x="90" y="106" width="10" height="3" fill="#38E5D1" />
        <rect x="74" y="116" width="10" height="3" fill="#FFE45C" />
        <rect x="90" y="116" width="10" height="3" fill="#FF62C7" />
        <rect x="74" y="126" width="10" height="3" fill="#38E5D1" />
        <rect x="90" y="126" width="10" height="3" fill="#FFE45C" />
        <rect x="74" y="136" width="10" height="3" fill="#FF62C7" />
        <rect x="90" y="136" width="10" height="3" fill="#38E5D1" />
        <rect x="74" y="150" width="10" height="3" fill="#FFE45C" />
        <rect x="90" y="150" width="10" height="3" fill="#FF62C7" />
        <rect x="74" y="160" width="10" height="3" fill="#38E5D1" />
      </g>

      {/* Tower 3 (Right Sleek Tower) */}
      <rect x="124" y="90" width="40" height="120" fill="#160033" />
      <g fill="#38E5D1">
        <rect x="130" y="102" width="6" height="4" fill="#FF62C7" />
        <rect x="142" y="102" width="6" height="4" fill="#38E5D1" />
        <rect x="130" y="114" width="6" height="4" fill="#FFE45C" />
        <rect x="142" y="114" width="6" height="4" fill="#FF85D6" />
        <rect x="130" y="126" width="6" height="4" fill="#38E5D1" />
        <rect x="142" y="126" width="6" height="4" fill="#FFE45C" />
        <rect x="130" y="140" width="6" height="4" fill="#FF62C7" />
      </g>
    </svg>
  );
};

/**
 * 7. Golden LEVEL UP box with star & chest (from reference image)
 */
export const PixelLevelUpBadge = ({ className = '' }) => {
  return (
    <div
      className={`relative inline-flex flex-col items-center justify-center p-3 bg-[#FFE45C] border-4 border-[#160033] shadow-[4px_4px_0px_#160033] select-none ${className}`}
    >
      <div className="flex items-center gap-1 font-pixel text-[#160033] text-xs font-bold leading-tight">
        <span>LEVEL</span>
      </div>
      <div className="flex items-center gap-1 font-pixel text-[#160033] text-sm font-black">
        <span>UP</span>
      </div>

      {/* Decorative Gold Star Sparkle */}
      <div className="absolute -top-3 -right-3 animate-twinkle">
        <PixelStar size={18} />
      </div>

      {/* Pixel Chest / Trophy Accent */}
      <div className="mt-1">
        <svg width="24" height="18" viewBox="0 0 24 18" fill="none">
          <rect x="2" y="2" width="20" height="14" fill="#803D1E" stroke="#160033" strokeWidth="2" />
          <rect x="2" y="2" width="20" height="6" fill="#D9824C" />
          <rect x="10" y="6" width="4" height="5" fill="#FFE45C" />
        </svg>
      </div>
    </div>
  );
};

/**
 * 8. Grand Golden Arcade Trophy with Sparkles
 */
export const PixelTrophy = ({ size = 64, className = '' }) => {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 64 64"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={`select-none ${className}`}
    >
      {/* Left & Right Trophy Handles */}
      <path d="M12 16 H6 V28 H12 V24 H10 V20 H12 Z" fill="#FFE45C" stroke="#160033" strokeWidth="2" />
      <path d="M52 16 H58 V28 H52 V24 H54 V20 H52 Z" fill="#FFE45C" stroke="#160033" strokeWidth="2" />

      {/* Trophy Main Cup */}
      <path
        d="M14 10 H50 V26 C50 36 42 42 32 42 C22 42 14 36 14 26 Z"
        fill="#FFE45C"
        stroke="#160033"
        strokeWidth="3"
      />
      {/* Inner Shading & Highlight */}
      <path d="M18 14 H46 V18 H18 Z" fill="#FFF099" />
      <path d="M18 20 H22 V30 C24 34 28 36 32 36 C36 36 40 34 42 30 V20 H46 V26 C46 34 40 38 32 38 C24 38 18 34 18 26 Z" fill="#F0CF43" />
      
      {/* Embossed Star on Cup */}
      <rect x="30" y="22" width="4" height="10" fill="#160033" />
      <rect x="27" y="25" width="10" height="4" fill="#160033" />
      <rect x="29" y="24" width="6" height="6" fill="#FFF099" />

      {/* Stem */}
      <rect x="28" y="42" width="8" height="8" fill="#F0CF43" stroke="#160033" strokeWidth="2" />

      {/* Trophy Base Platform */}
      <rect x="20" y="50" width="24" height="6" fill="#26005C" stroke="#160033" strokeWidth="2" />
      <rect x="16" y="56" width="32" height="6" fill="#160033" />
      <rect x="24" y="52" width="16" height="2" fill="#38E5D1" />
    </svg>
  );
};

/**
 * 9. The 6 Selectable Pixel Fighter Avatars
 */
export const PixelAvatarSprite = ({ id = 'terminal', size = 48, className = '' }) => {
  const avatars = {
    terminal: {
      name: 'Cyber Coder',
      bg: '#38E5D1',
      render: () => (
        <svg viewBox="0 0 32 32" width={size} height={size} fill="none">
          {/* Hair / Hoodie */}
          <rect x="8" y="4" width="16" height="8" fill="#160033" />
          <rect x="6" y="8" width="20" height="16" fill="#26005C" />
          {/* Face */}
          <rect x="10" y="10" width="12" height="10" fill="#FFDFC4" />
          {/* Matrix Cyber Sunglasses */}
          <rect x="8" y="12" width="16" height="4" fill="#160033" />
          <rect x="10" y="13" width="5" height="2" fill="#38E5D1" />
          <rect x="17" y="13" width="5" height="2" fill="#38E5D1" />
          {/* Hoodie Body */}
          <rect x="8" y="22" width="16" height="8" fill="#A72CFF" />
          <rect x="14" y="22" width="4" height="8" fill="#D9B8F4" />
        </svg>
      ),
    },
    wizard: {
      name: 'Retro Wizard',
      bg: '#D9B8F4',
      render: () => (
        <svg viewBox="0 0 32 32" width={size} height={size} fill="none">
          {/* Wizard Hat */}
          <polygon points="16,2 8,14 24,14" fill="#A72CFF" />
          <rect x="6" y="13" width="20" height="3" fill="#FFE45C" />
          {/* Face */}
          <rect x="10" y="15" width="12" height="8" fill="#FFDFC4" />
          <rect x="11" y="17" width="2" height="2" fill="#160033" />
          <rect x="19" y="17" width="2" height="2" fill="#160033" />
          {/* Long Beard */}
          <rect x="10" y="20" width="12" height="8" fill="#F5EEFF" />
          <rect x="12" y="28" width="8" height="3" fill="#F5EEFF" />
        </svg>
      ),
    },
    knight: {
      name: 'Bit Knight',
      bg: '#FFE45C',
      render: () => (
        <svg viewBox="0 0 32 32" width={size} height={size} fill="none">
          {/* Helmet */}
          <rect x="8" y="4" width="16" height="18" fill="#94A3B8" />
          <rect x="14" y="2" width="4" height="4" fill="#FF62C7" />
          {/* Visor Slit */}
          <rect x="10" y="12" width="12" height="4" fill="#160033" />
          <rect x="12" y="13" width="8" height="2" fill="#FFE45C" />
          {/* Armor Chestplate */}
          <rect x="8" y="22" width="16" height="8" fill="#64748B" />
          <rect x="14" y="22" width="4" height="8" fill="#FFE45C" />
        </svg>
      ),
    },
    ninja: {
      name: 'Neon Ninja',
      bg: '#FF62C7',
      render: () => (
        <svg viewBox="0 0 32 32" width={size} height={size} fill="none">
          {/* Mask / Hood */}
          <rect x="8" y="6" width="16" height="16" fill="#160033" />
          {/* Headband */}
          <rect x="6" y="8" width="20" height="3" fill="#FF62C7" />
          {/* Eye Slit */}
          <rect x="10" y="12" width="12" height="5" fill="#FFDFC4" />
          <rect x="11" y="14" width="3" height="2" fill="#160033" />
          <rect x="18" y="14" width="3" height="2" fill="#160033" />
          {/* Ninja Gi */}
          <rect x="8" y="22" width="16" height="8" fill="#26005C" />
          <rect x="12" y="24" width="8" height="4" fill="#FF62C7" />
        </svg>
      ),
    },
    cat: {
      name: 'Pixel Cat',
      bg: '#C9BDF5',
      render: () => (
        <svg viewBox="0 0 32 32" width={size} height={size} fill="none">
          {/* Cat Ears */}
          <polygon points="8,4 12,12 6,12" fill="#F5EEFF" />
          <polygon points="24,4 20,12 26,12" fill="#F5EEFF" />
          <polygon points="9,6 11,11 7,11" fill="#FF62C7" />
          <polygon points="23,6 21,11 25,11" fill="#FF62C7" />
          {/* Head */}
          <rect x="6" y="10" width="20" height="14" fill="#F5EEFF" />
          {/* Headphones */}
          <rect x="4" y="12" width="3" height="8" fill="#A72CFF" />
          <rect x="25" y="12" width="3" height="8" fill="#A72CFF" />
          <rect x="6" y="9" width="20" height="2" fill="#A72CFF" />
          {/* Eyes & Nose */}
          <rect x="10" y="14" width="3" height="3" fill="#160033" />
          <rect x="19" y="14" width="3" height="3" fill="#160033" />
          <rect x="15" y="18" width="2" height="2" fill="#FF62C7" />
          {/* Body */}
          <rect x="8" y="24" width="16" height="6" fill="#F5EEFF" />
        </svg>
      ),
    },
    robot: {
      name: 'Glitch Bot',
      bg: '#6CF2E2',
      render: () => (
        <svg viewBox="0 0 32 32" width={size} height={size} fill="none">
          {/* Antenna */}
          <rect x="15" y="2" width="2" height="6" fill="#160033" />
          <circle cx="16" cy="2" r="2" fill="#FFE45C" />
          {/* CRT Monitor Head */}
          <rect x="6" y="8" width="20" height="14" fill="#94A3B8" stroke="#160033" strokeWidth="2" />
          <rect x="9" y="11" width="14" height="8" fill="#160033" />
          {/* Glowing Eyes */}
          <rect x="11" y="13" width="3" height="4" fill="#38E5D1" />
          <rect x="18" y="13" width="3" height="4" fill="#38E5D1" />
          {/* Chassis */}
          <rect x="8" y="24" width="16" height="6" fill="#64748B" />
          <rect x="12" y="25" width="8" height="3" fill="#FF62C7" />
        </svg>
      ),
    },
  };

  const current = avatars[id] || avatars.terminal;

  return (
    <div
      className={`relative inline-flex items-center justify-center border-2 border-[#160033] shadow-[2px_2px_0px_#160033] ${className}`}
      style={{ backgroundColor: current.bg, width: size, height: size }}
      title={current.name}
    >
      {current.render()}
    </div>
  );
};

export const AVAILABLE_AVATARS = [
  { id: 'terminal', name: 'Cyber Coder', title: 'Language Architect' },
  { id: 'wizard', name: 'Retro Wizard', title: 'Syntax Enchanter' },
  { id: 'knight', name: 'Bit Knight', title: 'Compiler Champion' },
  { id: 'ninja', name: 'Neon Ninja', title: 'Latency Striker' },
  { id: 'cat', name: 'Pixel Cat', title: 'Code Purr-fectionist' },
  { id: 'robot', name: 'Glitch Bot', title: 'Deadlock Breaker' },
];
