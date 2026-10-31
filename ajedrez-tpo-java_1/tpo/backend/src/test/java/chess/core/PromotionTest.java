package chess.core;

import static chess.core.TestBoards.sq;
import static org.junit.jupiter.api.Assertions.*;

import chess.core.game.ChessGame;
import chess.core.game.CommandManager;
import chess.core.model.Board;
import chess.core.model.Color;
import chess.core.model.Piece;
import chess.core.model.PieceFactory;
import chess.core.model.PieceType;
import chess.core.model.Position;
import chess.core.model.StandardPieceFactory;
import chess.core.movement.KingMovementStrategy;
import chess.core.movement.KnightMovementStrategy;
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

class PromotionTest {
    private Board board;
    private ChessGame game;

    @BeforeEach
    void setUp() {
        board = new Board(8, 8);
        game = newGame(board, Color.WHITE);
    }

    private static ChessGame newGame(Board board, Color turn) {
        return new ChessGame(
                board,
                new TurnManager(turn),
                new MoveValidator(new CheckDetector()),
                new StandardPromotionPolicy(),
                new StandardPieceFactory(),
                new CommandManager());
    }

    private Piece place(String square, Color color, MovementStrategy strategy) {
        return TestBoards.place(board, square, color, strategy);
    }

    // ---------------------------------------------------------------- promocion basica

    @Test
    void whitePawnReachingLastRankLeavesAPendingPromotionAndKeepsTurn() {
        place("e1", Color.WHITE, new KingMovementStrategy());
        place("e8", Color.BLACK, new KingMovementStrategy());
        place("b7", Color.WHITE, new PawnMovementStrategy());

        assertTrue(game.makeMove(sq("b7"), sq("b8")));

        assertTrue(game.getPendingPromotion().isPresent());
        assertEquals(Color.WHITE, game.getCurrentTurn());
        assertEquals(Color.WHITE, game.getPendingPromotion().orElseThrow().getColor());
        assertEquals(sq("b8"), game.getPendingPromotion().orElseThrow().getPosition());
        assertEquals(
                List.of(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT),
                game.getPromotionOptions());
        assertFalse(game.isGameOver());
    }

    @Test
    void promoteReplacesPawnWithChosenPieceAndAdvancesTurn() {
        place("e1", Color.WHITE, new KingMovementStrategy());
        place("e8", Color.BLACK, new KingMovementStrategy());
        place("b7", Color.WHITE, new PawnMovementStrategy());
        assertTrue(game.makeMove(sq("b7"), sq("b8")));

        assertTrue(game.promote(PieceType.QUEEN));

        assertTrue(game.getPendingPromotion().isEmpty());
        assertEquals(Color.BLACK, game.getCurrentTurn());
        assertInstanceOf(QueenMovementStrategy.class,
                board.getSquare(sq("b8")).getPiece().getMovementStrategy());
        // La dama recien creada ya participa en la deteccion de jaque.
        assertTrue(game.isCheck(Color.BLACK));
    }

    @Test
    void blackPawnAlsoPromotesOnItsLastRank() {
        game = newGame(board, Color.BLACK);
        place("e1", Color.WHITE, new KingMovementStrategy());
        place("e8", Color.BLACK, new KingMovementStrategy());
        place("b2", Color.BLACK, new PawnMovementStrategy());

        assertTrue(game.makeMove(sq("b2"), sq("b1")));
        assertTrue(game.getPendingPromotion().isPresent());
        assertEquals(Color.BLACK, game.getCurrentTurn());

        assertTrue(game.promote(PieceType.KNIGHT));
        assertInstanceOf(KnightMovementStrategy.class,
                board.getSquare(sq("b1")).getPiece().getMovementStrategy());
        assertEquals(Color.WHITE, game.getCurrentTurn());
    }

    @Test
    void promotionByCaptureRegistersTheCapturedPiece() {
        place("e1", Color.WHITE, new KingMovementStrategy());
        place("e8", Color.BLACK, new KingMovementStrategy());
        place("b7", Color.WHITE, new PawnMovementStrategy());
        Piece blackRook = place("a8", Color.BLACK, new RookMovementStrategy());

        assertTrue(game.makeMove(sq("b7"), sq("a8")));
        assertEquals(List.of(blackRook), game.getCapturedPieces());
        assertTrue(game.getPendingPromotion().isPresent());

        assertTrue(game.promote(PieceType.QUEEN));
        assertInstanceOf(QueenMovementStrategy.class,
                board.getSquare(sq("a8")).getPiece().getMovementStrategy());
    }

