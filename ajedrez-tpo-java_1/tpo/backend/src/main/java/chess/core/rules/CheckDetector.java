package chess.core.rules;

import chess.core.model.Board;
import chess.core.model.Color;
import chess.core.model.Move;
import chess.core.model.Piece;
import chess.core.model.Position;
import chess.core.model.Square;
import chess.core.movement.KingMovementStrategy;

/**
 * CheckDetector (UML): detecta si un rey esta en jaque.
 * Logica pura sobre el Board recibido: no guarda estado propio.
 */
public class CheckDetector {

    /** ¿El rey de color esta atacado por alguna pieza rival? */
    public boolean isInCheck(Color color, Board board) {
        Color opponent = color == Color.WHITE ? Color.BLACK : Color.WHITE;

        // 1) Buscar el rey de color (se lo reconoce por su estrategia de movimiento)
        Position kingPosition = null;
        for (int row = 0; row < board.getHeight() && kingPosition == null; row++) {
            for (int column = 0; column < board.getWidth(); column++) {
                Position position = new Position(row, column);
                Piece piece = board.getSquare(position).getPiece();
                if (piece != null
                        && piece.getColor() == color
                        && piece.getMovementStrategy() instanceof KingMovementStrategy) {
                    kingPosition = position;
                    break;
                }
            }
        }
        if (kingPosition == null) {
            return false; // sin rey en el tablero (p. ej. en tests) no hay jaque
        }

        // 2) ¿Alguna pieza rival puede llegar a la casilla del rey?
        for (int row = 0; row < board.getHeight(); row++) {
            for (int column = 0; column < board.getWidth(); column++) {
                Position position = new Position(row, column);
                Piece piece = board.getSquare(position).getPiece();
                if (piece != null
                        && piece.getColor() == opponent
                        && piece.getValidMoves(position, board).contains(kingPosition)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Simula el movimiento, pregunta si el rey propio queda en jaque y DESHACE la simulacion:
     * el tablero queda exactamente como estaba.
     */
    public boolean willMoveLeaveKingInCheck(Move move, Board board) {
        Square origin = board.getSquare(move.getFrom());
        Square destination = board.getSquare(move.getTo());
        Piece pieceOnDestination = destination.getPiece();

        board.movePiece(move.getFrom(), move.getTo());
        try {
            return isInCheck(move.getMovedPiece().getColor(), board);
        } finally {
            // Restaurar el estado original pase lo que pase
            origin.setPiece(move.getMovedPiece());
            if (pieceOnDestination != null) {
                destination.setPiece(pieceOnDestination);
            } else {
                destination.clear();
            }
        }
    }
}
