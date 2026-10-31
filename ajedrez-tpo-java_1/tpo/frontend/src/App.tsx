import React, { useCallback, useEffect, useMemo, useState } from 'react';
import type { ChessGameClient, GameState, PieceType, Position } from './types/chess';
import { HttpChessGameClient } from './services/HttpChessGameClient';
import { Board } from './components/Board';
import { Sidebar } from './components/Sidebar';
import { PromotionDialog } from './components/PromotionDialog';
import './index.css';

export const App: React.FC = () => {
  // Cliente del núcleo Java. La UI solo conoce la interfaz ChessGameClient.
  const gameClient: ChessGameClient = useMemo(() => new HttpChessGameClient(), []);

  const [gameState, setGameState] = useState<GameState | null>(null);
  const [connectionError, setConnectionError] = useState<string | null>(null);
  const [selectedPosition, setSelectedPosition] = useState<Position | null>(null);
  const [possibleMoves, setPossibleMoves] = useState<Position[]>([]);
  const [invalidAttempt, setInvalidAttempt] = useState<boolean>(false);
  const [busy, setBusy] = useState<boolean>(false);

  const loadState = useCallback(async () => {
    try {
      setGameState(await gameClient.getState());
      setConnectionError(null);
    } catch {
      setConnectionError('No se pudo conectar con el backend Java.');
    }
  }, [gameClient]);

  // Carga inicial del estado (se ignora la respuesta si el componente ya se desmontó).
  useEffect(() => {
    let cancelled = false;
    gameClient
      .getState()
      .then(state => {
        if (!cancelled) setGameState(state);
      })
      .catch(() => {
        if (!cancelled) setConnectionError('No se pudo conectar con el backend Java.');
      });
    return () => {
      cancelled = true;
    };
  }, [gameClient]);

  if (gameState === null) {
    return (
      <div className="chess-app">
        <main className="connection-screen">
          <div className="card connection-card">
            {connectionError ? (
              <>
                <h2>{connectionError}</h2>
                <p>Levantá el servidor desde la carpeta <code>backend</code>:</p>
                <pre>mvn compile exec:java</pre>
                <button className="btn btn-reset" onClick={() => void loadState()}>
                  Reintentar
                </button>
              </>
            ) : (
              <h2>Conectando con el servidor…</h2>
            )}
          </div>
        </main>
      </div>
    );
  }

  const { board, currentTurn, capturedPieces, check, checkmate, gameOver, promotion } = gameState;
  const isWhiteCheckmate = Boolean(checkmate?.white);
  const isBlackCheckmate = Boolean(checkmate?.black);
  const isGameOver = Boolean(gameOver || isWhiteCheckmate || isBlackCheckmate);
  const isPromotionPending = Boolean(promotion?.required);

  // El historial vive en el núcleo (state.history); la UI solo lo muestra.
  const moveHistory = gameState.history ?? [];

  const clearSelection = () => {
    setSelectedPosition(null);
    setPossibleMoves([]);
  };

  const selectPiece = async (pos: Position) => {
    setSelectedPosition(pos);
    try {
      setPossibleMoves(await gameClient.getPossibleDestinations(pos));
    } catch {
      setPossibleMoves([]);
    }
  };

  const handleSquareClick = async (pos: Position) => {
    if (busy || isGameOver || isPromotionPending) return;
    setInvalidAttempt(false);
    const clickedPiece = board[pos.row][pos.column];

    // Caso 1: hay una pieza seleccionada
    if (selectedPosition) {
      // Click en la misma casilla: deseleccionar
      if (selectedPosition.row === pos.row && selectedPosition.column === pos.column) {
        setSelectedPosition(null);
        setPossibleMoves([]);
        return;
      }

      // Click en otra pieza propia: cambiar la selección
      if (clickedPiece && clickedPiece.color === currentTurn) {
        await selectPiece(pos);
        return;
      }

      // Intentar el movimiento: la validación la hace SIEMPRE el núcleo Java
      setBusy(true);
      try {
        const result = await gameClient.makeMove(selectedPosition, pos);
        setGameState(result.state);
        if (result.success) {
          clearSelection();
        } else {
          setInvalidAttempt(true);
          setTimeout(() => setInvalidAttempt(false), 1200);
        }
      } catch {
        setConnectionError('Se perdió la conexión con el backend Java.');
        setGameState(null);
      } finally {
        setBusy(false);
      }
      return;
    }

    // Caso 2: sin selección previa, seleccionar una pieza del turno actual
    if (clickedPiece && clickedPiece.color === currentTurn) {
      await selectPiece(pos);
    }
  };

  const handlePromotion = async (type: PieceType) => {
    if (busy || !promotion?.required) return;
    setBusy(true);
    try {
      const result = await gameClient.promote(type);
      setGameState(result.state);
      if (!result.success) {
        setInvalidAttempt(true);
        setTimeout(() => setInvalidAttempt(false), 1200);
      }
    } catch {
      setConnectionError('Se perdió la conexión con el backend Java.');
      setGameState(null);
    } finally {
      setBusy(false);
    }
  };

  const handleResetGame = async () => {
    try {
      setGameState(await gameClient.resetGame());
      setSelectedPosition(null);
      setPossibleMoves([]);
      setInvalidAttempt(false);
    } catch {
      setConnectionError('Se perdió la conexión con el backend Java.');
      setGameState(null);
    }
  };

  // Deshacer/rehacer: la jugada la revierte el núcleo (CommandManager); la UI solo refresca.
  const handleUndo = async () => {
    if (busy || isPromotionPending) return;
    clearSelection();
    setBusy(true);
    try {
      const result = await gameClient.undo();
      setGameState(result.state);
      if (!result.success) {
        setInvalidAttempt(true);
        setTimeout(() => setInvalidAttempt(false), 1200);
      }
    } catch {
      setConnectionError('Se perdió la conexión con el backend Java.');
      setGameState(null);
    } finally {
      setBusy(false);
    }
  };

  const handleRedo = async () => {
    if (busy || isPromotionPending) return;
    clearSelection();
    setBusy(true);
    try {
      const result = await gameClient.redo();
      setGameState(result.state);
      if (!result.success) {
        setInvalidAttempt(true);
        setTimeout(() => setInvalidAttempt(false), 1200);
      }
    } catch {
      setConnectionError('Se perdió la conexión con el backend Java.');
      setGameState(null);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="chess-app">
      <main className="chess-main-layout">
        <section className="board-section">
          {invalidAttempt && (
            <div className="invalid-move-toast">⚠️ Movimiento inválido</div>
          )}

          <Board
            board={board}
            selectedPosition={selectedPosition}
            possibleMoves={possibleMoves}
            isWhiteInCheck={check.white}
            isBlackInCheck={check.black}
            isWhiteInCheckmate={isWhiteCheckmate}
            isBlackInCheckmate={isBlackCheckmate}
            onSquareClick={pos => void handleSquareClick(pos)}
          />
        </section>

        <section className="sidebar-section">
          <Sidebar
            currentTurn={currentTurn}
            capturedPieces={capturedPieces}
            isWhiteCheck={check.white}
            isBlackCheck={check.black}
            isWhiteCheckmate={isWhiteCheckmate}
            isBlackCheckmate={isBlackCheckmate}
            moveHistory={moveHistory}
            canUndo={Boolean(gameState.canUndo) && !busy}
            canRedo={Boolean(gameState.canRedo) && !busy}
            onUndo={() => void handleUndo()}
            onRedo={() => void handleRedo()}
            onReset={() => void handleResetGame()}
          />
        </section>
      </main>

      {promotion?.required && promotion.color && (
        <PromotionDialog
          color={promotion.color}
          options={promotion.options}
          disabled={busy}
          onSelect={type => void handlePromotion(type)}
          onReset={() => void handleResetGame()}
        />
      )}
    </div>
  );
};

export default App;
