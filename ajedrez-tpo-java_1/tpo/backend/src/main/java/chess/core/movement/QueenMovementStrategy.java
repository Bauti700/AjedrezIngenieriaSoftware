package chess.core.movement;

import chess.core.model.Board;
import chess.core.model.Color;
import chess.core.model.Piece;
import chess.core.model.Position;
import java.util.ArrayList;
import java.util.List;

/** Dama: se desliza en linea recta y en diagonal. */
public class QueenMovementStrategy implements MovementStrategy {
    private static final int[][] DIRECTIONS = {
        {-1, 0}, {1, 0}, {0, -1}, {0, 1},
        {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
    };

    @Override
    public List<Position> getPossibleDestinations(Position from, Board board, Color color) {
        return slide(from, board, color, DIRECTIONS);
    }

    /** Recorre cada direccion hasta salir del tablero o chocar con una pieza (captura si es rival). */
    private List<Position> slide(Position from, Board board, Color color, int[][] directions) {
        List<Position> destinations = new ArrayList<>();
        for (int[] direction : directions) {
            Position current = from.offset(direction[0], direction[1]);
            while (board.isWithinBounds(current)) {
                Piece occupant = board.getSquare(current).getPiece();
                if (occupant == null) {
                    destinations.add(current);
                } else {
                    if (occupant.getColor() != color) {
                        destinations.add(current); // captura: incluye la casilla y corta el rayo
                    }
                    break;
                }
                current = current.offset(direction[0], direction[1]);
            }
        }
        return destinations;
    }
}
