package chess.core.game;

import chess.core.model.Board;
import chess.core.model.Color;
import chess.core.model.Move;
import chess.core.model.Piece;
import chess.core.model.PieceFactory;
import chess.core.model.PieceType;
import chess.core.model.Position;
import chess.core.rules.MoveValidator;
import chess.core.rules.PromotionPolicy;
import chess.core.rules.TurnManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * ChessGame (UML): implementa el puerto ChessGameAPI y coordina al resto.
 * No contiene reglas propias: valida con MoveValidator, mueve con Board, alterna con TurnManager.
 * Todas sus dependencias entran por constructor (sin new escondidos) => testeable con dobles.
 */
public class ChessGame implements ChessGameAPI {
    private final Board board;
    private final TurnManager turnManager;
    private final MoveValidator moveValidator;
    private final PromotionPolicy promotionPolicy;
    private final PieceFactory pieceFactory;
    private final CommandManager commandManager;
    private final HalfmoveClock halfmoveClock;
    private final List<Piece> capturedPieces;
    private PendingPromotion pendingPromotion;
    /** Jugada empezada (promocion pendiente) que todavia no entro al historial. */
    private MoveCommand pendingCommand;

    public ChessGame(Board board,
                     TurnManager turnManager,
                     MoveValidator moveValidator,
                     PromotionPolicy promotionPolicy,
                     PieceFactory pieceFactory,
                     CommandManager commandManager,
                     HalfmoveClock halfmoveClock) {
        this.board = Objects.requireNonNull(board);
        this.turnManager = Objects.requireNonNull(turnManager);
        this.moveValidator = Objects.requireNonNull(moveValidator);
        this.promotionPolicy = Objects.requireNonNull(promotionPolicy);
        this.pieceFactory = Objects.requireNonNull(pieceFactory);
        this.commandManager = Objects.requireNonNull(commandManager);
        this.halfmoveClock = Objects.requireNonNull(halfmoveClock);
        this.capturedPieces = new ArrayList<>();
    }

    public ChessGame(Board board,
                     TurnManager turnManager,
                     MoveValidator moveValidator,
                     PromotionPolicy promotionPolicy,
                     PieceFactory pieceFactory,
                     CommandManager commandManager) {
        this(board, turnManager, moveValidator, promotionPolicy, pieceFactory,
                commandManager, new HalfmoveClock());
    }

    @Override
    public boolean makeMove(Position from, Position to) {
        // Mientras hay una promocion pendiente, la partida esta "congelada"
        if (pendingPromotion != null) return false;

        if (from == null || to == null) return false;
        if (!board.isWithinBounds(from) || !board.isWithinBounds(to)) return false;

        Piece movedPiece = board.getSquare(from).getPiece();
        if (movedPiece == null) return false;

        // Si la partida ya finalizo por jaque mate, no se permiten mas jugadas
        if (isGameOver()) {
            return false;
        }

        Move move = new Move(from, to, movedPiece, board.getSquare(to).getPiece());
        if (!moveValidator.isValidMove(move, board, turnManager.getCurrentTurn())) {
            return false;
        }

        // La jugada se modela como un comando reversible (patron Command).
        boolean promotion = promotionPolicy.applies(movedPiece, to, board);
        MoveCommand command = new MoveCommand(
                board,
                turnManager,
                capturedPieces,
                halfmoveClock,
                movedPiece,
                from,
                to,
                move.getCapturedPiece(),
                promotion);

        // Primera mitad: mover la pieza y registrar la captura (todavia no cambia el turno).
        command.apply();

        // El peon ya llego a la ultima fila: la jugada no termina hasta elegir la pieza,
        // asi que el comando TAMPOCO entra al historial todavia.
        if (promotion) {
            pendingCommand = command;
            pendingPromotion = new PendingPromotion(movedPiece.getColor(), to);
            return true;
        }

        command.complete(null, null);
        commandManager.push(command);
        return true;
    }

    @Override
    public boolean promote(PieceType type) {
        if (pendingPromotion == null || !promotionPolicy.isAllowed(type)) return false;

        Position position = pendingPromotion.getPosition();
        Piece pawn = board.getSquare(position).getPiece();
        // Defensa: la casilla debe seguir teniendo el peon que origino la promocion.
        if (pawn == null || !promotionPolicy.applies(pawn, position, board)) {
            pendingPromotion = null;
            pendingCommand = null;
            return false;
        }

        Piece promoted = pieceFactory.create(pendingPromotion.getColor(), type);
        MoveCommand command = pendingCommand;
        if (command != null) {
            // Segunda mitad: reemplaza el peon, pasa el turno y recien ahora se apila.
            command.complete(promoted, type);
            commandManager.push(command);
        } else {
            // Camino defensivo: mismo efecto aunque no haya comando asociado.
            board.getSquare(position).setPiece(promoted);
            turnManager.switchTurn();
        }

        pendingCommand = null;
        pendingPromotion = null;
        return true;
    }

    /**
     * Deshacer/rehacer quedan bloqueados mientras hay una promocion pendiente:
     * la jugada todavia no esta completa y la partida esta "congelada".
     */
    @Override
    public boolean undo() {
        if (pendingPromotion != null) return false;
        return commandManager.undo();
    }

    @Override
    public boolean redo() {
        if (pendingPromotion != null) return false;
        return commandManager.redo();
    }

    @Override
    public boolean canUndo() {
        return pendingPromotion == null && commandManager.canUndo();
    }

    @Override
    public boolean canRedo() {
        return pendingPromotion == null && commandManager.canRedo();
    }

    @Override
    public List<String> getMoveHistory() {
        return commandManager.history();
    }

    @Override
    public Optional<PendingPromotion> getPendingPromotion() {
        return Optional.ofNullable(pendingPromotion);
    }

    @Override
    public List<PieceType> getPromotionOptions() {
        return pendingPromotion == null ? List.of() : promotionPolicy.allowedChoices();
    }

    @Override
    public boolean isCheck(Color color) {
        return moveValidator.getCheckDetector().isInCheck(color, board);
    }

    /**
     * Mientras hay una promocion pendiente la partida no puede darse por terminada:
     * recien despues de elegir la pieza se evalua el jaque mate con la pieza nueva.
     */
    @Override
    public boolean isCheckmate(Color color) {
        return pendingPromotion == null
                && moveValidator.getCheckDetector().isCheckmate(color, board);
    }

    /** Mientras hay promocion pendiente no se declaran tablas: la jugada todavia no termino. */
    @Override
    public boolean isStalemate(Color color) {
        return pendingPromotion == null
                && moveValidator.getCheckDetector().isStalemate(color, board);
    }

    @Override
    public boolean isFiftyMoveRuleDraw() {
        return pendingPromotion == null && halfmoveClock.isFiftyMoveRuleDraw();
    }

    @Override
    public int getHalfmoveClock() {
        return halfmoveClock.getHalfmoves();
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
