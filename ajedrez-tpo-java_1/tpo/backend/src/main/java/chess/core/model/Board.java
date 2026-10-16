package chess.core.model;

/**
 * Board (UML): grilla de casillas de width x height.
 * El tamano se recibe por constructor (no esta fijo en 8x8), pensando en la extension
 * "cambiar el tamano del tablero". Solo sabe de casillas y limites: NO valida reglas de ajedrez.
 */
public class Board {
    private final int width;
    private final int height;
    private final Square[][] grid;

    public Board(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid board size: " + width + "x" + height);
        }
        this.width = width;
        this.height = height;
        this.grid = new Square[height][width];
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                grid[row][column] = new Square(new Position(row, column));
            }
        }
    }

    /** Tablero estandar de 8x8. */
    public Board() {
        this(8, 8);
    }

    /** Getter del atributo width. */
    public int getWidth() {
        return width;
    }

    /** Getter del atributo height. */
    public int getHeight() {
        return height;
    }

    public Square getSquare(Position pos) {
        if (!isWithinBounds(pos)) {
            throw new IndexOutOfBoundsException("Position out of bounds: " + pos);
        }
        return grid[pos.getRow()][pos.getColumn()];
    }

    public boolean isWithinBounds(Position pos) {
        return pos.getRow() >= 0 && pos.getRow() < height
                && pos.getColumn() >= 0 && pos.getColumn() < width;
    }

    /**
     * Mueve "fisicamente" la pieza de from a to (si habia una pieza en to, queda pisada).
     * No valida reglas: quien llama ya debe haber validado el movimiento.
     */
    public void movePiece(Position from, Position to) {
        Square origin = getSquare(from);
        Square destination = getSquare(to);
        Piece piece = origin.getPiece();
        if (piece == null) {
            throw new IllegalStateException("No piece to move at " + from);
        }
        destination.setPiece(piece);
        origin.clear();
    }
}
