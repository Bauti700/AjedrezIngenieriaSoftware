package chess.core;

import chess.core.model.Board;
import chess.core.model.Color;
import chess.core.model.Piece;
import chess.core.model.Position;
import chess.core.movement.MovementStrategy;
import java.util.List;

/** Utilidades SOLO para tests: casillas en notacion algebraica sobre un tablero 8x8. */
public final class TestBoards {
    private TestBoards() {
    }

    /** "e2" -> Position(6, 4) */
    public static Position sq(String name) {
        int column = name.charAt(0) - 'a';
        int row = 8 - Integer.parseInt(name.substring(1));
        return new Position(row, column);
    }

    /** Position(6, 4) -> "e2" */
    public static String nameOf(Position position) {
        return "" + (char) ('a' + position.getColumn()) + (8 - position.getRow());
    }

    /** Lista de posiciones -> nombres ordenados (compara sin depender del orden). */
    public static List<String> names(List<Position> positions) {
        return positions.stream().map(TestBoards::nameOf).sorted().toList();
    }

    /** Coloca una pieza nueva en el tablero y la devuelve. */
    public static Piece place(Board board, String at, Color color, MovementStrategy strategy) {
        Piece piece = new Piece(color, strategy);
        board.getSquare(sq(at)).setPiece(piece);
        return piece;
    }
}
