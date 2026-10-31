package chess.core;

import static org.junit.jupiter.api.Assertions.*;

import chess.core.game.ChessGame;
import chess.core.game.CommandManager;
import chess.core.model.Board;
import chess.core.model.Color;
import chess.core.model.Piece;
import chess.core.model.Position;
import chess.core.model.StandardPieceFactory;
import chess.core.movement.KingMovementStrategy;
import chess.core.movement.MovementStrategy;
import chess.core.movement.PawnMovementStrategy;
import chess.core.movement.RookMovementStrategy;
import chess.core.rules.CheckDetector;
import chess.core.rules.MoveValidator;
import chess.core.rules.StandardPromotionPolicy;
import chess.core.rules.TurnManager;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Prueba de Open/Closed: se agrega una pieza NUEVA ("Camello", salta 3+1)
 * creando solo una clase nueva. Ninguna clase existente del nucleo se modifica.
 */
class ExtensibilityTest {

    static class CamelMovementStrategy implements MovementStrategy {
        private static final int[][] OFFSETS = {
            {-3, -1}, {-3, 1}, {-1, -3}, {-1, 3}, {1, -3}, {1, 3}, {3, -1}, {3, 1}
        };

        @Override
        public List<Position> getPossibleDestinations(Position from, Board board, Color color) {
            List<Position> destinations = new ArrayList<>();
            for (int[] offset : OFFSETS) {
                Position target = from.offset(offset[0], offset[1]);
                if (!board.isWithinBounds(target)) continue;
                Piece occupant = board.getSquare(target).getPiece();
                if (occupant == null || occupant.getColor() != color) destinations.add(target);
            }
            return destinations;
        }
    }

    private static ChessGame gameWith(Board board) {
        return new ChessGame(
                board,
                new TurnManager(),
                new MoveValidator(new CheckDetector()),
                new StandardPromotionPolicy(),
                new StandardPieceFactory(),
                new CommandManager());
    }

    @Test
    void newPieceTypeWorksInAFullGameWithoutModifyingExistingClasses() {
        // Arrange
        Board board = new Board();
        board.getSquare(new Position(7, 4)).setPiece(new Piece(Color.WHITE, new KingMovementStrategy()));
        board.getSquare(new Position(0, 4)).setPiece(new Piece(Color.BLACK, new KingMovementStrategy()));
        board.getSquare(new Position(7, 1)).setPiece(new Piece(Color.WHITE, new CamelMovementStrategy()));
        ChessGame game = gameWith(board);
        // Act & Assert
        assertFalse(game.makeMove(new Position(7, 1), new Position(5, 0))); // salto de caballo: invalido
        assertTrue(game.makeMove(new Position(7, 1), new Position(4, 2)));  // salto de camello: valido
    }

    @Test
    void newPieceCanGiveCheckWithoutCheckDetectorKnowingIt() {
        Board board = new Board();
        board.getSquare(new Position(0, 4)).setPiece(new Piece(Color.BLACK, new KingMovementStrategy()));
        board.getSquare(new Position(3, 5)).setPiece(new Piece(Color.WHITE, new CamelMovementStrategy()));
        assertTrue(new CheckDetector().isInCheck(Color.BLACK, board));
    }

    @Test
    void rulesWorkOnABiggerBoard() {
        // Arrange: tablero 10x10
        Board board = new Board(10, 10);
        RookMovementStrategy rook = new RookMovementStrategy();
        board.getSquare(new Position(9, 0)).setPiece(new Piece(Color.WHITE, rook));
        board.getSquare(new Position(8, 5)).setPiece(new Piece(Color.WHITE, new PawnMovementStrategy()));
        board.getSquare(new Position(0, 9)).setPiece(new Piece(Color.BLACK, new KingMovementStrategy()));
        ChessGame game = gameWith(board);
        // Act & Assert
        assertEquals(18, rook.getPossibleDestinations(new Position(9, 0), board, Color.WHITE).size());
        assertTrue(game.makeMove(new Position(8, 5), new Position(6, 5))); // doble paso desde su fila inicial
        assertTrue(game.makeMove(new Position(0, 9), new Position(1, 9)));
        assertTrue(game.makeMove(new Position(9, 0), new Position(0, 0))); // la torre recorre 10 filas
        assertFalse(game.isCheck(Color.BLACK));
    }
}
