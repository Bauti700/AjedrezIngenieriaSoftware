package chess.core.model;

import chess.core.movement.BishopMovementStrategy;
import chess.core.movement.KingMovementStrategy;
import chess.core.movement.KnightMovementStrategy;
import chess.core.movement.MovementStrategy;
import chess.core.movement.PawnMovementStrategy;
import chess.core.movement.QueenMovementStrategy;
import chess.core.movement.RookMovementStrategy;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Fabrica de piezas estandar (Factory + Flyweight).
 * Las estrategias no tienen estado: se crean una sola vez y se comparten entre todas las piezas.
 */
public final class StandardPieceFactory implements PieceFactory {
    private final Map<PieceType, MovementStrategy> strategies = new EnumMap<>(PieceType.class);

    public StandardPieceFactory() {
        strategies.put(PieceType.PAWN, new PawnMovementStrategy());
        strategies.put(PieceType.ROOK, new RookMovementStrategy());
        strategies.put(PieceType.KNIGHT, new KnightMovementStrategy());
        strategies.put(PieceType.BISHOP, new BishopMovementStrategy());
        strategies.put(PieceType.QUEEN, new QueenMovementStrategy());
        strategies.put(PieceType.KING, new KingMovementStrategy());
    }

    @Override
    public Piece create(Color color, PieceType type) {
        MovementStrategy strategy = strategies.get(Objects.requireNonNull(type, "type"));
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported piece type: " + type);
        }
        return new Piece(color, strategy);
    }
}
