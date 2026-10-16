import React from 'react';
import type { Color, Piece } from '../types/chess';
import { PieceIcon } from './PieceIcon';
import { RotateCcw, AlertCircle, ShieldAlert, Award, Layers } from 'lucide-react';

interface SidebarProps {
  currentTurn: Color;
  capturedPieces: Piece[];
  isWhiteCheck: boolean;
  isBlackCheck: boolean;
  moveHistory: string[];
  onReset: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({
  currentTurn,
  capturedPieces,
  isWhiteCheck,
  isBlackCheck,
  moveHistory,
  onReset
}) => {
  const whiteCaptured = capturedPieces.filter(p => p.color === 'white');
  const blackCaptured = capturedPieces.filter(p => p.color === 'black');

  return (
    <aside className="game-sidebar">
      {/* Header del Juego / Estado de Turno */}
      <div className="card status-card">
        <div className="card-header">
          <Layers size={18} className="icon-header" />
          <span>Estado de la Partida</span>
        </div>

        <div className="turn-indicator-box">
          <div className="turn-label">TURNO ACTUAL</div>
          <div className={`turn-badge ${currentTurn}`}>
            <span className={`turn-dot ${currentTurn}`} />
            <span>{currentTurn === 'white' ? 'Blancas' : 'Negras'}</span>
          </div>
        </div>

        {(isWhiteCheck || isBlackCheck) && (
          <div className="check-alert-banner">
            <ShieldAlert size={20} className="alert-icon" />
            <div>
              <strong>¡JAQUE!</strong> El rey{' '}
              {isWhiteCheck ? 'blanco' : 'negro'} está bajo ataque.
            </div>
          </div>
        )}
      </div>

      {/* Piezas Capturadas */}
      <div className="card captured-card">
        <div className="card-header">
          <Award size={18} className="icon-header" />
          <span>Piezas Capturadas</span>
        </div>

        <div className="captured-section">
          <div className="captured-player-title">Capturadas por Negras (Blancas caídas):</div>
          <div className="captured-grid">
            {whiteCaptured.length === 0 ? (
              <span className="empty-text">Ninguna</span>
            ) : (
              whiteCaptured.map((p, idx) => (
                <div key={idx} className="captured-mini-piece">
                  <PieceIcon piece={p} />
                </div>
              ))
            )}
          </div>
        </div>

        <div className="captured-section">
          <div className="captured-player-title">Capturadas por Blancas (Negras caídas):</div>
          <div className="captured-grid">
            {blackCaptured.length === 0 ? (
              <span className="empty-text">Ninguna</span>
            ) : (
              blackCaptured.map((p, idx) => (
                <div key={idx} className="captured-mini-piece">
                  <PieceIcon piece={p} />
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      {/* Historial de Movimientos */}
      <div className="card history-card">
        <div className="card-header">
          <AlertCircle size={18} className="icon-header" />
          <span>Registro de Jugadas</span>
        </div>
        <div className="history-list">
          {moveHistory.length === 0 ? (
            <div className="empty-text">No hay movimientos aún</div>
          ) : (
            moveHistory.map((mv, i) => (
              <div key={i} className="history-item">
                <span className="move-number">{i + 1}.</span>
                <span className="move-notation">{mv}</span>
              </div>
            ))
          )}
        </div>
      </div>

      {/* Controles de la partida */}
      <div className="actions-section">
        <button className="btn btn-reset" onClick={onReset}>
          <RotateCcw size={18} />
          <span>Reiniciar Partida</span>
        </button>
      </div>
    </aside>
  );
};
