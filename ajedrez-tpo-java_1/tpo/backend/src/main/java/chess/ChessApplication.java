package chess;

import chess.adapters.console.ConsoleUI;
import chess.adapters.http.ChessHttpServer;
import chess.core.game.ChessGame;
import chess.core.game.CommandManager;
import chess.core.model.Board;
import chess.core.model.Color;
import chess.core.model.PieceFactory;
import chess.core.model.PieceType;
import chess.core.model.Position;
import chess.core.model.StandardPieceFactory;
import chess.core.rules.CheckDetector;
import chess.core.rules.MoveValidator;
import chess.core.rules.StandardPromotionPolicy;
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
        PieceFactory pieceFactory = new StandardPieceFactory();
        placeStandardPieces(board, pieceFactory);
        MoveValidator moveValidator = new MoveValidator(new CheckDetector());
        return new ChessGame(
                board,
                new TurnManager(Color.WHITE),
                moveValidator,
                new StandardPromotionPolicy(),
                pieceFactory,
                new CommandManager());
    }

    /** Coloca la posicion inicial estandar del ajedrez en un tablero de 8x8. */
    public static void placeStandardPieces(Board board) {
        placeStandardPieces(board, new StandardPieceFactory());
    }

    /** Variante que recibe la fabrica: la raiz de composicion decide como crear las piezas. */
    public static void placeStandardPieces(Board board, PieceFactory pieceFactory) {
        if (board.getWidth() != 8 || board.getHeight() != 8) {
            throw new IllegalArgumentException("The standard setup requires an 8x8 board");
        }
        PieceType[] backRank = {
            PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
            PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        };

        for (int column = 0; column < 8; column++) {
            board.getSquare(new Position(0, column))
                    .setPiece(pieceFactory.create(Color.BLACK, backRank[column]));
            board.getSquare(new Position(1, column))
                    .setPiece(pieceFactory.create(Color.BLACK, PieceType.PAWN));
            board.getSquare(new Position(6, column))
                    .setPiece(pieceFactory.create(Color.WHITE, PieceType.PAWN));
            board.getSquare(new Position(7, column))
                    .setPiece(pieceFactory.create(Color.WHITE, backRank[column]));
        }
    }
}
