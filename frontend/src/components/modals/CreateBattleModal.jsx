import React, { useState } from 'react';
import { X, Play, Crown, Sparkles, Flame, Brain, Shield } from 'lucide-react';
import { Button } from '../common/Button';
import { Badge } from '../common/Badge';
import { PixelAvatarSprite, AVAILABLE_AVATARS } from '../pixel/PixelArtAssets';
import { CATEGORIES_DATA } from '../home/CategoriesSection';
import { sfx } from '../../utils/audio';

export const CreateBattleModal = ({ isOpen, onClose, onCreateRoom, initialCategory = null }) => {
  const [nickname, setNickname] = useState('PixelWarrior');
  const [selectedAvatar, setSelectedAvatar] = useState('terminal');
  const [selectedCategory, setSelectedCategory] = useState(initialCategory?.id || 1);
  const [difficulty, setDifficulty] = useState('MEDIUM');
  const [gameMode, setGameMode] = useState('CLASSIC');
  const [questionCount, setQuestionCount] = useState(10);
  const [timeLimit, setTimeLimit] = useState(20);

  if (!isOpen) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!nickname.trim()) return;

    const catObj = CATEGORIES_DATA.find((c) => c.id === Number(selectedCategory)) || CATEGORIES_DATA[0];
    const config = {
      categoryId: catObj.id,
      categoryName: catObj.name,
      difficulty,
      gameMode,
      totalQuestions: questionCount,
      timeLimitSec: timeLimit,
    };

    sfx.playClick();
    onCreateRoom(nickname.trim(), selectedAvatar, config);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-[#160033]/85 backdrop-blur-xs select-none">
      <div className="relative w-full max-w-2xl bg-[#26005C] border-4 border-[#160033] shadow-[8px_8px_0px_#160033] max-h-[90vh] overflow-y-auto">
        {/* Arcade Cabinet Header */}
        <div className="sticky top-0 z-10 px-4 py-3 bg-[#FFE45C] border-b-4 border-[#160033] flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Crown className="w-5 h-5 text-[#160033]" />
            <span className="font-pixel text-sm md:text-base text-[#160033]">
              CREATE BATTLE ARENA
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

        {/* Modal Form Content */}
        <form onSubmit={handleSubmit} className="p-4 md:p-6 space-y-6">
          {/* 1. Host Nickname & Avatar Selection */}
          <div className="p-4 bg-[#160033] border-3 border-[#A72CFF] shadow-[4px_4px_0px_#160033]">
            <label className="block font-arcade text-xs text-[#FFE45C] uppercase font-bold mb-2">
              1. FIGHTER IDENTITY & AVATAR
            </label>
            <div className="flex flex-col sm:flex-row items-center gap-4 mb-4">
              <PixelAvatarSprite id={selectedAvatar} size={64} />
              <div className="flex-1 w-full">
                <input
                  type="text"
                  maxLength={15}
                  value={nickname}
                  onChange={(e) => setNickname(e.target.value)}
                  placeholder="Enter fighter nickname..."
                  className="w-full bg-[#F5EEFF] border-3 border-[#160033] px-3 py-2 font-arcade text-sm text-[#160033] font-bold focus:outline-none focus:bg-[#FFE45C]"
                  required
                />
              </div>
            </div>

            {/* Avatar Selector Strip */}
            <div className="grid grid-cols-6 gap-2">
              {AVAILABLE_AVATARS.map((av) => (
                <button
                  type="button"
                  key={av.id}
                  onClick={() => {
                    sfx.playClick();
                    setSelectedAvatar(av.id);
                  }}
                  className={`p-1.5 border-2 transition-all flex flex-col items-center cursor-pointer ${
                    selectedAvatar === av.id
                      ? 'border-[#FFE45C] bg-[#FFE45C]/20 scale-105 shadow-[2px_2px_0px_#FFE45C]'
                      : 'border-[#26005C] bg-[#26005C] opacity-70 hover:opacity-100'
                  }`}
                  title={av.name}
                >
                  <PixelAvatarSprite id={av.id} size={32} />
                  <span className="font-arcade text-[9px] text-[#F5EEFF] mt-1 truncate max-w-full">
                    {av.name.split(' ')[0]}
                  </span>
                </button>
              ))}
            </div>
          </div>

          {/* 2. Category Selection */}
          <div>
            <label className="block font-arcade text-xs text-[#FFE45C] uppercase font-bold mb-2">
              2. SELECT BATTLE CATEGORY
            </label>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
              {CATEGORIES_DATA.map((cat) => (
                <button
                  type="button"
                  key={cat.id}
                  onClick={() => {
                    sfx.playClick();
                    setSelectedCategory(cat.id);
                  }}
                  className={`p-3 text-left border-3 transition-all cursor-pointer ${
                    selectedCategory === cat.id
                      ? 'bg-[#C9BDF5] text-[#160033] border-[#160033] shadow-[3px_3px_0px_#160033] font-bold'
                      : 'bg-[#160033] text-[#C9BDF5] border-[#26005C] hover:border-[#D9B8F4]'
                  }`}
                >
                  <div className="font-arcade text-xs font-bold truncate">
                    {cat.name}
                  </div>
                  <div className="text-[11px] opacity-80 truncate mt-0.5">
                    {cat.topics.join(', ')}
                  </div>
                </button>
              ))}
            </div>
          </div>

          {/* 3. Difficulty Selection */}
          <div>
            <label className="block font-arcade text-xs text-[#FFE45C] uppercase font-bold mb-2">
              3. SELECT DIFFICULTY
            </label>
            <div className="grid grid-cols-3 gap-3">
              {[
                { id: 'EASY', label: 'EASY', color: 'cyan', icon: <Shield className="w-3.5 h-3.5" /> },
                { id: 'MEDIUM', label: 'MEDIUM', color: 'yellow', icon: <Sparkles className="w-3.5 h-3.5" /> },
                { id: 'HARD', label: 'HARD', color: 'pink', icon: <Flame className="w-3.5 h-3.5" /> },
              ].map((diff) => (
                <button
                  type="button"
                  key={diff.id}
                  onClick={() => {
                    sfx.playClick();
                    setDifficulty(diff.id);
                  }}
                  className={`py-2 px-3 border-3 font-arcade text-xs font-black flex items-center justify-center gap-1.5 transition-all cursor-pointer ${
                    difficulty === diff.id
                      ? 'bg-[#FFE45C] text-[#160033] border-[#160033] shadow-[3px_3px_0px_#160033] scale-102'
                      : 'bg-[#160033] text-[#F5EEFF] border-[#26005C] hover:border-[#A72CFF]'
                  }`}
                >
                  {diff.icon}
                  {diff.label}
                </button>
              ))}
            </div>
          </div>

          {/* 4. Game Mode Selection */}
          <div>
            <label className="block font-arcade text-xs text-[#FFE45C] uppercase font-bold mb-2">
              4. SELECT GAME MODE
            </label>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              {[
                {
                  id: 'CLASSIC',
                  name: 'CLASSIC BATTLE',
                  desc: '1000 pts per correct answer + 100 pt combo streak bonus.',
                },
                {
                  id: 'SPEED_RUSH',
                  name: 'SPEED RUSH',
                  desc: 'High speed ratio multiplier! Fast answers award up to 1500 pts.',
                },
                {
                  id: 'BRAIN_BATTLE',
                  name: 'BRAIN BATTLE',
                  desc: 'High stakes! 1200 pts for correct, -300 pts penalty for wrong!',
                },
              ].map((mode) => (
                <button
                  type="button"
                  key={mode.id}
                  onClick={() => {
                    sfx.playClick();
                    setGameMode(mode.id);
                  }}
                  className={`p-3 text-left border-3 transition-all cursor-pointer ${
                    gameMode === mode.id
                      ? 'bg-[#38E5D1] text-[#160033] border-[#160033] shadow-[3px_3px_0px_#160033]'
                      : 'bg-[#160033] text-[#F5EEFF] border-[#26005C] hover:border-[#38E5D1]'
                  }`}
                >
                  <div className="font-arcade text-xs font-black mb-1">{mode.name}</div>
                  <div className="text-[11px] leading-tight text-[#160033]/80 font-medium">
                    {mode.desc}
                  </div>
                </button>
              ))}
            </div>
          </div>

          {/* 5. Match Length & Time Limit Controls */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block font-arcade text-xs text-[#C9BDF5] mb-1">
                QUESTIONS PER ROUND
              </label>
              <select
                value={questionCount}
                onChange={(e) => setQuestionCount(Number(e.target.value))}
                className="w-full bg-[#160033] border-3 border-[#D9B8F4] text-[#F5EEFF] font-arcade text-xs p-2 focus:outline-none"
              >
                <option value={5}>5 Questions</option>
                <option value={10}>10 Questions (Standard)</option>
                <option value={15}>15 Questions (Championship)</option>
                <option value={20}>20 Questions (Endurance)</option>
              </select>
            </div>

            <div>
              <label className="block font-arcade text-xs text-[#C9BDF5] mb-1">
                TIME PER QUESTION
              </label>
              <select
                value={timeLimit}
                onChange={(e) => setTimeLimit(Number(e.target.value))}
                className="w-full bg-[#160033] border-3 border-[#D9B8F4] text-[#F5EEFF] font-arcade text-xs p-2 focus:outline-none"
              >
                <option value={10}>10 Seconds (Intense)</option>
                <option value={15}>15 Seconds (Fast)</option>
                <option value={20}>20 Seconds (Standard)</option>
                <option value={30}>30 Seconds (Relaxed)</option>
              </select>
            </div>
          </div>

          {/* Actions Footer */}
          <div className="pt-4 border-t-3 border-[#160033] flex items-center justify-end gap-3">
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
              icon={<Play className="w-5 h-5 fill-[#160033]" />}
            >
              CREATE ROOM & START
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};
