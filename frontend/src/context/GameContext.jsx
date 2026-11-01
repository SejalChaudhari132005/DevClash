import React, { createContext, useContext, useState, useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { sfx } from '../utils/audio';

const STORAGE_KEY = 'devclash_session';

export const normalizePlayer = (p) => {
  if (!p) return p;
  const isHost = Boolean(p.isHost || p.host);
  return {
    ...p,
    isHost,
    host: isHost,
  };
};

export const saveSession = (data) => {
  try {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(data));
  } catch (e) {
    console.warn('Failed to save session to sessionStorage', e);
  }
};

export const loadSession = () => {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch (e) {
    return null;
  }
};

export const clearSession = () => {
  try {
    sessionStorage.removeItem(STORAGE_KEY);
  } catch (e) {}
};

export const getGatewayUrl = () => {
  if (import.meta.env.VITE_GATEWAY_URL) {
    return import.meta.env.VITE_GATEWAY_URL;
  }
  if (typeof window !== 'undefined' && window.location) {
    const isHttps = window.location.protocol === 'https:';
    if (window.location.port === '' || window.location.port === '80' || window.location.port === '443') {
      return `${window.location.protocol}//${window.location.host}`;
    }
    const protocol = isHttps ? 'https:' : 'http:';
    return `${protocol}//${window.location.hostname}:8085`;
  }
  return 'http://localhost:8085';
};

export const getGatewayWsUrl = () => {
  if (import.meta.env.VITE_GATEWAY_WS_URL) {
    return import.meta.env.VITE_GATEWAY_WS_URL;
  }
  if (typeof window !== 'undefined' && window.location) {
    const isHttps = window.location.protocol === 'https:';
    const protocol = isHttps ? 'https:' : 'http:';
    if (window.location.port === '' || window.location.port === '80' || window.location.port === '443') {
      return `${protocol}//${window.location.host}/ws-devclash`;
    }
    return `${protocol}//${window.location.hostname}:8085/ws-devclash`;
  }
  return 'http://localhost:8085/ws-devclash';
};

const GameContext = createContext(null);