    // ---------------------------------------------------------------- validaciones

    @Test
    void promoteRejectsPawnKingAndUnknownState() {
        assertFalse(game.promote(PieceType.QUEEN)); // no hay promocion pendiente
        assertTrue(game.getPromotionOptions().isEmpty());

        place("e1", Color.WHITE, new KingMovementStrategy());
        place("e8", Color.BLACK, new KingMovementStrategy());
        place("b7", Color.WHITE, new PawnMovementStrategy());
        assertTrue(game.makeMove(sq("b7"), sq("b8")));

        assertFalse(game.promote(PieceType.PAWN));
        assertFalse(game.promote(PieceType.KING));
        assertTrue(game.getPendingPromotion().isPresent());
        assertEquals(Color.WHITE, game.getCurrentTurn());

        assertTrue(game.promote(PieceType.ROOK));
        assertInstanceOf(RookMovementStrategy.class,
                board.getSquare(sq("b8")).getPiece().getMovementStrategy());
    }

    @Test
    void whilePromotionIsPendingNoOtherMoveIsAccepted() {
        place("e1", Color.WHITE, new KingMovementStrategy());
        place("e8", Color.BLACK, new KingMovementStrategy());
        place("b7", Color.WHITE, new PawnMovementStrategy());
        assertTrue(game.makeMove(sq("b7"), sq("b8")));

        assertFalse(game.makeMove(sq("e1"), sq("e2")));
        assertEquals(Color.WHITE, game.getCurrentTurn());
        assertTrue(game.getPendingPromotion().isPresent());

        assertTrue(game.promote(PieceType.BISHOP));
        assertTrue(game.makeMove(sq("e8"), sq("d8")));
    }

    @Test
    void nonPawnReachingLastRankDoesNotPromote() {
        place("e1", Color.WHITE, new KingMovementStrategy());
        place("e8", Color.BLACK, new KingMovementStrategy());
        place("a7", Color.WHITE, new RookMovementStrategy());

        assertTrue(game.makeMove(sq("a7"), sq("a8")));

        assertTrue(game.getPendingPromotion().isEmpty());
        assertEquals(Color.BLACK, game.getCurrentTurn());
    }

    @Test
    void normalPawnMoveDoesNotPromote() {
        place("e1", Color.WHITE, new KingMovementStrategy());
        place("e8", Color.BLACK, new KingMovementStrategy());
        place("a2", Color.WHITE, new PawnMovementStrategy());

        assertTrue(game.makeMove(sq("a2"), sq("a3")));

        assertTrue(game.getPendingPromotion().isEmpty());
        assertEquals(Color.BLACK, game.getCurrentTurn());
    }

    // ---------------------------------------------------------------- tableros no 8x8

    @Test
    void promotionUsesBoardHeightInsteadOfHardcodedEight() {
        Board small = new Board(5, 5);
        ChessGame smallGame = newGame(small, Color.WHITE);
        small.getSquare(new Position(3, 4)).setPiece(new Piece(Color.WHITE, new KingMovementStrategy()));
        small.getSquare(new Position(0, 4)).setPiece(new Piece(Color.BLACK, new KingMovementStrategy()));
        small.getSquare(new Position(1, 0)).setPiece(new Piece(Color.WHITE, new PawnMovementStrategy()));

        assertTrue(smallGame.makeMove(new Position(1, 0), new Position(0, 0)));
        assertTrue(smallGame.getPendingPromotion().isPresent());
        assertTrue(smallGame.promote(PieceType.QUEEN));
        assertInstanceOf(QueenMovementStrategy.class,
                small.getSquare(new Position(0, 0)).getPiece().getMovementStrategy());
    }

    // ---------------------------------------------------------------- factory

    @Test
    void standardFactorySharesStatelessStrategies() {
        PieceFactory factory = new StandardPieceFactory();

        Piece whiteQueen = factory.create(Color.WHITE, PieceType.QUEEN);
        Piece blackQueen = factory.create(Color.BLACK, PieceType.QUEEN);

        assertInstanceOf(QueenMovementStrategy.class, whiteQueen.getMovementStrategy());
        assertSame(whiteQueen.getMovementStrategy(), blackQueen.getMovementStrategy());
    }
}
