package chess.core.rules;

import chess.core.model.Board;
import chess.core.model.Color;
import chess.core.model.Piece;
import chess.core.model.PieceType;
import chess.core.model.Position;
import chess.core.movement.PawnMovementStrategy;
import java.util.List;

/**
 * Regla estandar: un peon que llega a la ultima fila promociona a dama, torre, alfil o caballo.
 * La fila final se calcula con el alto del tablero, asi tambien funciona en tableros que no son 8x8.
 */
public class StandardPromotionPolicy implements PromotionPolicy {
    private static final List<PieceType> CHOICES =
            List.of(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT);

    @Override
    public boolean applies(Piece piece, Position destination, Board board) {
        if (piece == null || destination == null || board == null) return false;
        if (!(piece.getMovementStrategy() instanceof PawnMovementStrategy)) return false;
        int promotionRow = piece.getColor() == Color.WHITE ? 0 : board.getHeight() - 1;
        return destination.getRow() == promotionRow;
    }

    @Override
    public List<PieceType> allowedChoices() {
        return CHOICES;
    }
}
