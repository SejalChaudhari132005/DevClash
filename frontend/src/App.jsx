import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { GameProvider, useGame } from './context/GameContext';
import { Navbar } from './components/common/Navbar';
import { HomePage } from './pages/HomePage';
import { LobbyPage } from './pages/LobbyPage';
import { BattlePage } from './pages/BattlePage';
import { ResultsPage } from './pages/ResultsPage';

const AppContent = () => {
  const { isConnected } = useGame();

  return (
    <div className="min-h-screen flex flex-col bg-[#160033] text-[#F5EEFF]">
      <Navbar isConnected={isConnected} />
      <main className="flex-1">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/lobby/:code" element={<LobbyPage />} />
          <Route path="/battle" element={<BattlePage />} />
          <Route path="/results" element={<ResultsPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      <footer className="py-6 text-center text-xs text-[#C9BDF5] bg-[#160033] border-t-4 border-[#160033]">
        <div className="max-w-7xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-3">
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 bg-[#FFE45C] border border-[#160033] inline-block animate-pulse" />
            <span className="font-arcade text-xs text-[#FFE45C]">DEVCLASH ARCADE ENGINE</span>
          </div>
          <p className="font-arcade text-[11px] text-[#C9BDF5]">
            Distributed Computing MicroProject • Java RMI / RPC Registry (Port 1099) • Spring Boot Gateway (Port 8085)
          </p>
          <div className="px-2 py-0.5 bg-[#FFE45C] text-[#160033] font-pixel text-[9px] border-2 border-[#160033] shadow-[2px_2px_0px_#160033]">
            CREDIT: 99
          </div>
        </div>
      </footer>
    </div>
  );
};

export default function App() {
  return (
    <BrowserRouter>
      <GameProvider>
        <AppContent />
      </GameProvider>
    </BrowserRouter>
  );
}
