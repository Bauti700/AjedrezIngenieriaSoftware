package chess.core.model;

/**
 * «interface» PieceFactory: crea piezas a partir de un color y un tipo.
 * El nucleo depende de esta abstraccion, no de las estrategias concretas.
 */
public interface PieceFactory {
    Piece create(Color color, PieceType type);
}
