package chess;

import chess.adapters.console.ConsoleUI;
import chess.adapters.http.ChessHttpServer;
import chess.core.game.ChessGame;
import chess.core.model.Board;
import chess.core.model.Color;
import chess.core.model.Piece;
import chess.core.model.Position;
import chess.core.movement.BishopMovementStrategy;
import chess.core.movement.KingMovementStrategy;
import chess.core.movement.KnightMovementStrategy;
import chess.core.movement.MovementStrategy;
import chess.core.movement.PawnMovementStrategy;
import chess.core.movement.QueenMovementStrategy;
import chess.core.movement.RookMovementStrategy;
import chess.core.rules.CheckDetector;
import chess.core.rules.MoveValidator;
import chess.core.rules.TurnManager;
import java.io.IOException;

/**
 * RAIZ DE COMPOSICION: el unico lugar donde se hacen los new y se conectan las dependencias.
 * Cambiar el armado (otro tablero, otra UI) no obliga a modificar ninguna clase del nucleo.
 *
 * Uso:  (sin argumentos)  -> servidor HTTP en el puerto 8080 para la UI React
 *       console           -> juego por consola
 */
public final class ChessApplication {

    private ChessApplication() {
    }

    public static void main(String[] args) throws IOException {
        String mode = args.length > 0 ? args[0] : "http";
        if (mode.equalsIgnoreCase("console")) {
            ConsoleUI ui = new ConsoleUI(createStandardGame());
            ui.render();
            ui.processInput();
        } else {
            int port = args.length > 1 ? Integer.parseInt(args[1]) : 8080;
            new ChessHttpServer(ChessApplication::createStandardGame, port).start();
            System.out.println("Servidor de ajedrez escuchando en http://localhost:" + port + "/api/state");
        }
    }

    /** Arma una partida estandar lista para jugar, con todas sus dependencias inyectadas. */
    public static ChessGame createStandardGame() {
        Board board = new Board(8, 8);
        placeStandardPieces(board);
        MoveValidator moveValidator = new MoveValidator(new CheckDetector());
        return new ChessGame(board, new TurnManager(Color.WHITE), moveValidator);
    }

    /** Coloca la posicion inicial estandar del ajedrez en un tablero de 8x8. */
    public static void placeStandardPieces(Board board) {
        if (board.getWidth() != 8 || board.getHeight() != 8) {
            throw new IllegalArgumentException("The standard setup requires an 8x8 board");
        }
        // Las estrategias no tienen estado: una instancia por tipo se comparte entre piezas.
        MovementStrategy rook = new RookMovementStrategy();
        MovementStrategy knight = new KnightMovementStrategy();
        MovementStrategy bishop = new BishopMovementStrategy();
        MovementStrategy queen = new QueenMovementStrategy();
        MovementStrategy king = new KingMovementStrategy();
        MovementStrategy pawn = new PawnMovementStrategy();
        MovementStrategy[] backRank = {rook, knight, bishop, queen, king, bishop, knight, rook};

        for (int column = 0; column < 8; column++) {
            board.getSquare(new Position(0, column)).setPiece(new Piece(Color.BLACK, backRank[column]));
            board.getSquare(new Position(1, column)).setPiece(new Piece(Color.BLACK, pawn));
            board.getSquare(new Position(6, column)).setPiece(new Piece(Color.WHITE, pawn));
            board.getSquare(new Position(7, column)).setPiece(new Piece(Color.WHITE, backRank[column]));
        }
    }
}
