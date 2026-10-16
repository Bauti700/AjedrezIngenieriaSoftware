package chess.core.rules;

import chess.core.model.Board;
import chess.core.model.Color;
import chess.core.model.Move;
import chess.core.model.Piece;
import chess.core.model.Position;
import java.util.Objects;

/**
 * MoveValidator (UML): decide si un Move es legal.
 * Recibe su dependencia (CheckDetector) por constructor: inyeccion de dependencias.
 */
public class MoveValidator {
    private final CheckDetector checkDetector;

    public MoveValidator(CheckDetector checkDetector) {
        this.checkDetector = Objects.requireNonNull(checkDetector);
    }

    /** Getter del atributo checkDetector (ChessGame lo usa para responder isCheck). */
    public CheckDetector getCheckDetector() {
        return checkDetector;
    }

    public boolean isValidMove(Move move, Board board, Color currentTurn) {
        Position from = move.getFrom();
        Position to = move.getTo();

        // 1) Ambas posiciones dentro del tablero
        if (!board.isWithinBounds(from) || !board.isWithinBounds(to)) return false;

        // 2) La pieza del Move es realmente la que esta en el origen
        Piece pieceAtOrigin = board.getSquare(from).getPiece();
        if (pieceAtOrigin == null || pieceAtOrigin != move.getMovedPiece()) return false;

        // 3) Solo se mueven piezas del jugador de turno
        if (pieceAtOrigin.getColor() != currentTurn) return false;

        // 4) El destino respeta las reglas propias de la pieza (su MovementStrategy)
        if (!pieceAtOrigin.getValidMoves(from, board).contains(to)) return false;

        // 5) No se puede dejar (ni poner) al propio rey en jaque
        return !checkDetector.willMoveLeaveKingInCheck(move, board);
    }
}
