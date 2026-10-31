package chess.core.game;

import chess.core.model.Board;
import chess.core.model.Piece;
import chess.core.model.PieceType;
import chess.core.model.Position;
import chess.core.rules.TurnManager;
import java.util.List;
import java.util.Objects;

/**
 * MoveCommand (UML): una jugada completa como objeto reversible (patron Command).
 *
 * Una jugada toca solo tres cosas: las casillas from/to, el turno y la lista de capturadas.
 * Por eso su inverso es exacto y no hace falta guardar un snapshot del tablero.
 *
 * La jugada puede completarse en dos fases porque la promocion necesita una eleccion del jugador:
 *   apply()               -> mueve la pieza y registra la captura (NO cambia el turno)
 *   complete(pieza, tipo) -> reemplaza el peon si promociono y pasa el turno
 * El comando recien se apila en el historial cuando esta completo.
 */
public final class MoveCommand implements Command {
    private final Board board;
    private final TurnManager turnManager;
    private final List<Piece> capturedPieces;
    private final Piece movedPiece;
    private final Position from;
    private final Position to;
    private final Piece capturedPiece;
    private final boolean promotion;
    private Piece promotedPiece;
    private String promotedLabel;

    public MoveCommand(Board board,
                       TurnManager turnManager,
                       List<Piece> capturedPieces,
                       Piece movedPiece,
                       Position from,
                       Position to,
                       Piece capturedPiece,
                       boolean promotion) {
        this.board = Objects.requireNonNull(board);
        this.turnManager = Objects.requireNonNull(turnManager);
        this.capturedPieces = Objects.requireNonNull(capturedPieces);
        this.movedPiece = Objects.requireNonNull(movedPiece);
        this.from = Objects.requireNonNull(from);
        this.to = Objects.requireNonNull(to);
        this.capturedPiece = capturedPiece;
        this.promotion = promotion;
    }

    /** Primera mitad: mueve la pieza y registra la captura. No cambia el turno. */
    public void apply() {
        board.getSquare(to).setPiece(movedPiece);
        board.getSquare(from).clear();
        if (capturedPiece != null) {
            capturedPieces.add(capturedPiece);
        }
    }

    /**
     * Segunda mitad: si la jugada era una promocion, reemplaza el peon por la pieza elegida.
     * En todos los casos pasa el turno. Se llama recien cuando la jugada esta terminada.
     */
    public void complete(Piece promoted, PieceType promotedType) {
        if (promotion) {
            promotedPiece = Objects.requireNonNull(promoted);
            if (promotedType != null) {
                promotedLabel = label(promotedType);
            }
            board.getSquare(to).setPiece(promotedPiece);
        }
        turnManager.switchTurn();
    }

    /** Rehacer: la jugada entera de una vez. Solo se invoca sobre comandos ya completos. */
    @Override
    public void execute() {
        apply();
        complete(promotedPiece, null);
    }

    /** Deshacer: inverso exacto de la jugada (no depende de snapshots). */
    @Override
    public void undo() {
        board.getSquare(from).setPiece(movedPiece);
        board.getSquare(to).setPiece(capturedPiece);
        if (capturedPiece != null) {
            capturedPieces.remove(capturedPiece);
        }
        // switchTurn es su propio inverso: cada comando apilado cambio el turno una sola vez.
        turnManager.switchTurn();
    }

    @Override
    public String describe() {
        String text = notation(from) + "→" + notation(to);
        return promotedLabel == null ? text : text + "=" + promotedLabel;
    }

    /** Notacion algebraica de la casilla, calculada con el alto real del tablero. */
    private String notation(Position position) {
        return "" + (char) ('a' + position.getColumn()) + (board.getHeight() - position.getRow());
    }

    private static String label(PieceType type) {
        return switch (type) {
            case QUEEN -> "DAMA";
            case ROOK -> "TORRE";
            case BISHOP -> "ALFIL";
            case KNIGHT -> "CABALLO";
            case PAWN -> "PEON";
            case KING -> "REY";
        };
    }
}
