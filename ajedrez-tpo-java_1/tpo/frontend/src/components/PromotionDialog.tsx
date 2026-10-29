import React from 'react';
import type { Color, PieceType } from '../types/chess';
import { PieceIcon } from './PieceIcon';

interface PromotionDialogProps {
  color: Color;
  options: PieceType[];
  disabled?: boolean;
  onSelect: (type: PieceType) => void;
  onReset?: () => void;
}

const LABELS: Record<PieceType, string> = {
  queen: 'Dama',
  rook: 'Torre',
  bishop: 'Alfil',
  knight: 'Caballo',
  pawn: 'Peón',
  king: 'Rey'
};

/**
 * Selector de promoción. La partida queda congelada en el núcleo hasta que el jugador elige:
 * no se puede cerrar con Escape ni hacer clic afuera, igual que en el reglamento.
 */
export const PromotionDialog: React.FC<PromotionDialogProps> = ({
  color,
  options,
  disabled = false,
  onSelect,
  onReset
}) => {
  return (
    <div
      className="promotion-overlay"
      role="dialog"
      aria-modal="true"
      aria-label="Elegir pieza de promoción"
    >
      <div className="promotion-dialog">
        <h2>Promoción del peón</h2>
        <p>Elegí a qué pieza querés promover.</p>
        <div className="promotion-options">
          {options.map(type => (
            <button
              key={type}
              type="button"
              className="promotion-option"
              disabled={disabled}
              onClick={() => onSelect(type)}
            >
              <PieceIcon piece={{ color, type }} />
              <span>{LABELS[type]}</span>
            </button>
          ))}
        </div>
        {onReset && (
          <button type="button" className="promotion-reset" disabled={disabled} onClick={onReset}>
            Reiniciar partida
          </button>
        )}
      </div>
    </div>
  );
};
