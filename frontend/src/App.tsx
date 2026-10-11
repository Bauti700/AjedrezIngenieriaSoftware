import React, { useState, useMemo } from 'react';
import type { ChessGameAPI, Position, Piece } from './types/chess';
import { MockChessGame } from './services/MockChessGame';
import { Board } from './components/Board';
import { Sidebar } from './components/Sidebar';
import './index.css';

export const App: React.FC = () => {
  // Instancia de ChessGameAPI (desacoplada; fácilmente reemplazable por la implementación definitiva)
  const gameEngine: ChessGameAPI = useMemo(() => new MockChessGame(), []);

  const [boardState, setBoardState] = useState<(Piece | null)[][]>(() =>
    gameEngine.getBoard()
  );
  const [selectedPosition, setSelectedPosition] = useState<Position | null>(null);
  const [possibleMoves, setPossibleMoves] = useState<Position[]>([]);
  const [moveHistory, setMoveHistory] = useState<string[]>([]);
  const [invalidAttempt, setInvalidAttempt] = useState<boolean>(false);

  const currentTurn = gameEngine.getCurrentTurn();
  const capturedPieces = gameEngine.getCapturedPieces();
  const isWhiteCheck = gameEngine.isCheck('white');
  const isBlackCheck = gameEngine.isCheck('black');

  const files = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'];
  const ranks = ['8', '7', '6', '5', '4', '3', '2', '1'];

  const handleSquareClick = (pos: Position) => {
    setInvalidAttempt(false);
    const clickedPiece = boardState[pos.row][pos.column];

    // Caso 1: Hay una pieza seleccionada y hacemos click en un destino
    if (selectedPosition) {
      // Si hace click en la misma posición, deseleccionamos
      if (
        selectedPosition.row === pos.row &&
        selectedPosition.column === pos.column
      ) {
        setSelectedPosition(null);
        setPossibleMoves([]);
        return;
      }

      // Si hace click en otra pieza de su mismo color, cambiamos la selección
      if (clickedPiece && clickedPiece.color === currentTurn) {
        setSelectedPosition(pos);
        const nextMoves = gameEngine.getPossibleDestinations
          ? gameEngine.getPossibleDestinations(pos)
          : [];
        setPossibleMoves(nextMoves);
        return;
      }

      // Intentar realizar el movimiento a través de la API
      const success = gameEngine.makeMove(selectedPosition, pos);
      if (success) {
        const fromNotation = `${files[selectedPosition.column]}${ranks[selectedPosition.row]}`;
        const toNotation = `${files[pos.column]}${ranks[pos.row]}`;
        const movingPiece = boardState[selectedPosition.row][selectedPosition.column];
        const notation = `${movingPiece?.type.toUpperCase()} ${fromNotation} ➜ ${toNotation}`;

        setMoveHistory(prev => [notation, ...prev]);
        setBoardState(gameEngine.getBoard());
        setSelectedPosition(null);
        setPossibleMoves([]);
      } else {
        // Movimiento inválido (feedback visual)
        setInvalidAttempt(true);
        setTimeout(() => setInvalidAttempt(false), 1200);
      }
      return;
    }

    // Caso 2: No hay selección previa, seleccionar pieza del turno actual
    if (clickedPiece && clickedPiece.color === currentTurn) {
      setSelectedPosition(pos);
      const moves = gameEngine.getPossibleDestinations
        ? gameEngine.getPossibleDestinations(pos)
        : [];
      setPossibleMoves(moves);
    }
  };

  const handleResetGame = () => {
    if (gameEngine.resetGame) {
      gameEngine.resetGame();
    }
    setBoardState(gameEngine.getBoard());
    setSelectedPosition(null);
    setPossibleMoves([]);
    setMoveHistory([]);
    setInvalidAttempt(false);
  };

  return (
    <div className="chess-app">
      <main className="chess-main-layout">
        <section className="board-section">
          {invalidAttempt && (
            <div className="invalid-move-toast">
              ⚠️ Movimiento inválido según las reglas de la pieza
            </div>
          )}

          <Board
            board={boardState}
            selectedPosition={selectedPosition}
            possibleMoves={possibleMoves}
            isWhiteInCheck={isWhiteCheck}
            isBlackInCheck={isBlackCheck}
            onSquareClick={handleSquareClick}
          />
        </section>

        <section className="sidebar-section">
          <Sidebar
            currentTurn={currentTurn}
            capturedPieces={capturedPieces}
            isWhiteCheck={isWhiteCheck}
            isBlackCheck={isBlackCheck}
            moveHistory={moveHistory}
            onReset={handleResetGame}
          />
        </section>
      </main>
    </div>
  );
};

export default App;
