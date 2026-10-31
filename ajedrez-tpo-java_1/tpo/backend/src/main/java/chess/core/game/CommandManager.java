package chess.core.game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/**
 * CommandManager (UML): el «invoker» del patron Command.
 * Apila cada jugada completa; deshacer la mueve a la pila de rehacer y viceversa.
 * Una jugada nueva siempre invalida el rehacer (la rama de futuro anterior deja de existir).
 */
public final class CommandManager {
    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    /** Registra una jugada ya ejecutada y descarta la rama de rehacer. */
    public void push(Command command) {
        undoStack.push(command);
        redoStack.clear();
    }

    public boolean undo() {
        if (undoStack.isEmpty()) return false;
        Command command = undoStack.pop();
        command.undo();
        redoStack.push(command);
        return true;
    }

    public boolean redo() {
        if (redoStack.isEmpty()) return false;
        Command command = redoStack.pop();
        command.execute();
        undoStack.push(command);
        return true;
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /** Jugadas ejecutadas, de la mas antigua a la mas reciente. */
    public List<String> history() {
        List<String> entries = new ArrayList<>();
        Iterator<Command> oldestFirst = undoStack.descendingIterator();
        while (oldestFirst.hasNext()) {
            entries.add(oldestFirst.next().describe());
        }
        return List.copyOf(entries);
    }
}
