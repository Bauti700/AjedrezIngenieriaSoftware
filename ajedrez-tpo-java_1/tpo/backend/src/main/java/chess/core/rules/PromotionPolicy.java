package chess.core.rules;

import chess.core.model.Board;
import chess.core.model.Piece;
import chess.core.model.PieceType;
import chess.core.model.Position;
import java.util.List;

/**
 * «interface» PromotionPolicy: decide CUANDO hay promocion y QUE piezas se pueden elegir.
 * Mantiene la regla fuera de ChessGame y permite variantes sin modificarlo (Open/Closed).
 */
public interface PromotionPolicy {

    /** ¿El movimiento de esta pieza a este destino dispara una promocion? */
    boolean applies(Piece piece, Position destination, Board board);

    /** Piezas a las que se puede promover, en el orden en que conviene mostrarlas. */
    List<PieceType> allowedChoices();

    /** Atajo: ¿es una eleccion valida? */
    default boolean isAllowed(PieceType type) {
        return type != null && allowedChoices().contains(type);
    }
}
