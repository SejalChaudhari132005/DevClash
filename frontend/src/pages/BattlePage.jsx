import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Flame, Trophy, Sparkles, ChevronRight, BarChart2 } from 'lucide-react';
import { useGame, loadSession } from '../context/GameContext';
import { CountdownTimer } from '../components/battle/CountdownTimer';
import { AnswerOption } from '../components/battle/AnswerOption';
import { BattleLeaderboard } from '../components/battle/BattleLeaderboard';
import { Button } from '../components/common/Button';
import { Badge } from '../components/common/Badge';
import { PixelAvatarSprite } from '../components/pixel/PixelArtAssets';
import { sfx } from '../utils/audio';

// Realistic sample questions for standalone preview / testing
const SAMPLE_QUESTIONS = [
  {
    questionIndex: 1,
    questionText: 'What will happen when the following Java code executes?',
    codeSnippet: 'List<Integer> list = List.of(1, 2, 3);\nlist.add(4);',
    optionA: '4 is appended to the list',
    optionB: 'UnsupportedOperationException is thrown',
    optionC: 'IndexOutOfBoundsException is thrown',
    optionD: 'Compilation Error',
    correctOption: 'B',
    explanation: 'List.of() returns an unmodifiable list. Mutating operations throw UnsupportedOperationException.',
  },
  {
    questionIndex: 2,
    questionText: 'What is the average time complexity of searching in a Hash Map with a good hash function?',
    codeSnippet: 'map.get(key); // Assuming well-distributed hash bucket',
    optionA: 'O(1)',
    optionB: 'O(log n)',
    optionC: 'O(n)',
    optionD: 'O(n log n)',
    correctOption: 'A',
    explanation: 'Direct bucket addressing provides O(1) amortized lookup in hash tables.',
  },
  {
    questionIndex: 3,
    questionText: 'Which ACID property guarantees that all database changes are permanent once committed?',
    codeSnippet: 'COMMIT TRANSACTION; -- WAL written to disk',
    optionA: 'Atomicity',
    optionB: 'Consistency',
    optionC: 'Isolation',
    optionD: 'Durability',
    correctOption: 'D',
    explanation: 'Durability ensures committed transactions survive unexpected system crashes.',
  },
];

