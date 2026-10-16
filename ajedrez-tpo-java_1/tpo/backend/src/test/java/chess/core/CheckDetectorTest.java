package chess.core;

import static chess.core.TestBoards.place;
import static chess.core.TestBoards.sq;
import static org.junit.jupiter.api.Assertions.*;

import chess.core.model.Board;
import chess.core.model.Color;
import chess.core.model.Move;
import chess.core.model.Piece;
import chess.core.movement.BishopMovementStrategy;
import chess.core.movement.KingMovementStrategy;
import chess.core.movement.KnightMovementStrategy;
import chess.core.movement.PawnMovementStrategy;
import chess.core.movement.RookMovementStrategy;
import chess.core.rules.CheckDetector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CheckDetectorTest {
    private Board board;
    private CheckDetector detector;

    @BeforeEach
    void setUp() {
        board = new Board();
        detector = new CheckDetector();
    }

    // ---------------------------------------------------------------- isInCheck

    @Test
    void detectsRookAttackingKingOnOpenFile() {
        place(board, "e1", Color.WHITE, new KingMovementStrategy());
        place(board, "e8", Color.BLACK, new RookMovementStrategy());
        assertTrue(detector.isInCheck(Color.WHITE, board));
        assertFalse(detector.isInCheck(Color.BLACK, board));
    }

    @Test
    void pieceInBetweenBlocksTheCheck() {
        place(board, "e1", Color.WHITE, new KingMovementStrategy());
        place(board, "e8", Color.BLACK, new RookMovementStrategy());
        place(board, "e4", Color.WHITE, new PawnMovementStrategy());
        assertFalse(detector.isInCheck(Color.WHITE, board));
    }

    @Test
    void detectsKnightCheckJumpingOverPieces() {
        place(board, "e1", Color.WHITE, new KingMovementStrategy());
        place(board, "f3", Color.BLACK, new KnightMovementStrategy());
        place(board, "e2", Color.WHITE, new PawnMovementStrategy());
        place(board, "f2", Color.WHITE, new PawnMovementStrategy());
        assertTrue(detector.isInCheck(Color.WHITE, board));
    }

    @Test
    void pawnGivesCheckDiagonallyNotStraight() {
        place(board, "e4", Color.WHITE, new KingMovementStrategy());
        place(board, "e5", Color.BLACK, new PawnMovementStrategy());
        assertFalse(detector.isInCheck(Color.WHITE, board));
        place(board, "d5", Color.BLACK, new PawnMovementStrategy());
        assertTrue(detector.isInCheck(Color.WHITE, board));
    }

    @Test
    void ownPiecesNeverGiveCheck() {
        place(board, "e1", Color.WHITE, new KingMovementStrategy());
        place(board, "e8", Color.WHITE, new RookMovementStrategy());
        assertFalse(detector.isInCheck(Color.WHITE, board));
    }

    @Test
    void noKingMeansNoCheck() {
        place(board, "e8", Color.BLACK, new RookMovementStrategy());
        assertFalse(detector.isInCheck(Color.WHITE, board));
    }

    // ---------------------------------------------------------------- willMoveLeaveKingInCheck

    @Test
    void detectsMovingAPinnedPiece() {
        // Arrange: alfil blanco clavado entre su rey y una torre negra
        place(board, "e1", Color.WHITE, new KingMovementStrategy());
        Piece bishop = place(board, "e2", Color.WHITE, new BishopMovementStrategy());
        place(board, "e8", Color.BLACK, new RookMovementStrategy());
        Move move = new Move(sq("e2"), sq("d3"), bishop, null);
        // Act & Assert
        assertTrue(detector.willMoveLeaveKingInCheck(move, board));
    }

    @Test
    void detectsKingWalkingIntoAttackedSquare() {
        Piece king = place(board, "e1", Color.WHITE, new KingMovementStrategy());
        place(board, "d8", Color.BLACK, new RookMovementStrategy());
        assertTrue(detector.willMoveLeaveKingInCheck(new Move(sq("e1"), sq("d1"), king, null), board));
        assertFalse(detector.willMoveLeaveKingInCheck(new Move(sq("e1"), sq("f1"), king, null), board));
    }

    @Test
    void capturingTheAttackerResolvesTheCheck() {
        place(board, "e1", Color.WHITE, new KingMovementStrategy());
        Piece rook = place(board, "a8", Color.WHITE, new RookMovementStrategy());
        Piece attacker = place(board, "e8", Color.BLACK, new RookMovementStrategy());
        assertFalse(detector.willMoveLeaveKingInCheck(new Move(sq("a8"), sq("e8"), rook, attacker), board));
    }

    @Test
    void leavesTheBoardExactlyAsItWasAfterSimulating() {
        // Arrange
        place(board, "e1", Color.WHITE, new KingMovementStrategy());
        Piece rook = place(board, "a8", Color.WHITE, new RookMovementStrategy());
        Piece target = place(board, "e8", Color.BLACK, new RookMovementStrategy());
        // Act
        detector.willMoveLeaveKingInCheck(new Move(sq("a8"), sq("e8"), rook, target), board);
        // Assert
        assertSame(rook, board.getSquare(sq("a8")).getPiece());
        assertSame(target, board.getSquare(sq("e8")).getPiece());
    }
}
