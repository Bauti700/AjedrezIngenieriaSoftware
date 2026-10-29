package chess.core.game;

import chess.core.model.Color;
import chess.core.model.Position;
import java.util.Objects;

/**
 * Promocion pendiente: el peon ya llego a la ultima fila y falta elegir la pieza.
 * Mientras exista una instancia, el turno NO avanza y no se aceptan otras jugadas.
 */
public final class PendingPromotion {
    private final Color color;
    private final Position position;

    public PendingPromotion(Color color, Position position) {
        this.color = Objects.requireNonNull(color);
        this.position = Objects.requireNonNull(position);
    }

    public Color getColor() {
        return color;
    }

    public Position getPosition() {
        return position;
    }
}
