package chess.core.model;

import java.util.Objects;

/** Move (UML): describe un intento de movimiento. Objeto inmutable. */
public class Move {
    private final Position from;
    private final Position to;
    private final Piece movedPiece;
    private final Piece capturedPiece;

    /** capturedPiece puede ser null si el destino esta vacio. */
    public Move(Position from, Position to, Piece movedPiece, Piece capturedPiece) {
        this.from = Objects.requireNonNull(from);
        this.to = Objects.requireNonNull(to);
        this.movedPiece = Objects.requireNonNull(movedPiece);
        this.capturedPiece = capturedPiece;
    }

    /** Getter del atributo from. */
    public Position getFrom() {
        return from;
    }

    /** Getter del atributo to. */
    public Position getTo() {
        return to;
    }

    /** Getter del atributo movedPiece. */
    public Piece getMovedPiece() {
        return movedPiece;
    }

    /** Devuelve la pieza capturada, o null si no hay captura. */
    public Piece getCapturedPiece() {
        return capturedPiece;
    }
}
