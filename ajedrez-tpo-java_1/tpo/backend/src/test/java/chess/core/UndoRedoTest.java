package chess.core;

import static chess.core.TestBoards.sq;
import static org.junit.jupiter.api.Assertions.*;

import chess.core.game.ChessGame;
import chess.core.game.CommandManager;
import chess.core.model.Board;
import chess.core.model.Color;
import chess.core.model.Piece;
import chess.core.model.PieceType;
import chess.core.model.StandardPieceFactory;
import chess.core.movement.KingMovementStrategy;
import chess.core.movement.MovementStrategy;
import chess.core.movement.PawnMovementStrategy;
import chess.core.movement.QueenMovementStrategy;
import chess.core.movement.RookMovementStrategy;
import chess.core.rules.CheckDetector;
import chess.core.rules.MoveValidator;
import chess.core.rules.StandardPromotionPolicy;
import chess.core.rules.TurnManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UndoRedoTest {
    private Board board;
    private ChessGame game;

    @BeforeEach
    void setUp() {
        board = new Board(8, 8);
        game = newGame(board);
    }

    private static ChessGame newGame(Board board) {
        return new ChessGame(
                board,
                new TurnManager(Color.WHITE),
                new MoveValidator(new CheckDetector()),
                new StandardPromotionPolicy(),
                new StandardPieceFactory(),
                new CommandManager());
    }

    private Piece place(String square, Color color, MovementStrategy strategy) {
        return TestBoards.place(board, square, color, strategy);
    }

    private void placeKings() {
        place("e1", Color.WHITE, new KingMovementStrategy());
        place("e8", Color.BLACK, new KingMovementStrategy());
    }

    // ---------------------------------------------------------------- jugada simple

    @Test
    void undoRestoresBoardAndTurnAndRedoReapplies() {
        placeKings();
        Piece pawn = place("a2", Color.WHITE, new PawnMovementStrategy());

        assertTrue(game.makeMove(sq("a2"), sq("a3")));
        assertEquals(Color.BLACK, game.getCurrentTurn());
        assertEquals(List.of("a2→a3"), game.getMoveHistory());
        assertTrue(game.canUndo());
        assertFalse(game.canRedo());

        assertTrue(game.undo());

        assertEquals(pawn, board.getSquare(sq("a2")).getPiece());
        assertNull(board.getSquare(sq("a3")).getPiece());
        assertEquals(Color.WHITE, game.getCurrentTurn());
        assertTrue(game.getMoveHistory().isEmpty());
        assertFalse(game.canUndo());
        assertTrue(game.canRedo());

        assertTrue(game.redo());

        assertNull(board.getSquare(sq("a2")).getPiece());
        assertEquals(pawn, board.getSquare(sq("a3")).getPiece());
        assertEquals(Color.BLACK, game.getCurrentTurn());
        assertEquals(List.of("a2→a3"), game.getMoveHistory());
    }

    // ---------------------------------------------------------------- capturas

    @Test
    void undoRestoresCapturedPieceAndRedoCapturesItAgain() {
        placeKings();
        Piece rook = place("a1", Color.WHITE, new RookMovementStrategy());
        Piece blackPawn = place("a3", Color.BLACK, new PawnMovementStrategy());

        assertTrue(game.makeMove(sq("a1"), sq("a3")));
        assertEquals(List.of(blackPawn), game.getCapturedPieces());
        assertNull(board.getSquare(sq("a1")).getPiece());

        assertTrue(game.undo());

        assertTrue(game.getCapturedPieces().isEmpty());
        assertEquals(rook, board.getSquare(sq("a1")).getPiece());
        assertEquals(blackPawn, board.getSquare(sq("a3")).getPiece());

        assertTrue(game.redo());

        assertEquals(List.of(blackPawn), game.getCapturedPieces());
        assertEquals(rook, board.getSquare(sq("a3")).getPiece());
    }

    // ---------------------------------------------------------------- promocion

    @Test
    void redoIsBlockedWhilePromotionIsPending() {
        place("e1", Color.WHITE, new KingMovementStrategy());
        place("e8", Color.BLACK, new KingMovementStrategy());
        place("b7", Color.WHITE, new PawnMovementStrategy());

        assertTrue(game.makeMove(sq("b7"), sq("b8")));
        assertTrue(game.getPendingPromotion().isPresent());

        // Aunque hubiera una rama de rehacer, la jugada incompleta bloquea ambos sentidos.
        assertFalse(game.undo());
        assertFalse(game.redo());
        assertFalse(game.canUndo());
        assertFalse(game.canRedo());

        assertTrue(game.promote(PieceType.QUEEN));
        assertTrue(game.undo());
        assertTrue(game.canRedo());
    }

    @Test
    void promotionByCaptureIsFullyUndoneAndRedone() {
        place("e1", Color.WHITE, new KingMovementStrategy());
        place("e8", Color.BLACK, new KingMovementStrategy());
        place("b7", Color.WHITE, new PawnMovementStrategy());
        Piece pawn = board.getSquare(sq("b7")).getPiece();
        Piece blackRook = place("a8", Color.BLACK, new RookMovementStrategy());

        assertTrue(game.makeMove(sq("b7"), sq("a8")));
        assertEquals(List.of(blackRook), game.getCapturedPieces());
        assertTrue(game.getPendingPromotion().isPresent());

        assertTrue(game.promote(PieceType.QUEEN));
        assertEquals(List.of("b7→a8=DAMA"), game.getMoveHistory());
        assertEquals(Color.BLACK, game.getCurrentTurn());

        // Un solo undo devuelve el peon, la torre capturada y el turno.
        assertTrue(game.undo());
        assertEquals(pawn, board.getSquare(sq("b7")).getPiece());
        assertEquals(blackRook, board.getSquare(sq("a8")).getPiece());
        assertTrue(game.getCapturedPieces().isEmpty());
        assertEquals(Color.WHITE, game.getCurrentTurn());
        assertTrue(game.getMoveHistory().isEmpty());

        // Redo vuelve a coronar y a capturar la torre.
        assertTrue(game.redo());
        assertInstanceOf(
                QueenMovementStrategy.class,
                board.getSquare(sq("a8")).getPiece().getMovementStrategy());
        assertEquals(List.of(blackRook), game.getCapturedPieces());
        assertEquals(Color.BLACK, game.getCurrentTurn());
        assertEquals(List.of("b7→a8=DAMA"), game.getMoveHistory());
    }

    @Test
    void promotionIsUndoneAsASingleCompleteTurn() {
        place("e1", Color.WHITE, new KingMovementStrategy());
        place("e8", Color.BLACK, new KingMovementStrategy());
        Piece pawn = place("b7", Color.WHITE, new PawnMovementStrategy());

        assertTrue(game.makeMove(sq("b7"), sq("b8")));

        // La jugada todavia no esta completa: no hay nada para deshacer.
        assertTrue(game.getPendingPromotion().isPresent());
        assertFalse(game.undo());
        assertFalse(game.canUndo());
        assertFalse(game.canRedo());
        assertTrue(game.getMoveHistory().isEmpty());

        // Al elegir la pieza se completa el turno y recien ahi entra al historial.
        assertTrue(game.promote(PieceType.QUEEN));
        assertEquals(Color.BLACK, game.getCurrentTurn());
        assertEquals(List.of("b7→b8=DAMA"), game.getMoveHistory());
        assertTrue(game.canUndo());

        // Un solo undo revierte la jugada entera (movimiento + promocion).
        assertTrue(game.undo());
        assertEquals(pawn, board.getSquare(sq("b7")).getPiece());
        assertNull(board.getSquare(sq("b8")).getPiece());
        assertEquals(Color.WHITE, game.getCurrentTurn());
        assertTrue(game.getPendingPromotion().isEmpty());

        // Redo vuelve a coronar la misma pieza.
        assertTrue(game.redo());
        assertInstanceOf(
                QueenMovementStrategy.class,
                board.getSquare(sq("b8")).getPiece().getMovementStrategy());
        assertEquals(Color.BLACK, game.getCurrentTurn());
    }

    // ---------------------------------------------------------------- jaque mate

    @Test
    void undoOfTheMatingMoveReopensTheGame() {
        place("c6", Color.WHITE, new KingMovementStrategy());
        place("a8", Color.BLACK, new KingMovementStrategy());
        Piece queen = place("b6", Color.WHITE, new QueenMovementStrategy());

        assertFalse(game.isGameOver());

        assertTrue(game.makeMove(sq("b6"), sq("b7")));
        assertTrue(game.isCheckmate(Color.BLACK));
        assertTrue(game.isGameOver());

        assertTrue(game.undo());

        assertEquals(queen, board.getSquare(sq("b6")).getPiece());
        assertNull(board.getSquare(sq("b7")).getPiece());
        assertFalse(game.isCheckmate(Color.BLACK));
        assertFalse(game.isGameOver());
        assertEquals(Color.WHITE, game.getCurrentTurn());

        assertTrue(game.redo());
        assertTrue(game.isGameOver());
    }

    // ---------------------------------------------------------------- pilas

    @Test
    void newMoveAfterUndoDiscardsTheRedoBranch() {
        placeKings();
        place("a2", Color.WHITE, new PawnMovementStrategy());
        place("h2", Color.WHITE, new PawnMovementStrategy());

        assertTrue(game.makeMove(sq("a2"), sq("a3")));
        assertTrue(game.undo());
        assertTrue(game.canRedo());

        assertTrue(game.makeMove(sq("h2"), sq("h3")));

        assertFalse(game.canRedo());
        assertFalse(game.redo());
        assertEquals(List.of("h2→h3"), game.getMoveHistory());
    }

    @Test
    void undoAndRedoWithoutHistoryReturnFalse() {
        assertFalse(game.undo());
        assertFalse(game.redo());
        assertFalse(game.canUndo());
        assertFalse(game.canRedo());
        assertTrue(game.getMoveHistory().isEmpty());
    }

    @Test
    void severalUndosWalkBackTheWholeHistory() {
        placeKings();
        Piece whitePawn = place("a2", Color.WHITE, new PawnMovementStrategy());
        Piece blackPawn = place("h7", Color.BLACK, new PawnMovementStrategy());
        Piece otherWhitePawn = place("b2", Color.WHITE, new PawnMovementStrategy());

        assertTrue(game.makeMove(sq("a2"), sq("a3")));   // blancas
        assertTrue(game.makeMove(sq("h7"), sq("h6")));   // negras
        assertTrue(game.makeMove(sq("b2"), sq("b3")));   // blancas
        assertEquals(List.of("a2→a3", "h7→h6", "b2→b3"), game.getMoveHistory());
        assertEquals(Color.BLACK, game.getCurrentTurn());

        assertTrue(game.undo());
        assertTrue(game.undo());
        assertTrue(game.undo());
        assertFalse(game.undo());

        assertEquals(whitePawn, board.getSquare(sq("a2")).getPiece());
        assertEquals(blackPawn, board.getSquare(sq("h7")).getPiece());
        assertEquals(otherWhitePawn, board.getSquare(sq("b2")).getPiece());
        assertEquals(Color.WHITE, game.getCurrentTurn());
        assertTrue(game.getMoveHistory().isEmpty());
        assertFalse(game.canUndo());
        assertTrue(game.canRedo());

        // Rehacer los tres devuelve exactamente el estado anterior.
        assertTrue(game.redo());
        assertTrue(game.redo());
        assertTrue(game.redo());
        assertEquals(List.of("a2→a3", "h7→h6", "b2→b3"), game.getMoveHistory());
        assertEquals(Color.BLACK, game.getCurrentTurn());
        assertFalse(game.canRedo());
    }

    @Test
    void failedMoveDoesNotEnterTheHistory() {
        placeKings();
        place("a2", Color.WHITE, new PawnMovementStrategy());

        assertFalse(game.makeMove(sq("a2"), sq("a5")));
        assertTrue(game.getMoveHistory().isEmpty());
        assertFalse(game.canUndo());
    }
}