export const BattlePage = () => {
  const navigate = useNavigate();
  const {
    room,
    player,
    players,
    currentQuestion: serverQuestion,
    questionIndex: serverQuestionIndex,
    totalQuestions: serverTotalQuestions,
    secondsRemaining: serverSecondsRemaining,
    roundResult: serverRoundResult,
    leaderboard: serverLeaderboard,
    gameSummary,
    submitAnswer: serverSubmitAnswer,
    refreshRoomState,
  } = useGame();

  // On mount: ensure live room state is synchronized with server
  useEffect(() => {
    const session = loadSession();
    const code = room?.roomCode || session?.roomCode;
    if (code && refreshRoomState) {
      refreshRoomState(code);
    }
  }, []);

  // Local state for interactive preview & offline simulation
  const [activeQuestion, setActiveQuestion] = useState(SAMPLE_QUESTIONS[0]);
  const [currentRound, setCurrentRound] = useState(1);
  const [totalRounds, setTotalRounds] = useState(10);
  const [secondsLeft, setSecondsLeft] = useState(20);
  const [timerPercentage, setTimerPercentage] = useState(100);
  const questionStartTimeRef = useRef(Date.now());
  const activeTimeLimitRef = useRef(20);

  const [selectedOption, setSelectedOption] = useState(null);
  const [isAnswerLocked, setIsAnswerLocked] = useState(false);
  const [roundOver, setRoundOver] = useState(false);
  const [roundFeedback, setRoundFeedback] = useState(null);
  const [score, setScore] = useState(0);
  const [streak, setStreak] = useState(0);
  const [showLeaderboardModal, setShowLeaderboardModal] = useState(false);

  // Derive live player stats from distributed WebSocket context
  const effectivePlayers = players || [];
  const currentPlayer = effectivePlayers.find((p) => p.playerId === player?.playerId) || player;
  const currentScore = currentPlayer?.score ?? score;
  const currentStreak = currentPlayer?.currentStreak ?? currentPlayer?.streak ?? streak;
  const answeredCount = effectivePlayers.filter((p) => p.hasAnswered || p.answeredCurrentRound).length;
  const totalFighters = effectivePlayers.length || 1;

  // Track the question index that was last initialized to avoid wiping selection on background polling
  const lastHandledQIndexRef = useRef(null);

  // Sync with Gateway WebSocket / REST state when a question arrives
  useEffect(() => {
    if (!serverQuestion) return;

    const qIndex = serverQuestionIndex || serverQuestion.questionIndex || 1;
    setActiveQuestion(serverQuestion);
    setCurrentRound(qIndex);
    setTotalRounds(serverTotalQuestions || 10);

    // ONLY reset player input and round state if this is truly a NEW question!
    if (lastHandledQIndexRef.current !== qIndex) {
      lastHandledQIndexRef.current = qIndex;
      const limit = serverQuestion.timeLimitSec || 20;
      activeTimeLimitRef.current = limit;
      questionStartTimeRef.current = Date.now();
      setSecondsLeft(limit);
      setTimerPercentage(100);
      setSelectedOption(null);
      setIsAnswerLocked(false);
      setRoundOver(false);
      setRoundFeedback(null);
    }
  }, [serverQuestion, serverQuestionIndex, serverTotalQuestions]);

  // Smooth, continuous high-precision countdown timer (100ms interval)
  useEffect(() => {
    if (roundOver) return;

    const interval = setInterval(() => {
      const limit = activeTimeLimitRef.current || 20;
      const elapsedSec = (Date.now() - questionStartTimeRef.current) / 1000;
      const remainingSec = Math.max(0, limit - elapsedSec);
      const remainingInt = Math.ceil(remainingSec);

      setSecondsLeft((prev) => {
        if (prev !== remainingInt && remainingInt <= 5 && remainingInt > 0) {
          sfx.playTick();
        }
        return remainingInt;
      });

      setTimerPercentage(Math.max(0, Math.min(100, (remainingSec / limit) * 100)));

      if (remainingSec <= 0) {
        clearInterval(interval);
        setRoundOver(true);
      }
    }, 100);

    return () => clearInterval(interval);
  }, [roundOver]);

  // Synchronize local timer with authoritative server countdown ticks
  useEffect(() => {
    if (serverSecondsRemaining !== undefined && serverSecondsRemaining !== null && !roundOver) {
      const limit = activeTimeLimitRef.current || 20;
      const currentCalculated = Math.max(0, limit - (Date.now() - questionStartTimeRef.current) / 1000);
      if (Math.abs(serverSecondsRemaining - currentCalculated) >= 1.5) {
        questionStartTimeRef.current = Date.now() - (limit - serverSecondsRemaining) * 1000;
      }
    }
  }, [serverSecondsRemaining, roundOver]);

  useEffect(() => {
    if (serverRoundResult) {
      setRoundOver(true);
      setRoundFeedback(serverRoundResult);
      if (serverRoundResult.correctOption === selectedOption) {
        sfx.playCorrect();
      } else {
        sfx.playWrong();
      }
    }
  }, [serverRoundResult, selectedOption]);

  // Automatically navigate to Results page when match finishes or summary arrives
  useEffect(() => {
    if (gameSummary || room?.state === 'FINISHED') {
      navigate('/results');
    }
  }, [gameSummary, room?.state, navigate]);

  // If final round is completed, automatically transition to results after 3.5s
  useEffect(() => {
    if (currentRound >= totalRounds && roundOver) {
      const timer = setTimeout(() => {
        navigate('/results');
      }, 3500);
      return () => clearTimeout(timer);
    }
  }, [currentRound, totalRounds, roundOver, navigate]);

  // Conclude round locally if timer hits 0 and server hasn't closed it yet
  useEffect(() => {
    if (secondsLeft === 0 && !roundOver) {
      setRoundOver(true);
      if (!isAnswerLocked) {
        setIsAnswerLocked(true);
        sfx.playWrong();
      }

      if (!serverQuestion) {
        const isCorrect = selectedOption === activeQuestion.correctOption;
        const timeLimit = activeQuestion.timeLimitSec || 20;
        const basePts = isCorrect ? 500 : 0;
        const speedBonus = isCorrect ? Math.round(500 * (secondsLeft / timeLimit)) : 0;
        const pts = isCorrect ? basePts + speedBonus : 0;

        if (isCorrect) {
          sfx.playCorrect();
          setScore((prev) => prev + pts);
          setStreak((prev) => prev + 1);
        } else {
          sfx.playWrong();
          setStreak(0);
        }

        setRoundFeedback({
          correctOption: activeQuestion.correctOption,
          explanation: activeQuestion.explanation,
          pointsAwarded: pts,
          basePoints: basePts,
          speedBonus: speedBonus,
          isCorrect,
        });
      }
    }
  }, [secondsLeft, roundOver, isAnswerLocked, serverQuestion, selectedOption, activeQuestion]);

  const handleSelectOption = (letter) => {
    if (isAnswerLocked || roundOver) return;

    setSelectedOption(letter);
    setIsAnswerLocked(true);
    sfx.playClick();

    const limit = activeQuestion.timeLimitSec || 20;
    const elapsedMs = Math.max(0, (limit - secondsLeft) * 1000);

    if (serverSubmitAnswer) {
      serverSubmitAnswer(letter, elapsedMs);
    }
  };

  const handleNextSimulation = () => {
    sfx.playClick();
    if (currentRound < SAMPLE_QUESTIONS.length) {
      const nextIndex = currentRound;
      setActiveQuestion(SAMPLE_QUESTIONS[nextIndex]);
      setCurrentRound(nextIndex + 1);
      setSecondsLeft(20);
      setSelectedOption(null);
      setIsAnswerLocked(false);
      setRoundOver(false);
      setRoundFeedback(null);
    } else {
      navigate('/results');
    }
  };

  const rankings = serverLeaderboard && serverLeaderboard.length > 0
    ? serverLeaderboard
    : effectivePlayers.length > 0
      ? [...effectivePlayers].sort((a, b) => (b.score || 0) - (a.score || 0))
      : (player
          ? [{ playerId: player.playerId, nickname: player.nickname, score: currentScore, streak: currentStreak, avatar: player.avatar || 'terminal' }]
          : []);

  return (
    <div className="min-h-[calc(100vh-80px)] bg-gradient-to-b from-[#26005C] via-[#160033] to-[#160033] py-6 px-4 sm:px-6 lg:px-8 select-none">
      <div className="max-w-5xl mx-auto space-y-6">
        {/* HUD Top Bar */}
        <div className="p-4 bg-[#C9BDF5] border-4 border-[#160033] shadow-[6px_6px_0px_#160033] flex flex-wrap items-center justify-between gap-4">
          {/* Round & Category Info */}
          <div className="flex items-center gap-3">
            <span className="font-pixel text-base sm:text-lg text-[#160033] font-black">
              ROUND {String(currentRound).padStart(2, '0')} / {totalRounds}
            </span>
            <Badge variant="purple" className="text-xs">
              {room?.config?.categoryName || 'PROGRAMMING'}
            </Badge>
            <Badge variant="yellow" className="text-xs">
              {room?.config?.difficulty || 'MEDIUM'}
            </Badge>
            {totalFighters > 1 && (
              <Badge variant="pink" className="text-xs">
                {answeredCount}/{totalFighters} ANSWERED
              </Badge>
            )}
          </div>

          {/* Player Live Score & Streak Display */}
          <div className="flex items-center gap-4">
            {/* Streak Multiplier */}
            <div className="flex items-center gap-1 font-arcade text-xs font-black px-3 py-1 bg-[#160033] text-[#FFE45C] border-2 border-[#160033] shadow-[2px_2px_0px_#160033]">
              <Flame className="w-4 h-4 text-[#FF62C7] animate-bounce" />
              <span>{currentStreak}x COMBO</span>
            </div>

            {/* Score Box */}
            <div className="px-3 py-1 bg-[#38E5D1] text-[#160033] border-2 border-[#160033] font-pixel text-xs sm:text-sm font-black shadow-[2px_2px_0px_#160033]">
              SCORE: {currentScore} PTS
            </div>

            {/* View Live Leaderboard Toggle Button */}
            <Button
              variant="dark"
              size="sm"
              onClick={() => setShowLeaderboardModal(true)}
              icon={<BarChart2 className="w-4 h-4" />}
            >
              STANDINGS
            </Button>
          </div>
        </div>

        {/* Countdown Timer Bar */}
        <CountdownTimer
          secondsRemaining={secondsLeft}
          totalSeconds={activeTimeLimitRef.current || 20}
          percentage={timerPercentage}
        />

        {/* Main Question Arena Card */}
        <div className="p-6 md:p-8 bg-[#160033] border-4 border-[#A72CFF] shadow-[8px_8px_0px_#160033] relative">
          <div className="flex items-start justify-between gap-4 mb-4">
            <span className="font-arcade text-xs text-[#FFE45C] uppercase font-bold tracking-wider">
              QUESTION #{currentRound}
            </span>
            {isAnswerLocked && !roundOver && (
              <Badge variant="yellow" className="text-xs animate-pulse">
                ANSWER LOCKED IN
              </Badge>
            )}
          </div>

          {/* Question Text */}
          <h2 className="font-arcade text-lg sm:text-xl md:text-2xl font-bold text-[#F5EEFF] leading-relaxed mb-6">
            {activeQuestion.questionText}
          </h2>

          {/* Code Snippet Block (if available) with CRT scanlines */}
          {activeQuestion.codeSnippet && (
            <div className="mb-6 bg-[#0a001a] border-3 border-[#160033] shadow-[4px_4px_0px_#160033] p-4 font-terminal text-base md:text-xl text-[#38E5D1] crt-scanlines overflow-x-auto">
              <pre className="font-terminal whitespace-pre-wrap">{activeQuestion.codeSnippet}</pre>
            </div>
          )}

          {/* 4 Answer Options Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <AnswerOption
              letter="A"
              text={activeQuestion.optionA}
              isSelected={selectedOption === 'A'}
              isCorrect={roundOver && activeQuestion.correctOption === 'A'}
              isLocked={isAnswerLocked || roundOver}
              isRoundEnded={roundOver}
              onClick={handleSelectOption}
            />
            <AnswerOption
              letter="B"
              text={activeQuestion.optionB}
              isSelected={selectedOption === 'B'}
              isCorrect={roundOver && activeQuestion.correctOption === 'B'}
              isLocked={isAnswerLocked || roundOver}
              isRoundEnded={roundOver}
              onClick={handleSelectOption}
            />
            <AnswerOption
              letter="C"
              text={activeQuestion.optionC}
              isSelected={selectedOption === 'C'}
              isCorrect={roundOver && activeQuestion.correctOption === 'C'}
              isLocked={isAnswerLocked || roundOver}
              isRoundEnded={roundOver}
              onClick={handleSelectOption}
            />
            <AnswerOption
              letter="D"
              text={activeQuestion.optionD}
              isSelected={selectedOption === 'D'}
              isCorrect={roundOver && activeQuestion.correctOption === 'D'}
              isLocked={isAnswerLocked || roundOver}
              isRoundEnded={roundOver}
              onClick={handleSelectOption}
            />
          </div>
        </div>

        {/* Round Feedback Drawer (Appears when round ends) */}
        {roundOver && (
          <div className="p-5 bg-[#C9BDF5] border-4 border-[#160033] shadow-[6px_6px_0px_#160033] text-[#160033] space-y-3 animate-fade-in">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div className="flex flex-wrap items-center gap-2">
                <span className="font-pixel text-sm md:text-base font-black">
                  {(selectedOption === (roundFeedback?.correctOption || activeQuestion.correctOption))
                    ? '🎉 CORRECT ANSWER!'
                    : '💥 INCORRECT / ROUND ENDED'}
                </span>
                {roundFeedback?.pointsAwarded > 0 ? (
                  <>
                    <span className="px-2 py-0.5 bg-[#4DEEEA] border-2 border-[#160033] font-pixel text-xs font-black">
                      +500 BASE
                    </span>
                    {roundFeedback.speedBonus > 0 && (
                      <span className="px-2 py-0.5 bg-[#FF62C7] text-white border-2 border-[#160033] font-pixel text-xs font-black">
                        +{roundFeedback.speedBonus} SPEED
                      </span>
                    )}
                    <span className="px-2 py-0.5 bg-[#FFE45C] border-2 border-[#160033] font-pixel text-xs font-black">
                      TOTAL: +{roundFeedback.pointsAwarded} PTS
                    </span>
                  </>
                ) : (
                  <span className="px-2 py-0.5 bg-[#FF4567] text-white border-2 border-[#160033] font-pixel text-xs font-black">
                    +0 PTS
                  </span>
                )}
                <span className="px-2 py-0.5 bg-[#2C0066] text-[#FFE45C] border-2 border-[#160033] font-pixel text-xs font-black">
                  KEY: OPTION {roundFeedback?.correctOption || activeQuestion.correctOption}
                </span>
              </div>

              {currentRound >= totalRounds ? (
                <Button
                  variant="yellow"
                  size="md"
                  onClick={() => navigate('/results')}
                  icon={<Trophy className="w-4 h-4" />}
                  className="animate-bounce"
                >
                  FINAL PODIUM & RESULTS ➔
                </Button>
              ) : !serverQuestion ? (
                <Button
                  variant="yellow"
                  size="md"
                  onClick={handleNextSimulation}
                  icon={<ChevronRight className="w-4 h-4" />}
                >
                  NEXT QUESTION ➔
                </Button>
              ) : null}
            </div>

            {(roundFeedback?.explanation || activeQuestion.explanation) && (
              <p className="font-arcade text-xs md:text-sm text-[#26005C] font-semibold leading-relaxed border-t-2 border-[#160033]/20 pt-2">
                💡 <span className="font-bold">EXPLANATION:</span> {roundFeedback?.explanation || activeQuestion.explanation}
              </p>
            )}

            {/* Quick Round Standings Peek */}
            {rankings.length > 0 && (
              <div className="border-t-2 border-[#160033]/20 pt-2 flex flex-wrap items-center justify-between gap-2">
                <div className="flex items-center gap-2">
                  <span className="font-pixel text-xs font-black text-[#160033]">
                    🏆 ROUND LEADER: {rankings[0]?.nickname} ({rankings[0]?.score || 0} PTS)
                  </span>
                  {rankings[0]?.rankChange > 0 && (
                    <span className="px-1 py-0.2 bg-[#22c55e]/25 border border-[#22c55e] text-[#16a34a] font-pixel text-[8px]">
                      ▲ +{rankings[0].rankChange}
                    </span>
                  )}
                </div>
                <button
                  onClick={() => setShowLeaderboardModal(true)}
                  className="font-arcade text-xs font-black text-[#26005C] hover:text-[#160033] underline cursor-pointer"
                >
                  VIEW FULL LIVE LEADERBOARD ({rankings.length} FIGHTERS) ➔
                </button>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Standings Modal Popup */}
      {showLeaderboardModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-[#160033]/85 backdrop-blur-xs">
          <div className="relative w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <button
              onClick={() => setShowLeaderboardModal(false)}
              className="absolute top-2 right-2 z-10 w-8 h-8 bg-[#FF62C7] border-2 border-[#160033] flex items-center justify-center text-[#160033] font-black cursor-pointer shadow-[2px_2px_0px_#160033] hover:scale-105"
            >
              ✕
            </button>
            <BattleLeaderboard
              rankings={rankings}
              currentPlayerId={player?.playerId}
            />
          </div>
        </div>
      )}
    </div>
  );
};
