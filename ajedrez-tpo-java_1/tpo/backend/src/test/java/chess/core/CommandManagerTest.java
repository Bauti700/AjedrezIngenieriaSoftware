package chess.core;

import static org.junit.jupiter.api.Assertions.*;

import chess.core.game.Command;
import chess.core.game.CommandManager;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests unitarios del invoker, aislados del tablero: los comandos son stubs que solo
 * cuentan cuantas veces se ejecutaron o deshicieron. El comportamiento real de una jugada
 * se prueba en UndoRedoTest a traves de ChessGame.
 */
class CommandManagerTest {

    /** Comando de prueba: registra llamadas y devuelve una descripcion fija. */
    private static final class RecordingCommand implements Command {
        private final String description;
        private int timesExecuted;
        private int timesUndone;

        RecordingCommand(String description) {
            this.description = description;
        }

        @Override
        public void execute() {
            timesExecuted++;
        }

        @Override
        public void undo() {
            timesUndone++;
        }

        @Override
        public String describe() {
            return description;
        }
    }

    @Test
    void historyListsCommandsFromOldestToNewest() {
        CommandManager manager = new CommandManager();
        manager.push(new RecordingCommand("a2→a3"));
        manager.push(new RecordingCommand("h7→h6"));
        manager.push(new RecordingCommand("b2→b3"));

        assertEquals(List.of("a2→a3", "h7→h6", "b2→b3"), manager.history());
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
    }

    @Test
    void undoInvokesTheLastCommandAndMovesItToTheRedoStack() {
        CommandManager manager = new CommandManager();
        RecordingCommand command = new RecordingCommand("e2→e4");
        manager.push(command);

        assertTrue(manager.undo());

        assertEquals(1, command.timesUndone);
        assertEquals(0, command.timesExecuted);
        assertTrue(manager.history().isEmpty());
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
    }

    @Test
    void redoReappliesCommandsInTheirOriginalOrder() {
        CommandManager manager = new CommandManager();
        RecordingCommand first = new RecordingCommand("a2→a3");
        RecordingCommand second = new RecordingCommand("h7→h6");
        manager.push(first);
        manager.push(second);

        assertTrue(manager.undo()); // deshace second
        assertTrue(manager.undo()); // deshace first
        assertTrue(manager.history().isEmpty());
        assertEquals(0, first.timesExecuted);
        assertEquals(0, second.timesExecuted);

        assertTrue(manager.redo()); // rehace first
        assertEquals(List.of("a2→a3"), manager.history());
        assertEquals(1, first.timesExecuted);

        assertTrue(manager.redo()); // rehace second
        assertEquals(List.of("a2→a3", "h7→h6"), manager.history());
        assertEquals(1, second.timesExecuted);
        assertFalse(manager.canRedo());
    }

    @Test
    void pushingANewCommandDiscardsTheRedoBranch() {
        CommandManager manager = new CommandManager();
        manager.push(new RecordingCommand("a2→a3"));
        assertTrue(manager.undo());
        assertTrue(manager.canRedo());

        manager.push(new RecordingCommand("h2→h3"));

        assertFalse(manager.canRedo());
        assertFalse(manager.redo());
        assertEquals(List.of("h2→h3"), manager.history());
        assertTrue(manager.canUndo());
    }

    @Test
    void undoAndRedoWithoutHistoryReturnFalse() {
        CommandManager manager = new CommandManager();

        assertFalse(manager.undo());
        assertFalse(manager.redo());
        assertFalse(manager.canUndo());
        assertFalse(manager.canRedo());
        assertTrue(manager.history().isEmpty());
    }
}
