package chess.core.game;

import chess.core.model.Board;
import chess.core.model.Color;
import chess.core.model.Move;
import chess.core.model.Piece;
import chess.core.model.Position;
import chess.core.rules.MoveValidator;
import chess.core.rules.TurnManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ChessGame (UML): implementa el puerto ChessGameAPI y coordina al resto.
 * No contiene reglas propias: valida con MoveValidator, mueve con Board, alterna con TurnManager.
 * Todas sus dependencias entran por constructor (sin new escondidos) => testeable con dobles.
 */
public class ChessGame implements ChessGameAPI {
    private final Board board;
    private final TurnManager turnManager;
    private final MoveValidator moveValidator;
    private final List<Piece> capturedPieces;

    public ChessGame(Board board, TurnManager turnManager, MoveValidator moveValidator) {
        this.board = Objects.requireNonNull(board);
        this.turnManager = Objects.requireNonNull(turnManager);
        this.moveValidator = Objects.requireNonNull(moveValidator);
        this.capturedPieces = new ArrayList<>();
    }

    @Override
    public boolean makeMove(Position from, Position to) {
        if (from == null || to == null) return false;
        if (!board.isWithinBounds(from) || !board.isWithinBounds(to)) return false;

        Piece movedPiece = board.getSquare(from).getPiece();
        if (movedPiece == null) return false;

        Move move = new Move(from, to, movedPiece, board.getSquare(to).getPiece());
        if (!moveValidator.isValidMove(move, board, turnManager.getCurrentTurn())) {
            return false;
        }

        if (move.getCapturedPiece() != null) {
            capturedPieces.add(move.getCapturedPiece());
        }
        board.movePiece(from, to);
        turnManager.switchTurn();
        return true;
    }

    @Override
    public boolean isCheck(Color color) {
        return moveValidator.getCheckDetector().isInCheck(color, board);
    }

    @Override
    public Color getCurrentTurn() {
        return turnManager.getCurrentTurn();
    }

    /** Devuelve una copia inmodificable: desde afuera no se puede alterar la lista interna. */
    @Override
    public List<Piece> getCapturedPieces() {
        return List.copyOf(capturedPieces);
    }

    /** Getter del atributo board. */
    @Override
    public Board getBoard() {
        return board;
    }
}
