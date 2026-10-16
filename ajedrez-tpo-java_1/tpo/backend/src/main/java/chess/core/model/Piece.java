package chess.core.model;

import chess.core.movement.MovementStrategy;
import java.util.List;
import java.util.Objects;

/**
 * Piece (UML): una pieza NO hereda de una jerarquia (Torre extends Pieza...).
 * Se arma por COMPOSICION: un color + una estrategia de movimiento (patron Strategy).
 * Una pieza nueva = una estrategia nueva, sin modificar esta clase (Open/Closed).
 */
public class Piece {
    private final Color color;
    private final MovementStrategy movementStrategy;

    public Piece(Color color, MovementStrategy movementStrategy) {
        this.color = Objects.requireNonNull(color);
        this.movementStrategy = Objects.requireNonNull(movementStrategy);
    }

    public Color getColor() {
        return color;
    }

    /** Getter del atributo movementStrategy (permite identificar el tipo de pieza). */
    public MovementStrategy getMovementStrategy() {
        return movementStrategy;
    }

    /** Delega en la estrategia: la pieza no sabe COMO se mueve, solo a quien preguntarle. */
    public List<Position> getValidMoves(Position from, Board board) {
        return movementStrategy.getPossibleDestinations(from, board, color);
    }
}
