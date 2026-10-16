package chess.core;

import static org.junit.jupiter.api.Assertions.*;

import chess.core.model.Color;
import chess.core.rules.TurnManager;
import org.junit.jupiter.api.Test;

class TurnManagerTest {

    @Test
    void startsWithWhiteByDefault() {
        assertEquals(Color.WHITE, new TurnManager().getCurrentTurn());
    }

    @Test
    void alternatesTurns() {
        // Arrange
        TurnManager turns = new TurnManager(Color.WHITE);
        // Act & Assert
        turns.switchTurn();
        assertEquals(Color.BLACK, turns.getCurrentTurn());
        turns.switchTurn();
        assertEquals(Color.WHITE, turns.getCurrentTurn());
    }

    @Test
    void canStartWithBlackIfInjected() {
        assertEquals(Color.BLACK, new TurnManager(Color.BLACK).getCurrentTurn());
    }
}
