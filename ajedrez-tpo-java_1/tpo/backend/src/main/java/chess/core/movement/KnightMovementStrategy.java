package chess.core.movement;

import chess.core.model.Board;
import chess.core.model.Color;
import chess.core.model.Piece;
import chess.core.model.Position;
import java.util.ArrayList;
import java.util.List;

/** Caballo: salta en "L" (2+1), ignorando las piezas intermedias. */
public class KnightMovementStrategy implements MovementStrategy {
    private static final int[][] OFFSETS = {
        {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
        {1, -2}, {1, 2}, {2, -1}, {2, 1}
    };

    @Override
    public List<Position> getPossibleDestinations(Position from, Board board, Color color) {
        return step(from, board, color, OFFSETS);
    }

    /** Aplica cada desplazamiento una sola vez: valido si la casilla esta libre o tiene una pieza rival. */
    private List<Position> step(Position from, Board board, Color color, int[][] offsets) {
        List<Position> destinations = new ArrayList<>();
        for (int[] offset : offsets) {
            Position target = from.offset(offset[0], offset[1]);
            if (!board.isWithinBounds(target)) continue;
            Piece occupant = board.getSquare(target).getPiece();
            if (occupant == null || occupant.getColor() != color) {
                destinations.add(target);
            }
        }
        return destinations;
    }
}
