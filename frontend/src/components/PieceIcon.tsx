import React from 'react';
import type { Piece as PieceModel } from '../types/chess';

interface PieceProps {
  piece: PieceModel;
}

// Iconografía vectorial nítida para piezas de ajedrez
export const PieceIcon: React.FC<PieceProps> = ({ piece }) => {
  const isWhite = piece.color === 'white';
  const fill = isWhite ? '#f8fafc' : '#1e293b';
  const stroke = isWhite ? '#0f172a' : '#f1f5f9';

  const renderShape = () => {
    switch (piece.type) {
      case 'pawn':
        return (
          <path
            d="M22 38h4a2 2 0 0 0 2-2v-4a6 6 0 0 0-3-5.2V22a5 5 0 1 0-8 0v4.8A6 6 0 0 0 14 32v4a2 2 0 0 0 2 2h6z"
            fill={fill}
            stroke={stroke}
            strokeWidth="2"
            strokeLinejoin="round"
          />
        );
      case 'rook':
        return (
          <path
            d="M14 38h20v-4H32v-8l-2-3h-1v-5h2V12h-4v3h-3v-3h-4v3h-3v-3h-4v6h2v5h-1l-2 3v8h-2v4z"
            fill={fill}
            stroke={stroke}
            strokeWidth="2"
            strokeLinejoin="round"
          />
        );
      case 'knight':
        return (
          <path
            d="M14 38h20v-4c0-3-2-5-4-7l2-9-5-4-3 3-4-2c-3 3-2 8-2 10l-4 4v5z"
            fill={fill}
            stroke={stroke}
            strokeWidth="2"
            strokeLinejoin="round"
          />
        );
      case 'bishop':
        return (
          <g>
            <path
              d="M24 10a5 5 0 0 0-5 5c0 3 2 5 3 7-4 3-5 7-5 12h14c0-5-1-9-5-12 1-2 3-4 3-7a5 5 0 0 0-5-5z"
              fill={fill}
              stroke={stroke}
              strokeWidth="2"
              strokeLinejoin="round"
            />
            <path d="M21 20l6 6M21 26l6-6" stroke={stroke} strokeWidth="1.5" />
          </g>
        );
      case 'queen':
        return (
          <path
            d="M13 38h22v-4l-3-12 5-6-7 2-6-7-6 7-7-2 5 6-3 12v4z"
            fill={fill}
            stroke={stroke}
            strokeWidth="2"
            strokeLinejoin="round"
          />
        );
      case 'king':
        return (
          <g>
            <path
              d="M22 6h4v3h-4V6zM20 9h8v3h-8V9z"
              fill={fill}
              stroke={stroke}
              strokeWidth="1.5"
            />
            <path
              d="M15 38h18v-4l-2-10 4-4-5-2-6 5-6-5-5 2 4 4-2 10v4z"
              fill={fill}
              stroke={stroke}
              strokeWidth="2"
              strokeLinejoin="round"
            />
          </g>
        );
      default:
        return null;
    }
  };

  return (
    <svg
      viewBox="0 0 48 48"
      className="piece-svg"
      style={{
        width: '80%',
        height: '80%',
        filter: isWhite
          ? 'drop-shadow(0 4px 6px rgba(0,0,0,0.35))'
          : 'drop-shadow(0 4px 6px rgba(0,0,0,0.6))',
        transition: 'transform 0.15s ease',
        cursor: 'grab'
      }}
    >
      {renderShape()}
    </svg>
  );
};
