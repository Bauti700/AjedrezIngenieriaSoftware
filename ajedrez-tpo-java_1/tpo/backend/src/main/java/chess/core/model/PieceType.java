package chess.core.model;

/**
 * Tipo de pieza, independiente de su estrategia de movimiento.
 * Se usa como opcion de promocion y como identificador en la frontera (adaptadores).
 */
public enum PieceType {
    PAWN,
    ROOK,
    KNIGHT,
    BISHOP,
    QUEEN,
    KING
}
