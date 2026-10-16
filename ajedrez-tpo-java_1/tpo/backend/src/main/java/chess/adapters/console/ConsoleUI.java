package chess.adapters.console;

import chess.core.game.ChessGameAPI;
import chess.core.model.Board;
import chess.core.model.Color;
import chess.core.model.Piece;
import chess.core.model.Position;
import chess.core.movement.BishopMovementStrategy;
import chess.core.movement.KingMovementStrategy;
import chess.core.movement.KnightMovementStrategy;
import chess.core.movement.PawnMovementStrategy;
import chess.core.movement.QueenMovementStrategy;
import chess.core.movement.RookMovementStrategy;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ConsoleUI (UML): ADAPTADOR de entrada/salida por consola.
 * Depende solo del puerto ChessGameAPI (inyectado por constructor) y de Position.
 * El nucleo no sabe que esta clase existe: las dependencias apuntan hacia adentro.
 */
public class ConsoleUI {
    private final ChessGameAPI gameEngine;

    public ConsoleUI(ChessGameAPI gameEngine) {
        this.gameEngine = gameEngine;
    }

    /** Dibuja el tablero y el estado. Mayusculas = blancas, minusculas = negras. */
    public void render() {
        Board board = gameEngine.getBoard();

        // Traduccion estrategia -> letra: detalle de presentacion, por eso vive en el adaptador.
        Function<Piece, String> symbolOf = piece -> {
            Object strategy = piece.getMovementStrategy();
            String letter;
            if (strategy instanceof KingMovementStrategy) letter = "K";
            else if (strategy instanceof QueenMovementStrategy) letter = "Q";
            else if (strategy instanceof RookMovementStrategy) letter = "R";
            else if (strategy instanceof BishopMovementStrategy) letter = "B";
            else if (strategy instanceof KnightMovementStrategy) letter = "N";
            else if (strategy instanceof PawnMovementStrategy) letter = "P";
            else letter = "?";
            return piece.getColor() == Color.WHITE ? letter : letter.toLowerCase();
        };

        StringBuilder files = new StringBuilder("    ");
        for (int column = 0; column < board.getWidth(); column++) {
            files.append((char) ('a' + column)).append(' ');
        }
        String border = "   " + "-".repeat(board.getWidth() * 2 + 1);

        StringBuilder out = new StringBuilder("\n").append(files).append('\n').append(border).append('\n');
        for (int row = 0; row < board.getHeight(); row++) {
            String rank = String.format("%2d", board.getHeight() - row);
            out.append(rank).append(" | ");
            for (int column = 0; column < board.getWidth(); column++) {
                Piece piece = board.getSquare(new Position(row, column)).getPiece();
                out.append(piece == null ? "." : symbolOf.apply(piece)).append(' ');
            }
            out.append("| ").append(rank).append('\n');
        }
        out.append(border).append('\n').append(files).append("\n\n");

        out.append("Turno: ")
                .append(gameEngine.getCurrentTurn() == Color.WHITE ? "Blancas (MAYUSCULAS)" : "Negras (minusculas)")
                .append('\n');
        if (gameEngine.isCheck(Color.WHITE)) out.append("JAQUE! El rey blanco esta atacado.\n");
        if (gameEngine.isCheck(Color.BLACK)) out.append("JAQUE! El rey negro esta atacado.\n");

        List<Piece> captured = gameEngine.getCapturedPieces();
        if (!captured.isEmpty()) {
            out.append("Capturadas:");
            for (Piece piece : captured) out.append(' ').append(symbolOf.apply(piece));
            out.append('\n');
        }
        System.out.print(out);
    }

    /**
     * Lee jugadas de la entrada estandar en formato "e2 e4" (o "e2e4") hasta "salir" o fin de entrada.
     */
    public void processInput() {
        Board board = gameEngine.getBoard();
        Pattern movePattern = Pattern.compile("^([a-z])(\\d{1,2})\\s*([a-z])(\\d{1,2})$");

        // Convierte columna (letra) + numero de fila (rank) en Position, segun el tamano del tablero.
        java.util.function.BiFunction<String, String, Position> toPosition = (file, rank) ->
                new Position(board.getHeight() - Integer.parseInt(rank), file.charAt(0) - 'a');

        System.out.println("Ingresa jugadas como \"e2 e4\". Escribi \"ayuda\" o \"salir\".");
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
        while (true) {
            System.out.print("\nJugada > ");
            if (!scanner.hasNextLine()) break;
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("salir") || input.equals("exit")) break;
            if (input.equals("ayuda") || input.equals("help")) {
                System.out.println("Formato: <origen> <destino>, por ejemplo \"e2 e4\" o \"g1f3\".");
                continue;
            }

            Matcher matcher = movePattern.matcher(input);
            if (!matcher.matches()) {
                System.out.println("Formato invalido. Ejemplo: e2 e4");
                continue;
            }
            Position from = toPosition.apply(matcher.group(1), matcher.group(2));
            Position to = toPosition.apply(matcher.group(3), matcher.group(4));

            if (gameEngine.makeMove(from, to)) {
                render();
            } else {
                System.out.println("Movimiento invalido.");
            }
        }
        System.out.println("\nPartida finalizada.");
    }
}
