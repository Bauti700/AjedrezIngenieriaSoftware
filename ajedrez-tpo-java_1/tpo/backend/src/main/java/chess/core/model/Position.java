package chess.core.model;

import java.util.Objects;

/**
 * Position (UML): coordenada INMUTABLE dentro del tablero.
 * row 0 = fila superior (rank 8 en un tablero estandar), column 0 = columna "a".
 */
public final class Position {
    private final int row;
    private final int column;

    public Position(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    /** Devuelve una NUEVA posicion desplazada; la original no cambia. */
    public Position offset(int rowOffset, int colOffset) {
        return new Position(row + rowOffset, column + colOffset);
    }

    // equals/hashCode: dos posiciones con la misma fila y columna son iguales (objeto de valor).
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Position)) return false;
        Position that = (Position) other;
        return row == that.row && column == that.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }

    @Override
    public String toString() {
        return "(" + row + ", " + column + ")";
    }
}
