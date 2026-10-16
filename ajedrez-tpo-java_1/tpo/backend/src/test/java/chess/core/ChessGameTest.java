package chess.core;

import static chess.core.TestBoards.sq;
import static org.junit.jupiter.api.Assertions.*;

import chess.ChessApplication;
import chess.core.game.ChessGame;
import chess.core.game.ChessGameAPI;
import chess.core.model.Board;
import chess.core.model.Color;
import chess.core.model.Piece;
import chess.core.model.Position;
import chess.core.movement.KingMovementStrategy;
import chess.core.movement.PawnMovementStrategy;
import chess.core.movement.QueenMovementStrategy;
import chess.core.movement.RookMovementStrategy;
import chess.core.rules.CheckDetector;
import chess.core.rules.MoveValidator;
import chess.core.rules.TurnManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChessGameTest {
    private ChessGame game;

    @BeforeEach
    void setUp() {
        game = ChessApplication.createStandardGame();
    }

    /** Ejecuta jugadas "e2 e4"; el test falla si alguna es rechazada. */
    private static void play(ChessGameAPI game, String... moves) {
        for (String move : moves) {
            String[] parts = move.split(" ");
            assertTrue(game.makeMove(sq(parts[0]), sq(parts[1])), "move rejected: " + move);
        }
    }

    // ---------------------------------------------------------------- estado inicial

    @Test
    void startsWithWhiteToMoveNoCapturesNoCheck() {
        assertEquals(Color.WHITE, game.getCurrentTurn());
        assertTrue(game.getCapturedPieces().isEmpty());
        assertFalse(game.isCheck(Color.WHITE));
        assertFalse(game.isCheck(Color.BLACK));
    }

    @Test
    void has32PiecesInStandardSquares() {
        Board board = game.getBoard();
        int count = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (board.getSquare(new Position(r, c)).isOccupied()) count++;
            }
        }
        assertEquals(32, count);
        Piece whiteKing = board.getSquare(sq("e1")).getPiece();
        assertEquals(Color.WHITE, whiteKing.getColor());
        assertInstanceOf(KingMovementStrategy.class, whiteKing.getMovementStrategy());
        assertInstanceOf(QueenMovementStrategy.class, board.getSquare(sq("d8")).getPiece().getMovementStrategy());
    }

    // ---------------------------------------------------------------- turnos

    @Test
    void alternatesTurnsAfterEachValidMove() {
        play(game, "e2 e4");
        assertEquals(Color.BLACK, game.getCurrentTurn());
        play(game, "e7 e5");
        assertEquals(Color.WHITE, game.getCurrentTurn());
    }

    @Test
    void rejectsMovingOpponentPieceAndKeepsTurn() {
        assertFalse(game.makeMove(sq("e7"), sq("e5")));
        assertEquals(Color.WHITE, game.getCurrentTurn());
    }

    // ---------------------------------------------------------------- movimientos invalidos

    @Test
    void rejectsMoveForbiddenByPieceRulesAndLeavesBoardUnchanged() {
        Piece rook = game.getBoard().getSquare(sq("a1")).getPiece();
        assertFalse(game.makeMove(sq("a1"), sq("a4"))); // la torre esta bloqueada por su peon
        assertSame(rook, game.getBoard().getSquare(sq("a1")).getPiece());
        assertEquals(Color.WHITE, game.getCurrentTurn());
    }

    @Test
    void rejectsMovingFromEmptySquare() {
        assertFalse(game.makeMove(sq("e4"), sq("e5")));
    }

    @Test
    void rejectsPositionsOutOfBoard() {
        assertFalse(game.makeMove(new Position(6, 4), new Position(-1, 4)));
        assertFalse(game.makeMove(new Position(9, 9), new Position(5, 4)));
        assertFalse(game.makeMove(null, new Position(5, 4)));
    }

    @Test
    void rejectsCapturingOwnPiece() {
        assertFalse(game.makeMove(sq("d1"), sq("d2")));
    }

    @Test
    void knightCanJumpAtTheStart() {
        play(game, "g1 f3");
    }

    // ---------------------------------------------------------------- capturas

    @Test
    void captureRemovesPieceFromBoardAndRegistersIt() {
        // Arrange
        play(game, "e2 e4", "d7 d5");
        Piece blackPawn = game.getBoard().getSquare(sq("d5")).getPiece();
        // Act
        play(game, "e4 d5");
        // Assert
        assertEquals(List.of(blackPawn), game.getCapturedPieces());
        assertEquals(Color.WHITE, game.getBoard().getSquare(sq("d5")).getPiece().getColor());
        assertFalse(game.getBoard().getSquare(sq("e4")).isOccupied());
    }

    @Test
    void capturedListCannotBeAlteredFromOutside() {
        play(game, "e2 e4", "d7 d5", "e4 d5");
        assertThrows(UnsupportedOperationException.class, () -> game.getCapturedPieces().clear());
        assertEquals(1, game.getCapturedPieces().size());
    }

    // ---------------------------------------------------------------- jaque

    @Test
    void detectsCheck() {
        play(game, "e2 e4", "f7 f6", "d1 h5"); // 1.e4 f6 2.Dh5+
        assertTrue(game.isCheck(Color.BLACK));
        assertFalse(game.isCheck(Color.WHITE));
    }

    @Test
    void whileInCheckOnlyMovesThatResolveItAreAccepted() {
        play(game, "e2 e4", "f7 f6", "d1 h5");
        assertFalse(game.makeMove(sq("a7"), sq("a6"))); // no resuelve el jaque
        assertEquals(Color.BLACK, game.getCurrentTurn());
        play(game, "g7 g6"); // tapa el jaque
        assertFalse(game.isCheck(Color.BLACK));
    }

    @Test
    void pinnedPieceCannotMoveIfThatExposesItsKing() {
        // Arrange: 1.e4 e6 2.d4 Ab4+ 3.c3 (tapa el jaque; el peon de c3 queda clavado)
        play(game, "e2 e4", "e7 e6", "d2 d4", "f8 b4");
        assertTrue(game.isCheck(Color.WHITE));
        play(game, "c2 c3", "a7 a6");
        // Act
        boolean moved = game.makeMove(sq("c3"), sq("c4"));
        // Assert
        assertFalse(moved);
        assertFalse(game.isCheck(Color.WHITE));
    }

    // ---------------------------------------------------------------- inyeccion de dependencias

    @Test
    void canBeAssembledWithAnyBoardAndStartingTurn() {
        // Arrange: posicion armada a mano, empiezan las negras
        Board board = new Board();
        board.getSquare(sq("e8")).setPiece(new Piece(Color.BLACK, new KingMovementStrategy()));
        board.getSquare(sq("e1")).setPiece(new Piece(Color.WHITE, new KingMovementStrategy()));
        board.getSquare(sq("d1")).setPiece(new Piece(Color.WHITE, new RookMovementStrategy()));
        ChessGame custom = new ChessGame(board, new TurnManager(Color.BLACK), new MoveValidator(new CheckDetector()));
        // Act & Assert
        assertEquals(Color.BLACK, custom.getCurrentTurn());
        assertFalse(custom.makeMove(sq("e8"), sq("d8"))); // d8 atacada por la torre
        assertTrue(custom.makeMove(sq("e8"), sq("f8")));
    }

    @Test
    void movementStrategiesAreSharedSafelyBetweenPieces() {
        play(game, "a2 a4", "h7 h5", "b2 b4");
        Piece a4 = game.getBoard().getSquare(sq("a4")).getPiece();
        Piece b4 = game.getBoard().getSquare(sq("b4")).getPiece();
        assertInstanceOf(PawnMovementStrategy.class, a4.getMovementStrategy());
        assertSame(a4.getMovementStrategy(), b4.getMovementStrategy());
    }
}
