import React from 'react';
import { CategoryCard } from './CategoryCard';
import { PixelStar } from '../pixel/PixelArtAssets';
import { Badge } from '../common/Badge';

export const CATEGORIES_DATA = [
  {
    id: 1,
    name: 'PROGRAMMING',
    slug: 'programming',
    description: 'Core OOP, Java streams, Python idioms, C pointers, and C++ memory models.',
    topics: ['Java', 'Python', 'C', 'C++'],
    questionCount: 10,
  },
  {
    id: 2,
    name: 'DATA STRUCTURES & ALGORITHMS',
    slug: 'dsa',
    description: 'Array manipulation, balanced trees, graph BFS/DFS, sorting, and complexity analysis.',
    topics: ['Arrays', 'Trees', 'Graphs', 'Sorting', 'Searching'],
    questionCount: 10,
  },
  {
    id: 3,
    name: 'DATABASE MANAGEMENT',
    slug: 'dbms',
    description: 'Relational queries, index optimization, ACID concurrency, and schema normalization.',
    topics: ['SQL', 'Normalization', 'Transactions', 'Locks'],
    questionCount: 10,
  },
  {
    id: 4,
    name: 'OPERATING SYSTEMS',
    slug: 'os',
    description: 'CPU scheduling algorithms, virtual memory paging, multithreading, and deadlock avoidance.',
    topics: ['Processes', 'Threads', 'Scheduling', 'Deadlocks'],
    questionCount: 10,
  },
  {
    id: 5,
    name: 'COMPUTER NETWORKS',
    slug: 'networks',
    description: 'TCP/IP handshake, OSI 7-layer stack, DNS resolution, and HTTP/3 protocol mechanics.',
    topics: ['TCP/IP', 'HTTP', 'DNS', 'OSI Model'],
    questionCount: 10,
  },
  {
    id: 6,
    name: 'AI & TECHNOLOGY',
    slug: 'ai-tech',
    description: 'Machine learning fundamentals, transformer architectures, and distributed systems.',
    topics: ['AI', 'Machine Learning', 'Generative AI', 'Cloud'],
    questionCount: 10,
  },
];

export const CategoriesSection = ({ onSelectCategory }) => {
  return (
    <section className="relative py-16 bg-[#160033] border-b-8 border-[#160033] overflow-hidden">
      {/* Background Ambience Stars */}
      <div className="absolute top-8 right-12 animate-twinkle">
        <PixelStar size={20} />
      </div>
      <div className="absolute bottom-16 left-12 animate-twinkle" style={{ animationDelay: '1.2s' }}>
        <PixelStar size={16} />
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        {/* Section Header */}
        <div className="flex flex-col items-center text-center mb-12">
          <Badge variant="cyan" className="mb-3 text-xs">
            SELECT ARENA
          </Badge>
          <h2 className="font-pixel text-2xl sm:text-3xl md:text-5xl text-[#F5EEFF] mb-3 drop-shadow-[3px_3px_0px_#26005C]">
            CHOOSE YOUR BATTLE
          </h2>
          <p className="max-w-xl text-sm md:text-base text-[#C9BDF5]">
            Pick a computer science domain. Jump into an existing room or launch your own multiplayer battle.
          </p>
        </div>

        {/* 6 Category Cards Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-16">
          {CATEGORIES_DATA.map((cat) => (
            <CategoryCard
              key={cat.id}
              category={cat}
              onSelect={onSelectCategory}
            />
          ))}
        </div>

        {/* Retro City Skyline & Arcade Info Box Row */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center bg-[#26005C] border-4 border-[#160033] shadow-[8px_8px_0px_#160033] p-6 md:p-8">
          {/* Left: Retro OS / Terminal Window "HAL YG PERLU DISIAPKAN" style */}
          <div className="lg:col-span-6">
            {/* Retro Window Bar */}
            <div className="bg-[#C9BDF5] border-3 border-[#160033] shadow-[4px_4px_0px_#160033] p-4 text-[#160033]">
              <div className="flex items-center justify-between pb-2 mb-3 border-b-2 border-[#160033]">
                <span className="font-pixel text-xs md:text-sm font-bold">
                  BATTLE RULES & HOW TO PLAY
                </span>
                <span className="px-2 py-0.5 bg-[#FFE45C] border border-[#160033] font-arcade text-[10px] font-black">
                  RULES.TXT
                </span>
              </div>
              <ul className="font-terminal text-base md:text-lg space-y-1.5 leading-tight">
                <li>1. CREATE ROOM or join using a 6-digit room code.</li>
                <li>2. Wait in the arcade lobby until all fighters toggle READY.</li>
                <li>3. HOST presses START BATTLE when ready (min 2 players).</li>
                <li>4. 10 technical questions per match, 20s countdown per round.</li>
                <li>5. SPEED RUSH rewards speed; BRAIN BATTLE penalizes mistakes!</li>
                <li>6. Real-time distributed state maintained centrally on Java RMI!</li>
              </ul>
            </div>
          </div>

          {/* Right: Retro Pixel City Metropolis (from user uploaded reference artwork) */}
          <div className="lg:col-span-6 flex flex-col items-center justify-center">
            <div className="relative border-4 border-[#160033] shadow-[6px_6px_0px_#160033] overflow-hidden bg-[#160033] w-full">
              <div className="bg-[#FFE45C] border-b-3 border-[#160033] px-3 py-1 flex items-center justify-between">
                <span className="font-pixel text-[10px] sm:text-xs text-[#160033] font-black">
                  DEVCLASH METROPOLIS
                </span>
                <span className="font-arcade text-[10px] bg-[#160033] text-[#38E5D1] px-2 py-0.5 border border-[#160033]">
                  BATTLE DISTRICT
                </span>
              </div>
              <div className="relative overflow-hidden aspect-video bg-[#26005C]">
                <img
                  src="/images/pixel-city.jpg"
                  alt="DevClash Pixel City"
                  className="w-full h-full object-cover pixelated select-none"
                  style={{ imageRendering: 'pixelated' }}
                />
              </div>
              <div className="bg-[#160033] border-t-2 border-[#FFE45C] px-3 py-1.5 flex items-center justify-between">
                <span className="font-arcade text-[10px] text-[#C9BDF5]">CYBERPUNK DISTRICT • ARENA 01</span>
                <span className="font-arcade text-[10px] text-[#38E5D1] font-bold">ONLINE</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};