export const GameProvider = ({ children }) => {
  const initialSession = loadSession();

  const [client, setClient] = useState(null);
  const [isConnected, setIsConnected] = useState(false);
  const [room, setRoom] = useState(initialSession?.room || null);
  const [player, setPlayer] = useState(initialSession?.player ? normalizePlayer(initialSession.player) : null);
  const [sessionToken, setSessionToken] = useState(initialSession?.sessionToken || null);
  const [players, setPlayers] = useState(
    initialSession?.room?.players?.map(normalizePlayer) ||
    (initialSession?.player ? [normalizePlayer(initialSession.player)] : [])
  );
  const [currentQuestion, setCurrentQuestion] = useState(null);
  const [questionIndex, setQuestionIndex] = useState(1);
  const [totalQuestions, setTotalQuestions] = useState(10);
  const [secondsRemaining, setSecondsRemaining] = useState(20);
  const [roundResult, setRoundResult] = useState(null);
  const [leaderboard, setLeaderboard] = useState([]);
  const [gameSummary, setGameSummary] = useState(null);
  const [isGameStarted, setIsGameStarted] = useState(false);
  const [errorMsg, setErrorMsg] = useState(null);

  const clientRef = useRef(null);
  const currentRoomCode = useRef(initialSession?.roomCode || initialSession?.room?.roomCode || null);
  const playerRef = useRef(player);
  const subscriptionsRef = useRef(new Map());

  useEffect(() => {
    playerRef.current = player;
  }, [player]);

  const subscribeToRoom = (stompClient, roomCode) => {
    if (!stompClient || !stompClient.connected || !roomCode) return;
    const cleanCode = roomCode.trim().toUpperCase();
    currentRoomCode.current = cleanCode;

    // Avoid duplicate subscriptions
    if (subscriptionsRef.current.has(cleanCode)) {
      return;
    }

    console.log(`[DevClash] Subscribing to /topic/room/${cleanCode}`);
    const sub = stompClient.subscribe(`/topic/room/${cleanCode}`, (message) => {
      try {
        const event = JSON.parse(message.body);
        handleServerEvent(event);
      } catch (e) {
        console.error('Failed to parse incoming message:', e);
      }
    });

    subscriptionsRef.current.set(cleanCode, sub);
  };

  useEffect(() => {
    const wsUrl = getGatewayWsUrl();

    // Use SockJS factory directly without brokerURL override so fallback and Cloudflare tunnels work reliably
    const stompClient = new Client({
      webSocketFactory: () => new SockJS(wsUrl),
      reconnectDelay: 2000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: (msg) => {
        // console.log('[STOMP]', msg);
      },
      onConnect: () => {
        console.log('[DevClash STOMP] Connected to Gateway via SockJS');
        setIsConnected(true);
        subscriptionsRef.current.clear();

        const session = loadSession();
        const codeToSub = currentRoomCode.current || session?.roomCode || session?.room?.roomCode;
        if (codeToSub) {
          subscribeToRoom(stompClient, codeToSub);
        }
      },
      onDisconnect: () => {
        console.warn('[DevClash STOMP] Disconnected from Gateway');
        setIsConnected(false);
        subscriptionsRef.current.clear();
      },
      onStompError: (frame) => {
        console.error('[DevClash STOMP] Broker error:', frame.headers['message']);
      },
      onWebSocketError: (err) => {
        console.warn('[DevClash STOMP] WebSocket transport warning:', err);
      },
    });

    stompClient.activate();
    clientRef.current = stompClient;
    setClient(stompClient);

    return () => {
      stompClient.deactivate();
    };
  }, []);

  // Background interval sync (2.5s) to guarantee mobile devices with packet loss or sleep stay updated
  useEffect(() => {
    const code = currentRoomCode.current || room?.roomCode || loadSession()?.roomCode;
    if (!code) return;
    const interval = setInterval(() => {
      refreshRoomState(code);
    }, 2500);
    return () => clearInterval(interval);
  }, [room?.roomCode]);

  const handleServerEvent = (event) => {
    const { eventType, payload } = event;
    console.log('[DevClash Event]', eventType, payload);

    switch (eventType) {
      case 'ROOM_CREATED':
      case 'JOIN_SUCCESS': {
        if (payload.room) {
          setRoom(payload.room);
          if (payload.room.players) {
            setPlayers(payload.room.players.map(normalizePlayer));
          }
        }
        if (payload.player) {
          const normP = normalizePlayer(payload.player);
          setPlayer(normP);
          saveSession({
            roomCode: payload.room?.roomCode || currentRoomCode.current,
            room: payload.room,
            player: normP,
            sessionToken: payload.sessionToken || loadSession()?.sessionToken,
          });
        } else if (payload.playerId) {
          setPlayer((prev) => {
            const isHost = payload.room?.hostPlayerId === payload.playerId || prev?.isHost;
            const updated = normalizePlayer({
              ...(prev || {}),
              playerId: payload.playerId,
              nickname: payload.room?.hostNickname || prev?.nickname,
              isHost,
              host: isHost,
            });
            saveSession({
              roomCode: payload.room?.roomCode || currentRoomCode.current,
              room: payload.room,
              player: updated,
              sessionToken: payload.sessionToken || loadSession()?.sessionToken,
            });
            return updated;
          });
        }
        if (payload.sessionToken) setSessionToken(payload.sessionToken);
        break;
      }

      case 'ROOM_UPDATED': {
        if (payload.room) {
          setRoom(payload.room);
          if (payload.room.players) {
            setPlayers(payload.room.players.map(normalizePlayer));
          }
          if (payload.room.state === 'COUNTDOWN' || payload.room.state === 'QUESTION_ACTIVE') {
            setIsGameStarted(true);
          }
          // Reconcile current player identity
          setPlayer((prev) => {
            if (!prev) return prev;
            const isHost = Boolean(
              prev.isHost ||
              prev.host ||
              (payload.room.hostPlayerId && prev.playerId === payload.room.hostPlayerId)
            );
            const updated = { ...prev, isHost, host: isHost };
            saveSession({
              roomCode: payload.room.roomCode,
              room: payload.room,
              player: updated,
              sessionToken: loadSession()?.sessionToken,
            });
            return updated;
          });
        }
        break;
      }

      case 'PLAYER_JOINED': {
        sfx.playClick();
        const rawPlayer = payload.player || (payload.playerId ? payload : null);
        if (payload.room?.players) {
          setPlayers(payload.room.players.map(normalizePlayer));
          setRoom(payload.room);
        } else if (rawPlayer) {
          const joinedPlayer = normalizePlayer(rawPlayer);
          setPlayers((prev) => {
            const exists = prev.some((p) => p.playerId === joinedPlayer.playerId);
            return exists
              ? prev.map((p) => (p.playerId === joinedPlayer.playerId ? joinedPlayer : p))
              : [...prev, joinedPlayer];
          });
        }
        if (payload.room) {
          setRoom(payload.room);
        }
        break;
      }

      case 'PLAYER_LEFT': {
        if (payload.room?.players) {
          setPlayers(payload.room.players.map(normalizePlayer));
          setRoom(payload.room);
        } else {
          setPlayers((prev) => prev.filter((p) => p.playerId !== payload.playerId));
        }
        if (payload.room) {
          setRoom(payload.room);
        }

        // Check if the current client was kicked
        if (playerRef.current && payload.playerId === playerRef.current.playerId) {
          if (payload.kicked) {
            clearSession();
            alert('You have been removed from the room by the host.');
            window.location.href = '/';
          }
        }
        break;
      }

      case 'PLAYER_READY':
      case 'PLAYER_READY_CHANGED': {
        if (payload.room?.players) {
          setPlayers(payload.room.players.map(normalizePlayer));
          setRoom(payload.room);
        } else {
          setPlayers((prev) =>
            prev.map((p) =>
              p.playerId === payload.playerId
                ? { ...p, ready: payload.ready, connected: payload.connected ?? p.connected }
                : p
            )
          );
        }
        if (payload.room) {
          setRoom(payload.room);
        }
        break;
      }

      case 'PLAYER_DISCONNECTED': {
        if (payload.room?.players) {
          setPlayers(payload.room.players.map(normalizePlayer));
          setRoom(payload.room);
        } else {
          setPlayers((prev) =>
            prev.map((p) =>
              p.playerId === payload.playerId ? { ...p, connected: false } : p
            )
          );
        }
        break;
      }

      case 'GAME_STARTED':
      case 'GAME_STARTING': {
        setIsGameStarted(true);
        if (payload.question) {
          setCurrentQuestion(payload.question);
          setQuestionIndex(payload.question.questionIndex || 1);
          setTotalQuestions(payload.question.totalQuestions || 10);
          setSecondsRemaining(payload.question.timeLimitSec || 20);
        }
        if (payload.room) {
          setRoom(payload.room);
        }
        setRoundResult(null);
        sfx.playVictory();
        break;
      }

      case 'QUESTION_STARTED': {
        setIsGameStarted(true);
        setCurrentQuestion(payload.question);
        setQuestionIndex(payload.questionIndex);
        setTotalQuestions(payload.totalQuestions);
        setSecondsRemaining(payload.timeLimitSec);
        setRoundResult(null);
        setPlayers((prev) =>
          prev.map((p) => ({ ...p, hasAnswered: false, answeredCurrentRound: false }))
        );
        sfx.playTick();
        break;
      }

      case 'TIMER_UPDATE':
      case 'TIMER_TICK': {
        const sec = payload.secondsRemaining !== undefined ? payload.secondsRemaining : payload.timeRemaining;
        setSecondsRemaining(sec);
        if (sec <= 5 && sec > 0) {
          sfx.playTick();
        }
        break;
      }

      case 'ANSWER_SUBMITTED': {
        setPlayers((prev) =>
          prev.map((p) =>
            p.playerId === payload.playerId
              ? { ...p, hasAnswered: true, answeredCurrentRound: true }
              : p
          )
        );
        break;
      }

      case 'SCORE_UPDATED': {
        setPlayers((prev) =>
          prev.map((p) =>
            p.playerId === payload.playerId
              ? { ...p, score: payload.score ?? payload.newScore, currentStreak: payload.streak }
              : p
          )
        );
        break;
      }

      case 'QUESTION_ENDED': {
        setRoundResult(payload);
        if (payload.updatedScores) {
          setPlayers((prev) =>
            prev.map((p) => {
              const updated = payload.updatedScores.find((s) => s.playerId === p.playerId);
              return updated
                ? { ...p, score: updated.score, currentStreak: updated.streak, rank: updated.rank }
                : p;
            })
          );
        }
        sfx.playCorrect();
        break;
      }

      case 'LEADERBOARD_UPDATED': {
        setLeaderboard(payload);
        break;
      }

      case 'GAME_FINISHED': {
        setGameSummary(payload);
        setIsGameStarted(false);
        sfx.playVictory();
        break;
      }

      case 'ERROR': {
        setErrorMsg(payload.error || 'Server error occurred');
        break;
      }

      default:
        break;
    }
  };

  const createRoom = async (nickname, avatar, config) => {
    const gatewayUrl = getGatewayUrl();
    try {
      const response = await fetch(`${gatewayUrl}/api/quiz/room/create`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nickname, avatar, config }),
      });

      if (!response.ok) {
        const err = await response.json();
        throw new Error(err.error || 'Failed to create room via Gateway');
      }

      const roomData = await response.json();
      const hostP = normalizePlayer({
        playerId: roomData.hostPlayerId,
        nickname: roomData.hostNickname || nickname,
        avatar: avatar,
        isHost: true,
        host: true,
        ready: true,
        connected: true,
      });

      setRoom(roomData);
      setPlayer(hostP);
      setSessionToken(roomData.hostSessionToken);
      const initialPlayers = (roomData.players || [hostP]).map(normalizePlayer);
      setPlayers(initialPlayers);

      saveSession({
        roomCode: roomData.roomCode,
        room: roomData,
        player: hostP,
        sessionToken: roomData.hostSessionToken,
      });

      if (clientRef.current?.connected) {
        subscribeToRoom(clientRef.current, roomData.roomCode);
      } else {
        currentRoomCode.current = roomData.roomCode;
      }

      return roomData;
    } catch (e) {
      setErrorMsg(e.message);
      throw e;
    }
  };

  const joinRoom = async (roomCode, nickname, avatar) => {
    const cleanCode = roomCode.trim().toUpperCase();
    const gatewayUrl = getGatewayUrl();

    if (clientRef.current?.connected) {
      subscribeToRoom(clientRef.current, cleanCode);
    } else {
      currentRoomCode.current = cleanCode;
    }

    try {
      const response = await fetch(`${gatewayUrl}/api/quiz/room/join`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ roomCode: cleanCode, nickname, avatar }),
      });

      const result = await response.json();
      if (!result.success) {
        throw new Error(result.message || 'Cannot join room.');
      }

      const normPlayers = (result.room?.players || []).map(normalizePlayer);
      const me = normPlayers.find((p) => p.playerId === result.playerId) || normalizePlayer({
        playerId: result.playerId,
        nickname: nickname,
        avatar: avatar,
        isHost: false,
        host: false,
        ready: false,
        connected: true,
      });

      setRoom(result.room);
      setPlayer(me);
      setSessionToken(result.sessionToken);
      setPlayers(normPlayers);

      saveSession({
        roomCode: cleanCode,
        room: result.room,
        player: me,
        sessionToken: result.sessionToken,
      });

      return result;
    } catch (e) {
      setErrorMsg(e.message);
      throw e;
    }
  };

  const toggleReady = async (isReady) => {
    const session = loadSession();
    const currentCode = room?.roomCode || session?.roomCode || currentRoomCode.current;
    const currentPId = player?.playerId || session?.player?.playerId;
    if (!currentCode || !currentPId) return;

    const gatewayUrl = getGatewayUrl();

    // Optimistically update local player state
    setPlayers((prev) =>
      prev.map((p) => (p.playerId === currentPId ? { ...p, ready: isReady } : p))
    );
    setPlayer((prev) => {
      const updated = { ...(prev || {}), ready: isReady };
      saveSession({
        roomCode: currentCode,
        room: room || session?.room,
        player: updated,
        sessionToken: sessionToken || session?.sessionToken,
      });
      return updated;
    });

    try {
      if (clientRef.current?.connected) {
        clientRef.current.publish({
          destination: '/app/room.ready',
          body: JSON.stringify({
            roomCode: currentCode,
            playerId: currentPId,
            ready: isReady,
          }),
        });
      }
      await fetch(`${gatewayUrl}/api/quiz/room/ready`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          roomCode: currentCode,
          playerId: currentPId,
          ready: isReady,
        }),
      });
    } catch (e) {
      console.error('toggleReady error:', e);
    }
  };

  const removePlayer = async (targetPlayerId) => {
    const session = loadSession();
    const currentCode = room?.roomCode || session?.roomCode || currentRoomCode.current;
    const hostId = player?.playerId || session?.player?.playerId;
    if (!currentCode || !hostId) return;

    const gatewayUrl = getGatewayUrl();

    try {
      if (clientRef.current?.connected) {
        clientRef.current.publish({
          destination: '/app/room.remove',
          body: JSON.stringify({
            roomCode: currentCode,
            hostPlayerId: hostId,
            targetPlayerId,
          }),
        });
      }
      await fetch(`${gatewayUrl}/api/quiz/room/remove`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          roomCode: currentCode,
          hostPlayerId: hostId,
          targetPlayerId,
        }),
      });
    } catch (e) {
      setErrorMsg(e.message);
    }
  };

  const leaveRoom = async (overrideRoomCode, overridePlayerId) => {
    const session = loadSession();
    const targetRoomCode = (
      overrideRoomCode ||
      room?.roomCode ||
      session?.roomCode ||
      currentRoomCode.current ||
      ''
    ).trim().toUpperCase();
    const targetPlayerId = overridePlayerId || player?.playerId || session?.player?.playerId;
    const gatewayUrl = getGatewayUrl();

    if (targetRoomCode && targetPlayerId) {
      try {
        if (clientRef.current?.connected) {
          clientRef.current.publish({
            destination: '/app/room.leave',
            body: JSON.stringify({
              roomCode: targetRoomCode,
              playerId: targetPlayerId,
            }),
          });
        }
        await fetch(`${gatewayUrl}/api/quiz/room/leave`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            roomCode: targetRoomCode,
            playerId: targetPlayerId,
          }),
        });
      } catch (e) {
        console.error('leaveRoom error:', e);
      }
    }

    if (targetRoomCode && subscriptionsRef.current.has(targetRoomCode)) {
      try {
        subscriptionsRef.current.get(targetRoomCode).unsubscribe();
      } catch (err) {}
      subscriptionsRef.current.delete(targetRoomCode);
    }

    clearSession();
    currentRoomCode.current = null;
    setRoom(null);
    setPlayer(null);
    setPlayers([]);
    setCurrentQuestion(null);
    setRoundResult(null);
    setGameSummary(null);
    setIsGameStarted(false);
  };

  const resetGame = () => {
    setCurrentQuestion(null);
    setQuestionIndex(1);
    setRoundResult(null);
    setGameSummary(null);
    setIsGameStarted(false);
  };

  const startGame = async () => {
    const session = loadSession();
    const targetRoomCode = room?.roomCode || session?.roomCode || currentRoomCode.current;
    const targetHostId = player?.playerId || session?.player?.playerId;
    if (!targetRoomCode || !targetHostId) return;

    const gatewayUrl = getGatewayUrl();

    try {
      const response = await fetch(`${gatewayUrl}/api/quiz/room/start`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          roomCode: targetRoomCode,
          hostPlayerId: targetHostId,
        }),
      });

      if (!response.ok) {
        const data = await response.json();
        throw new Error(data.error || 'Cannot start match.');
      }
      setIsGameStarted(true);
    } catch (e) {
      setErrorMsg(e.message);
      throw e;
    }
  };

  const submitAnswer = async (selectedOption, responseTimeMs) => {
    const session = loadSession();
    const targetRoomCode = (
      room?.roomCode ||
      session?.roomCode ||
      currentRoomCode.current ||
      ''
    ).trim().toUpperCase();
    const targetPlayerId = player?.playerId || session?.player?.playerId;
    if (!targetRoomCode || !targetPlayerId) return;

    const payload = {
      roomCode: targetRoomCode,
      playerId: targetPlayerId,
      questionIndex,
      selectedOption,
      responseTimeMs: responseTimeMs || 1000,
    };

    // 1. Dual dispatch via STOMP WebSocket if connected
    if (clientRef.current?.connected) {
      try {
        clientRef.current.publish({
          destination: '/app/game.answer',
          body: JSON.stringify(payload),
        });
      } catch (err) {
        console.warn('STOMP submitAnswer dispatch error:', err);
      }
    }

    // 2. Dual dispatch via REST API to guarantee answer delivery even on mobile or packet loss
    const gatewayUrl = getGatewayUrl();
    try {
      await fetch(`${gatewayUrl}/api/quiz/room/answer`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
    } catch (e) {
      console.warn('REST submitAnswer dispatch error:', e);
    }
  };

  const refreshRoomState = async (roomCode) => {
    if (!roomCode) return;
    const cleanCode = roomCode.trim().toUpperCase();
    const gatewayUrl = getGatewayUrl();

    // Guarantee WebSocket subscription to room topic
    if (clientRef.current?.connected) {
      subscribeToRoom(clientRef.current, cleanCode);
    } else {
      currentRoomCode.current = cleanCode;
    }

    try {
      const session = loadSession();
      const currentPId = player?.playerId || session?.player?.playerId || '';
      const response = await fetch(`${gatewayUrl}/api/quiz/room/${cleanCode}/state?playerId=${encodeURIComponent(currentPId)}`);
      if (response.ok) {
        setIsConnected(true);
        const data = await response.json();
        const rawPlayers = data.players || data.room?.players || [];
        const normPlayers = rawPlayers.map(normalizePlayer);
        setPlayers(normPlayers);

        const roomObj = data.room || {
          roomCode: cleanCode,
          hostPlayerId: data.hostPlayerId,
          hostNickname: data.hostNickname,
          players: normPlayers,
          config: data.state?.config || data.config,
        };
        setRoom(roomObj);

        if (data.currentQuestion || data.state?.currentQuestion) {
          const newQ = data.currentQuestion || data.state?.currentQuestion;
          setCurrentQuestion((prevQ) => {
            if (!prevQ || prevQ.questionIndex !== newQ.questionIndex || prevQ.questionText !== newQ.questionText) {
              return newQ;
            }
            return prevQ;
          });
        }

        if (data.currentQuestionIndex || data.state?.currentQuestionIndex) {
          setQuestionIndex(data.currentQuestionIndex || data.state?.currentQuestionIndex);
        }

        if (data.timeRemainingSec !== undefined && data.timeRemainingSec !== null && data.timeRemainingSec > 0) {
          setSecondsRemaining(data.timeRemainingSec);
        }

        if (data.currentLeaderboard || data.state?.currentLeaderboard) {
          setLeaderboard(data.currentLeaderboard || data.state?.currentLeaderboard);
        }

        const roomStateVal = data.state?.state || data.room?.state;
        if (roomStateVal === 'COUNTDOWN' || roomStateVal === 'QUESTION_ACTIVE') {
          setIsGameStarted(true);
        } else if (roomStateVal === 'FINISHED' || data.gameSummary) {
          setIsGameStarted(false);
          const sortedByPoints = [...normPlayers].sort((a, b) => {
            const diff = (Number(b.score ?? b.points ?? 0)) - (Number(a.score ?? a.points ?? 0));
            if (diff !== 0) return diff;
            return (Number(b.correctAnswers ?? 0)) - (Number(a.correctAnswers ?? 0));
          });
          const highestScorer = sortedByPoints[0];
          const finalSummary = data.gameSummary ? {
            ...data.gameSummary,
            winnerNickname: data.gameSummary.winnerNickname || highestScorer?.nickname || 'Champion',
            winnerScore: data.gameSummary.winnerScore !== undefined ? data.gameSummary.winnerScore : (highestScorer?.score || 0),
            fullRankings: (data.gameSummary.fullRankings || sortedByPoints).slice().sort((a, b) => (Number(b.score) || 0) - (Number(a.score) || 0)),
            finalPodium: (data.gameSummary.finalPodium || sortedByPoints).slice().sort((a, b) => (Number(b.score) || 0) - (Number(a.score) || 0)),
          } : {
            roomCode: cleanCode,
            winnerNickname: highestScorer?.nickname || 'Winner',
            winnerScore: highestScorer?.score || 0,
            fullRankings: sortedByPoints,
            finalPodium: sortedByPoints.slice(0, 3),
            totalQuestions: data.state?.config?.totalQuestions || 10,
          };
          setGameSummary(finalSummary);
        }

        // Reconcile and preserve current player identity and host status
        setPlayer((prevPlayer) => {
          const session = loadSession();
          const targetId = prevPlayer?.playerId || session?.player?.playerId;
          const found = normPlayers.find(
            (p) => p.playerId === targetId || (prevPlayer?.nickname && p.nickname === prevPlayer?.nickname)
          );

          if (found) {
            const isHost = Boolean(
              found.isHost ||
              found.host ||
              (roomObj && found.playerId === roomObj.hostPlayerId) ||
              (session?.player && (session.player.isHost || session.player.host))
            );
            const updated = { ...found, isHost, host: isHost };
            saveSession({
              roomCode: cleanCode,
              room: roomObj,
              player: updated,
              sessionToken: session?.sessionToken,
            });
            return updated;
          }

          if (prevPlayer) {
            const isHost = Boolean(
              prevPlayer.isHost ||
              prevPlayer.host ||
              (roomObj && prevPlayer.playerId === roomObj.hostPlayerId)
            );
            return { ...prevPlayer, isHost, host: isHost };
          }

          if (session?.player) {
            return normalizePlayer(session.player);
          }

          return null;
        });
      }
    } catch (e) {
      console.warn('refreshRoomState error:', e);
    }
  };

  const clearError = () => setErrorMsg(null);

  return (
    <GameContext.Provider
      value={{
        isConnected,
        room,
        player,
        players,
        sessionToken,
        currentQuestion,
        questionIndex,
        totalQuestions,
        secondsRemaining,
        roundResult,
        leaderboard,
        gameSummary,
        isGameStarted,
        errorMsg,
        createRoom,
        joinRoom,
        toggleReady,
        removePlayer,
        leaveRoom,
        resetGame,
        startGame,
        submitAnswer,
        refreshRoomState,
        clearError,
      }}
    >
      {children}
    </GameContext.Provider>
  );
};

export const useGame = () => useContext(GameContext);
