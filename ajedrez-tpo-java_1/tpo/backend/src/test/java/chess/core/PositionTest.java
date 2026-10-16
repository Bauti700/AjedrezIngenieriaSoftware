package chess.core;

import static org.junit.jupiter.api.Assertions.*;

import chess.core.model.Position;
import org.junit.jupiter.api.Test;

class PositionTest {

    @Test
    void exposesRowAndColumn() {
        // Arrange
        Position position = new Position(3, 5);
        // Act & Assert
        assertEquals(3, position.getRow());
        assertEquals(5, position.getColumn());
    }

    @Test
    void offsetReturnsNewPositionWithoutMutatingOriginal() {
        // Arrange
        Position original = new Position(4, 4);
        // Act
        Position moved = original.offset(-2, 1);
        // Assert
        assertEquals(new Position(2, 5), moved);
        assertEquals(new Position(4, 4), original);
    }

    @Test
    void positionsWithSameCoordinatesAreEqual() {
        assertEquals(new Position(1, 2), new Position(1, 2));
        assertEquals(new Position(1, 2).hashCode(), new Position(1, 2).hashCode());
        assertNotEquals(new Position(1, 2), new Position(2, 1));
    }
}
