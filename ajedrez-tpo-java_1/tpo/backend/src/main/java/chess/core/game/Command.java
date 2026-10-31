package chess.core.game;

/**
 * «interface» Command (UML): una accion reversible sobre la partida.
 * ChessGame arma y registra los comandos; CommandManager los guarda para deshacer/rehacer.
 */
public interface Command {

    /** Vuelve a aplicar la accion (lo usa redo sobre un estado previamente deshecho). */
    void execute();

    /** Revierte exactamente lo que la accion hizo. */
    void undo();

    /** Texto corto para el registro de jugadas. */
    String describe();
}
