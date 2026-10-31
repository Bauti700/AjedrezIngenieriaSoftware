package chess.core.game;

import chess.core.model.Board;
import chess.core.model.Color;
import chess.core.model.Piece;
import chess.core.model.PieceType;
import chess.core.model.Position;
import java.util.List;
import java.util.Optional;

/**
 * «interface» ChessGameAPI (UML): el PUERTO del nucleo.
 * Los adaptadores (ConsoleUI, ChessHttpServer) dependen SOLO de esta interfaz,
 * nunca de ChessGame directamente (Inversion de Dependencias).
 */
public interface ChessGameAPI {
    boolean makeMove(Position from, Position to);

    boolean isCheck(Color color);

    boolean isCheckmate(Color color);

    default boolean isGameOver() {
        return isCheckmate(Color.WHITE) || isCheckmate(Color.BLACK);
    }

    Color getCurrentTurn();

    List<Piece> getCapturedPieces();

    /**
     * Si un peon acaba de llegar a la ultima fila, devuelve la promocion pendiente.
     * Mientras haya una, makeMove rechaza cualquier otra jugada y el turno no avanza.
     */
    Optional<PendingPromotion> getPendingPromotion();

    /** Opciones validas para resolver la promocion pendiente (vacio si no hay ninguna). */
    List<PieceType> getPromotionOptions();

    /** Resuelve la promocion pendiente con la pieza elegida. Devuelve false si no corresponde. */
    boolean promote(PieceType type);

    /** Deshace la ultima jugada completa. False si no hay nada para deshacer. */
    boolean undo();

    /** Rehace la ultima jugada deshecha. False si no hay nada para rehacer. */
    boolean redo();

    boolean canUndo();

    boolean canRedo();

    /** Jugadas ejecutadas, de la mas antigua a la mas reciente (para el registro de la UI). */
    List<String> getMoveHistory();

    /**
     * Necesario para que un adaptador pueda dibujar el tablero.
     * Uso de SOLO LECTURA: los cambios se hacen siempre via makeMove.
     */
    Board getBoard();
}
