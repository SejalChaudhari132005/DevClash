import React from 'react';
import { Code2, Binary, Database, Cpu, Network, Sparkles, ArrowRight } from 'lucide-react';
import { Badge } from '../common/Badge';
import { sfx } from '../../utils/audio';

const CATEGORY_ICONS = {
  programming: (
    <div className="w-12 h-12 bg-[#38E5D1] border-3 border-[#160033] shadow-[3px_3px_0px_#160033] flex items-center justify-center">
      <Code2 className="w-7 h-7 text-[#160033]" />
    </div>
  ),
  dsa: (
    <div className="w-12 h-12 bg-[#FFE45C] border-3 border-[#160033] shadow-[3px_3px_0px_#160033] flex items-center justify-center">
      <Binary className="w-7 h-7 text-[#160033]" />
    </div>
  ),
  dbms: (
    <div className="w-12 h-12 bg-[#FF62C7] border-3 border-[#160033] shadow-[3px_3px_0px_#160033] flex items-center justify-center">
      <Database className="w-7 h-7 text-[#160033]" />
    </div>
  ),
  os: (
    <div className="w-12 h-12 bg-[#A72CFF] border-3 border-[#160033] shadow-[3px_3px_0px_#160033] flex items-center justify-center">
      <Cpu className="w-7 h-7 text-[#F5EEFF]" />
    </div>
  ),
  networks: (
    <div className="w-12 h-12 bg-[#C9BDF5] border-3 border-[#160033] shadow-[3px_3px_0px_#160033] flex items-center justify-center">
      <Network className="w-7 h-7 text-[#160033]" />
    </div>
  ),
  'ai-tech': (
    <div className="w-12 h-12 bg-[#FFE45C] border-3 border-[#160033] shadow-[3px_3px_0px_#160033] flex items-center justify-center">
      <Sparkles className="w-7 h-7 text-[#160033]" />
    </div>
  ),
};

export const CategoryCard = ({ category, onSelect }) => {
  const { id, name, slug, description, topics, color = '#C9BDF5', questionCount = 10 } = category;

  const handleCardClick = () => {
    sfx.playClick();
    if (onSelect) onSelect(category);
  };

  return (
    <div
      onClick={handleCardClick}
      className="group relative bg-[#26005C] border-4 border-[#160033] shadow-[6px_6px_0px_#160033] hover:shadow-[8px_8px_0px_#38E5D1] hover:-translate-y-1 transition-all duration-150 cursor-pointer flex flex-col justify-between p-5 select-none"
    >
      {/* Top Header Badge */}
      <div className="flex items-start justify-between gap-3 mb-4">
        {CATEGORY_ICONS[slug] || CATEGORY_ICONS.programming}
        <Badge variant="yellow" className="text-[10px]">
          {questionCount} QUESTIONS
        </Badge>
      </div>

      {/* Title & Description */}
      <div className="mb-4">
        <h3 className="font-arcade text-lg md:text-xl font-bold text-[#F5EEFF] group-hover:text-[#FFE45C] transition-colors mb-2 leading-tight">
          {name}
        </h3>
        <p className="text-xs md:text-sm text-[#C9BDF5] line-clamp-2 font-medium">
          {description}
        </p>
      </div>

      {/* Subtopics Pill Badges */}
      <div className="flex flex-wrap gap-1.5 mb-5">
        {topics && topics.map((t, i) => (
          <span
            key={i}
            className="px-2 py-0.5 bg-[#160033] text-[#38E5D1] font-arcade text-[10px] border border-[#A72CFF]"
          >
            {t}
          </span>
        ))}
      </div>

      {/* Bottom Action Strip */}
      <div className="pt-3 border-t-2 border-[#160033]/60 flex items-center justify-between">
        <span className="font-arcade text-xs text-[#FFE45C] font-bold group-hover:underline">
          PRESS TO CLASH
        </span>
        <div className="w-7 h-7 bg-[#38E5D1] border-2 border-[#160033] flex items-center justify-center group-hover:translate-x-1 transition-transform">
          <ArrowRight className="w-4 h-4 text-[#160033]" />
        </div>
      </div>
    </div>
  );
};
