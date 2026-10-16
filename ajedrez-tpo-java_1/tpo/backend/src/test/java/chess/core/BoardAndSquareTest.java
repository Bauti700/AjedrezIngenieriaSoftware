package chess.core;

import static org.junit.jupiter.api.Assertions.*;

import chess.core.model.Board;
import chess.core.model.Color;
import chess.core.model.Piece;
import chess.core.model.Position;
import chess.core.model.Square;
import chess.core.movement.RookMovementStrategy;
import org.junit.jupiter.api.Test;

class BoardAndSquareTest {

    @Test
    void squareStartsEmptyCanHoldPieceAndBeCleared() {
        // Arrange
        Square square = new Square(new Position(0, 0));
        Piece piece = new Piece(Color.WHITE, new RookMovementStrategy());
        // Act & Assert
        assertFalse(square.isOccupied());
        square.setPiece(piece);
        assertTrue(square.isOccupied());
        assertSame(piece, square.getPiece());
        square.clear();
        assertNull(square.getPiece());
    }

    @Test
    void boardDefaultsTo8x8() {
        Board board = new Board();
        assertEquals(8, board.getWidth());
        assertEquals(8, board.getHeight());
    }

    @Test
    void boardKnowsItsBounds() {
        Board board = new Board();
        assertTrue(board.isWithinBounds(new Position(0, 0)));
        assertTrue(board.isWithinBounds(new Position(7, 7)));
        assertFalse(board.isWithinBounds(new Position(8, 0)));
        assertFalse(board.isWithinBounds(new Position(0, -1)));
    }

    @Test
    void eachSquareKnowsItsPosition() {
        assertEquals(new Position(2, 5), new Board().getSquare(new Position(2, 5)).getPosition());
    }

    @Test
    void accessingSquareOutOfBoundsThrows() {
        assertThrows(IndexOutOfBoundsException.class, () -> new Board().getSquare(new Position(9, 9)));
    }

    @Test
    void invalidSizesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Board(0, 8));
        assertThrows(IllegalArgumentException.class, () -> new Board(8, -1));
    }

    @Test
    void movePieceMovesAndEmptiesOriginOverwritingDestination() {
        // Arrange
        Board board = new Board();
        Piece mover = new Piece(Color.WHITE, new RookMovementStrategy());
        Piece victim = new Piece(Color.BLACK, new RookMovementStrategy());
        board.getSquare(new Position(7, 0)).setPiece(mover);
        board.getSquare(new Position(0, 0)).setPiece(victim);
        // Act
        board.movePiece(new Position(7, 0), new Position(0, 0));
        // Assert
        assertFalse(board.getSquare(new Position(7, 0)).isOccupied());
        assertSame(mover, board.getSquare(new Position(0, 0)).getPiece());
    }

    @Test
    void movePieceFromEmptySquareThrows() {
        Board board = new Board();
        assertThrows(IllegalStateException.class, () -> board.movePiece(new Position(4, 4), new Position(3, 4)));
    }

    @Test
    void supportsNonStandardSizes() {
        Board board = new Board(10, 6);
        assertTrue(board.isWithinBounds(new Position(5, 9)));
        assertFalse(board.isWithinBounds(new Position(6, 0)));
    }
}
