package chess.core.movement;

import chess.core.model.Board;
import chess.core.model.Color;
import chess.core.model.Piece;
import chess.core.model.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * Peon:
 *  - avanza 1 casilla hacia adelante si esta libre;
 *  - desde su fila inicial puede avanzar 2 si ambas casillas estan libres;
 *  - captura SOLO en diagonal hacia adelante (nunca de frente).
 * "Adelante" depende del color: blancas suben (row - 1), negras bajan (row + 1).
 * La fila inicial se calcula con el alto del tablero (sirve para tableros distintos de 8x8).
 */
public class PawnMovementStrategy implements MovementStrategy {

    @Override
    public List<Position> getPossibleDestinations(Position from, Board board, Color color) {
        List<Position> destinations = new ArrayList<>();
        int direction = color == Color.WHITE ? -1 : 1;
        int startRow = color == Color.WHITE ? board.getHeight() - 2 : 1;

        // Avance simple
        Position oneStep = from.offset(direction, 0);
        if (board.isWithinBounds(oneStep) && !board.getSquare(oneStep).isOccupied()) {
            destinations.add(oneStep);

            // Avance doble: solo desde la fila inicial y con el camino libre
            Position twoSteps = from.offset(2 * direction, 0);
            if (from.getRow() == startRow
                    && board.isWithinBounds(twoSteps)
                    && !board.getSquare(twoSteps).isOccupied()) {
                destinations.add(twoSteps);
            }
        }

        // Capturas en diagonal
        for (int colOffset : new int[] {-1, 1}) {
            Position diagonal = from.offset(direction, colOffset);
            if (!board.isWithinBounds(diagonal)) continue;
            Piece occupant = board.getSquare(diagonal).getPiece();
            if (occupant != null && occupant.getColor() != color) {
                destinations.add(diagonal);
            }
        }
        return destinations;
    }
}
