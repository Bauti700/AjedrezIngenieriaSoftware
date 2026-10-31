package chess.core.game;

import chess.core.model.Piece;
import chess.core.movement.PawnMovementStrategy;

/**
 * HalfmoveClock (UML): reloj de la regla de los 50 movimientos.
 *
 * Cuenta medias jugadas desde la ultima jugada de peon o captura. Cualquiera de las dos
 * reinicia el reloj; una jugada normal lo incrementa. Con 100 medias jugadas (50 por bando)
 * la partida es tablas por regla de los 50 movimientos.
 *
 * El estado se puede capturar y restaurar (snapshot) para que MoveCommand lo revierta
 * junto con el resto de la jugada.
 */
public final class HalfmoveClock {
    /** 50 jugadas completas por bando = 100 medias jugadas. */
    public static final int FIFTY_MOVE_HALFMOVES = 100;

    private int halfmoves;
    private boolean hasCompletedMove;

    public HalfmoveClock() {
        this.halfmoves = 0;
        this.hasCompletedMove = false;
    }

    /** Se llama al completar una jugada (despues de mover y cambiar el turno). */
    public void afterMove(Piece movedPiece, Piece capturedPiece) {
        boolean pawnMove = movedPiece != null
                && movedPiece.getMovementStrategy() instanceof PawnMovementStrategy;
        if (pawnMove || capturedPiece != null) {
            halfmoves = 0;
        } else {
            halfmoves++;
        }
        hasCompletedMove = true;
    }

    public int getHalfmoves() {
        return halfmoves;
    }

    /** Hay tablas automaticas por regla de los 50 movimientos. */
    public boolean isFiftyMoveRuleDraw() {
        return hasCompletedMove && halfmoves >= FIFTY_MOVE_HALFMOVES;
    }

    /** Estado actual, para que un comando pueda deshacer el avance del reloj. */
    public int snapshot() {
        return halfmoves;
    }

    public void restore(int snapshot) {
        restore(snapshot, snapshot > 0);
    }

    public boolean snapshotHasCompletedMove() {
        return hasCompletedMove;
    }

    public void restore(int snapshot, boolean hasCompletedMove) {
        if (snapshot < 0) {
            throw new IllegalArgumentException("Invalid halfmove snapshot: " + snapshot);
        }
        this.halfmoves = snapshot;
        this.hasCompletedMove = hasCompletedMove;
    }

    /** Solo para tests y para reanudar partidas con un reloj previo. */
    public static HalfmoveClock startingAt(int halfmoves) {
        HalfmoveClock clock = new HalfmoveClock();
        clock.restore(halfmoves, false);
        return clock;
    }
}
