package chess.core.model;

/** Square (UML): una casilla del tablero; contiene 0..1 pieza (null = vacia). */
public class Square {
    private final Position position;
    private Piece piece;

    public Square(Position position) {
        this.position = position;
        this.piece = null;
    }

    /** Getter del atributo position. */
    public Position getPosition() {
        return position;
    }

    public boolean isOccupied() {
        return piece != null;
    }

    /** Devuelve la pieza, o null si la casilla esta vacia. */
    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public void clear() {
        this.piece = null;
    }
}
