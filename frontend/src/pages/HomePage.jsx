import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useGame } from '../context/GameContext';
import { HeroSection } from '../components/home/HeroSection';
import { CategoriesSection } from '../components/home/CategoriesSection';
import { CreateBattleModal } from '../components/modals/CreateBattleModal';
import { JoinBattleModal } from '../components/modals/JoinBattleModal';
import { sfx } from '../utils/audio';

export const HomePage = () => {
  const navigate = useNavigate();
  const { createRoom, joinRoom, isConnected, errorMsg, clearError } = useGame();

  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [isJoinOpen, setIsJoinOpen] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [prefilledJoinCode, setPrefilledJoinCode] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [customError, setCustomError] = useState(null);

  const handleOpenCreate = (category = null) => {
    sfx.playClick();
    setSelectedCategory(category);
    setIsCreateOpen(true);
  };

  const handleOpenJoin = (code = '') => {
    sfx.playClick();
    setPrefilledJoinCode(code);
    setIsJoinOpen(true);
  };

  const handleExecuteCreateRoom = async (nickname, avatar, config) => {
    setIsLoading(true);
    setCustomError(null);
    clearError();

    try {
      const roomData = await createRoom(nickname, avatar, config);
      setIsCreateOpen(false);
      navigate(`/lobby/${roomData.roomCode}`);
    } catch (err) {
      console.error('Room create error:', err);
      setCustomError(err.message || 'Failed to create room.');
      sfx.playWrong();
    } finally {
      setIsLoading(false);
    }
  };

  const handleExecuteJoinRoom = async (roomCode, nickname, avatar) => {
    setIsLoading(true);
    setCustomError(null);
    clearError();

    try {
      await joinRoom(roomCode, nickname, avatar);
      setIsJoinOpen(false);
      navigate(`/lobby/${roomCode}`);
    } catch (err) {
      console.error('Room join error:', err);
      setCustomError(err.message || 'Cannot join room.');
      sfx.playWrong();
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="w-full">
      {/* 1. Hero Section with retro pixel-art arcade environment */}
      <HeroSection
        onStartPlaying={() => handleOpenCreate(null)}
        onCreateBattle={() => handleOpenCreate(null)}
        onJoinBattle={(code) => handleOpenJoin(code)}
      />

      {/* 2. Choose Your Battle: 6 Computer Science Categories */}
      <CategoriesSection
        onSelectCategory={(cat) => handleOpenCreate(cat)}
      />

      {/* 3. Create Battle Modal */}
      <CreateBattleModal
        isOpen={isCreateOpen}
        onClose={() => setIsCreateOpen(false)}
        onCreateRoom={handleExecuteCreateRoom}
        initialCategory={selectedCategory}
      />

      {/* 4. Join Battle Modal with 6-Digit Pixel Input */}
      <JoinBattleModal
        isOpen={isJoinOpen}
        onClose={() => setIsJoinOpen(false)}
        onJoinRoom={handleExecuteJoinRoom}
        prefilledCode={prefilledJoinCode}
        errorMessage={customError || errorMsg}
        isConnected={isConnected}
      />

      {/* Bottom Checkered Cobblestone Platform Footer Strip */}
      <div className="w-full h-8 pixel-pavement" />
    </div>
  );
};
