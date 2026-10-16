package chess.core;

import static chess.core.TestBoards.place;
import static chess.core.TestBoards.sq;
import static org.junit.jupiter.api.Assertions.*;

import chess.core.model.Board;
import chess.core.model.Color;
import chess.core.model.Move;
import chess.core.model.Piece;
import chess.core.movement.KingMovementStrategy;
import chess.core.movement.RookMovementStrategy;
import chess.core.rules.CheckDetector;
import chess.core.rules.MoveValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MoveValidatorTest {
    private Board board;

    /**
     * STUB de CheckDetector: devuelve un valor fijo, sin logica.
     * Como MoveValidator recibe su dependencia por constructor, se prueba aislado del detector real.
     */
    private static CheckDetector stubCheckDetector(boolean leavesKingInCheck) {
        return new CheckDetector() {
            @Override
            public boolean isInCheck(Color color, Board board) {
                return false;
            }

            @Override
            public boolean willMoveLeaveKingInCheck(Move move, Board board) {
                return leavesKingInCheck;
            }
        };
    }

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    @Test
    void acceptsMoveAllowedByPieceStrategy() {
        // Arrange
        MoveValidator validator = new MoveValidator(stubCheckDetector(false));
        Piece rook = place(board, "a1", Color.WHITE, new RookMovementStrategy());
        // Act
        boolean result = validator.isValidMove(new Move(sq("a1"), sq("a5"), rook, null), board, Color.WHITE);
        // Assert
        assertTrue(result);
    }

    @Test
    void rejectsMoveNotAllowedByPieceStrategy() {
        MoveValidator validator = new MoveValidator(stubCheckDetector(false));
        Piece rook = place(board, "a1", Color.WHITE, new RookMovementStrategy());
        assertFalse(validator.isValidMove(new Move(sq("a1"), sq("b2"), rook, null), board, Color.WHITE));
    }

    @Test
    void rejectsMovingOpponentPiece() {
        MoveValidator validator = new MoveValidator(stubCheckDetector(false));
        Piece rook = place(board, "a8", Color.BLACK, new RookMovementStrategy());
        assertFalse(validator.isValidMove(new Move(sq("a8"), sq("a5"), rook, null), board, Color.WHITE));
    }

    @Test
    void rejectsWhenCheckDetectorSaysKingWouldBeInCheck() {
        MoveValidator validator = new MoveValidator(stubCheckDetector(true));
        Piece rook = place(board, "a1", Color.WHITE, new RookMovementStrategy());
        assertFalse(validator.isValidMove(new Move(sq("a1"), sq("a5"), rook, null), board, Color.WHITE));
    }

    @Test
    void rejectsMoveWhosePieceIsNotOnOriginSquare() {
        MoveValidator validator = new MoveValidator(stubCheckDetector(false));
        place(board, "a1", Color.WHITE, new RookMovementStrategy());
        Piece impostor = place(board, "h1", Color.WHITE, new RookMovementStrategy());
        assertFalse(validator.isValidMove(new Move(sq("a1"), sq("a5"), impostor, null), board, Color.WHITE));
    }

    @Test
    void worksWithRealCheckDetectorKingCannotMoveIntoCheck() {
        MoveValidator validator = new MoveValidator(new CheckDetector());
        Piece king = place(board, "e1", Color.WHITE, new KingMovementStrategy());
        place(board, "d8", Color.BLACK, new RookMovementStrategy());
        assertFalse(validator.isValidMove(new Move(sq("e1"), sq("d1"), king, null), board, Color.WHITE));
        assertTrue(validator.isValidMove(new Move(sq("e1"), sq("f1"), king, null), board, Color.WHITE));
    }
}
