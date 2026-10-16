package chess.core.game;

import chess.core.model.Board;
import chess.core.model.Color;
import chess.core.model.Piece;
import chess.core.model.Position;
import java.util.List;

/**
 * «interface» ChessGameAPI (UML): el PUERTO del nucleo.
 * Los adaptadores (ConsoleUI, ChessHttpServer) dependen SOLO de esta interfaz,
 * nunca de ChessGame directamente (Inversion de Dependencias).
 */
public interface ChessGameAPI {
    boolean makeMove(Position from, Position to);

    boolean isCheck(Color color);

    Color getCurrentTurn();

    List<Piece> getCapturedPieces();

    /**
     * Necesario para que un adaptador pueda dibujar el tablero.
     * Uso de SOLO LECTURA: los cambios se hacen siempre via makeMove.
     */
    Board getBoard();
}
