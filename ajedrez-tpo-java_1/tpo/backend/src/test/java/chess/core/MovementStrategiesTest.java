package chess.core;

import static chess.core.TestBoards.names;
import static chess.core.TestBoards.place;
import static chess.core.TestBoards.sq;
import static org.junit.jupiter.api.Assertions.*;

import chess.core.model.Board;
import chess.core.model.Color;
import chess.core.movement.BishopMovementStrategy;
import chess.core.movement.KingMovementStrategy;
import chess.core.movement.KnightMovementStrategy;
import chess.core.movement.PawnMovementStrategy;
import chess.core.movement.QueenMovementStrategy;
import chess.core.movement.RookMovementStrategy;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MovementStrategiesTest {
    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    @Nested
    class Pawn {
        private final PawnMovementStrategy pawn = new PawnMovementStrategy();

        @Test
        void whitePawnOnStartRowAdvancesOneOrTwo() {
            place(board, "e2", Color.WHITE, pawn);
            assertEquals(List.of("e3", "e4"), names(pawn.getPossibleDestinations(sq("e2"), board, Color.WHITE)));
        }

        @Test
        void blackPawnMovesDownwards() {
            place(board, "d7", Color.BLACK, pawn);
            assertEquals(List.of("d5", "d6"), names(pawn.getPossibleDestinations(sq("d7"), board, Color.BLACK)));
        }

        @Test
        void cannotAdvanceTwoOutsideStartRow() {
            place(board, "e3", Color.WHITE, pawn);
            assertEquals(List.of("e4"), names(pawn.getPossibleDestinations(sq("e3"), board, Color.WHITE)));
        }

        @Test
        void isBlockedByAnyPieceInFrontAndCannotCaptureForward() {
            place(board, "e2", Color.WHITE, pawn);
            place(board, "e3", Color.BLACK, new RookMovementStrategy());
            assertTrue(pawn.getPossibleDestinations(sq("e2"), board, Color.WHITE).isEmpty());
        }

        @Test
        void doubleStepBlockedIfSecondSquareOccupied() {
            place(board, "e2", Color.WHITE, pawn);
            place(board, "e4", Color.BLACK, new RookMovementStrategy());
            assertEquals(List.of("e3"), names(pawn.getPossibleDestinations(sq("e2"), board, Color.WHITE)));
        }

        @Test
        void capturesDiagonallyOnlyEnemies() {
            place(board, "e4", Color.WHITE, pawn);
            place(board, "d5", Color.BLACK, new RookMovementStrategy()); // rival: se captura
            place(board, "f5", Color.WHITE, new RookMovementStrategy()); // propia: no
            assertEquals(List.of("d5", "e5"), names(pawn.getPossibleDestinations(sq("e4"), board, Color.WHITE)));
        }
    }

    @Nested
    class Rook {
        private final RookMovementStrategy rook = new RookMovementStrategy();

        @Test
        void reaches14SquaresOnEmptyBoard() {
            place(board, "d4", Color.WHITE, rook);
            assertEquals(14, rook.getPossibleDestinations(sq("d4"), board, Color.WHITE).size());
        }

        @Test
        void stopsBeforeOwnPiecesAndOnEnemies() {
            place(board, "a1", Color.WHITE, rook);
            place(board, "a4", Color.WHITE, new PawnMovementStrategy());
            place(board, "d1", Color.BLACK, new PawnMovementStrategy());
            assertEquals(List.of("a2", "a3", "b1", "c1", "d1"),
                    names(rook.getPossibleDestinations(sq("a1"), board, Color.WHITE)));
        }

        @Test
        void doesNotMoveDiagonally() {
            place(board, "d4", Color.WHITE, rook);
            assertFalse(names(rook.getPossibleDestinations(sq("d4"), board, Color.WHITE)).contains("e5"));
        }
    }

    @Nested
    class Bishop {
        private final BishopMovementStrategy bishop = new BishopMovementStrategy();

        @Test
        void reaches13SquaresFromCenter() {
            place(board, "d4", Color.WHITE, bishop);
            assertEquals(13, bishop.getPossibleDestinations(sq("d4"), board, Color.WHITE).size());
        }

        @Test
        void cannotJumpOverPieces() {
            place(board, "c1", Color.WHITE, bishop);
            place(board, "d2", Color.WHITE, new PawnMovementStrategy());
            assertEquals(List.of("a3", "b2"), names(bishop.getPossibleDestinations(sq("c1"), board, Color.WHITE)));
        }
    }

    @Nested
    class Queen {
        private final QueenMovementStrategy queen = new QueenMovementStrategy();

        @Test
        void combinesRookAndBishop() {
            place(board, "d4", Color.WHITE, queen);
            assertEquals(27, queen.getPossibleDestinations(sq("d4"), board, Color.WHITE).size());
        }

        @Test
        void doesNotMoveLikeAKnight() {
            place(board, "d4", Color.WHITE, queen);
            assertFalse(names(queen.getPossibleDestinations(sq("d4"), board, Color.WHITE)).contains("e6"));
        }
    }

    @Nested
    class Knight {
        private final KnightMovementStrategy knight = new KnightMovementStrategy();

        @Test
        void hasEightLShapedMovesFromCenter() {
            place(board, "d4", Color.WHITE, knight);
            assertEquals(List.of("b3", "b5", "c2", "c6", "e2", "e6", "f3", "f5"),
                    names(knight.getPossibleDestinations(sq("d4"), board, Color.WHITE)));
        }

        @Test
        void onlyTwoMovesFromCorner() {
            place(board, "a1", Color.WHITE, knight);
            assertEquals(List.of("b3", "c2"), names(knight.getPossibleDestinations(sq("a1"), board, Color.WHITE)));
        }

        @Test
        void jumpsOverSurroundingPieces() {
            place(board, "b1", Color.WHITE, knight);
            for (String s : List.of("a2", "b2", "c2")) place(board, s, Color.WHITE, new PawnMovementStrategy());
            assertEquals(List.of("a3", "c3", "d2"),
                    names(knight.getPossibleDestinations(sq("b1"), board, Color.WHITE)));
        }
    }

    @Nested
    class King {
        private final KingMovementStrategy king = new KingMovementStrategy();

        @Test
        void movesOneSquareInAnyDirection() {
            place(board, "e4", Color.WHITE, king);
            assertEquals(8, king.getPossibleDestinations(sq("e4"), board, Color.WHITE).size());
        }

        @Test
        void cannotLandOnOwnPiecesButCanCaptureEnemies() {
            place(board, "e1", Color.WHITE, king);
            place(board, "d1", Color.WHITE, new QueenMovementStrategy());
            place(board, "f1", Color.BLACK, new BishopMovementStrategy());
            assertEquals(List.of("d2", "e2", "f1", "f2"),
                    names(king.getPossibleDestinations(sq("e1"), board, Color.WHITE)));
        }
    }
}
