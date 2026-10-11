import React from 'react';
import type { Position, Piece } from '../types/chess';
import { PieceIcon } from './PieceIcon';

interface BoardProps {
  board: (Piece | null)[][];
  selectedPosition: Position | null;
  possibleMoves: Position[];
  isWhiteInCheck: boolean;
  isBlackInCheck: boolean;
  onSquareClick: (pos: Position) => void;
}

export const Board: React.FC<BoardProps> = ({
  board,
  selectedPosition,
  possibleMoves,
  isWhiteInCheck,
  isBlackInCheck,
  onSquareClick
}) => {
  const columnsLabels = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
  const rowLabels = ['8', '7', '6', '5', '4', '3', '2', '1'];

  const isMoveDestination = (r: number, c: number) => {
    return possibleMoves.some(m => m.row === r && m.column === c);
  };

  const isSelected = (r: number, c: number) => {
    return selectedPosition?.row === r && selectedPosition?.column === c;
  };

  return (
    <div className="chess-board-wrapper">
      <div className="chess-board-container">
        {board.map((row, rIdx) => (
          <div key={rIdx} className="board-row">
            {row.map((piece, cIdx) => {
              const isDark = (rIdx + cIdx) % 2 === 1;
              const selected = isSelected(rIdx, cIdx);
              const isPossible = isMoveDestination(rIdx, cIdx);
              const isCheckKing =
                piece?.type === 'king' &&
                ((piece.color === 'white' && isWhiteInCheck) ||
                  (piece.color === 'black' && isBlackInCheck));

              return (
                <div
                  key={`${rIdx}-${cIdx}`}
                  className={`square ${isDark ? 'square-dark' : 'square-light'} ${
                    selected ? 'selected' : ''
                  } ${isCheckKing ? 'in-check' : ''}`}
                  onClick={() => onSquareClick({ row: rIdx, column: cIdx })}
                  title={`${columnsLabels[cIdx]}${rowLabels[rIdx]}`}
                >
                  {/* Coordenadas en los bordes */}
                  {cIdx === 0 && (
                    <span className="coord-label coord-rank">{rowLabels[rIdx]}</span>
                  )}
                  {rIdx === 7 && (
                    <span className="coord-label coord-file">{columnsLabels[cIdx]}</span>
                  )}

                  {/* Indicador de movimiento posible */}
                  {isPossible && (
                    <div
                      className={`move-hint ${
                        piece ? 'move-hint-capture' : 'move-hint-dot'
                      }`}
                    />
                  )}

                  {/* Pieza */}
                  {piece && <PieceIcon piece={piece} />}
                </div>
              );
            })}
          </div>
        ))}
      </div>
    </div>
  );
};
