package chess.adapters.http;

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
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ADAPTADOR HTTP (JSON) para la interfaz web en React.
 * Igual que ConsoleUI, depende SOLO del puerto ChessGameAPI: el nucleo no sabe que existe.
 * Usa el HttpServer que trae el JDK (sin frameworks ni dependencias externas).
 *
 * Endpoints:
 *   GET  /api/state                      -> estado completo de la partida
 *   GET  /api/moves?row=R&column=C       -> destinos posibles de la pieza (para resaltar en la UI)
 *   POST /api/move  {fromRow,fromColumn,toRow,toColumn} -> {success, state}
 *   POST /api/reset                      -> nueva partida
 */
public class ChessHttpServer {
    private final Supplier<ChessGameAPI> gameFactory;
    private final int port;
    private ChessGameAPI gameEngine;
    private HttpServer server;

    /** gameFactory: como crear una partida nueva (se inyecta desde la raiz de composicion). */
    public ChessHttpServer(Supplier<ChessGameAPI> gameFactory, int port) {
        this.gameFactory = gameFactory;
        this.port = port;
        this.gameEngine = gameFactory.get();
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/state", exchange -> handle(exchange, "GET", this::stateEndpoint));
        server.createContext("/api/moves", exchange -> handle(exchange, "GET", this::movesEndpoint));
        server.createContext("/api/move", exchange -> handle(exchange, "POST", this::moveEndpoint));
        server.createContext("/api/reset", exchange -> handle(exchange, "POST", this::resetEndpoint));
        server.start();
    }

    public void stop() {
        if (server != null) server.stop(0);
    }

    // ---------------------------------------------------------------- endpoints

    private String stateEndpoint(HttpExchange exchange) {
        return stateJson();
    }

    private String movesEndpoint(HttpExchange exchange) {
        Map<String, Integer> query = parseQuery(exchange.getRequestURI().getRawQuery());
        Integer row = query.get("row");
        Integer column = query.get("column");
        StringBuilder json = new StringBuilder("{\"destinations\":[");
        if (row != null && column != null) {
            Board board = gameEngine.getBoard();
            Position from = new Position(row, column);
            if (board.isWithinBounds(from)) {
                Piece piece = board.getSquare(from).getPiece();
                if (piece != null && piece.getColor() == gameEngine.getCurrentTurn()) {
                    // Destinos segun las reglas propias de la pieza (la validacion final la hace makeMove)
                    List<Position> destinations = piece.getValidMoves(from, board);
                    for (int i = 0; i < destinations.size(); i++) {
                        if (i > 0) json.append(',');
                        json.append(positionJson(destinations.get(i)));
                    }
                }
            }
        }
        return json.append("]}").toString();
    }

    private String moveEndpoint(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Integer fromRow = jsonInt(body, "fromRow");
        Integer fromColumn = jsonInt(body, "fromColumn");
        Integer toRow = jsonInt(body, "toRow");
        Integer toColumn = jsonInt(body, "toColumn");
        boolean success = fromRow != null && fromColumn != null && toRow != null && toColumn != null
                && gameEngine.makeMove(new Position(fromRow, fromColumn), new Position(toRow, toColumn));
        return "{\"success\":" + success + ",\"state\":" + stateJson() + "}";
    }

    private String resetEndpoint(HttpExchange exchange) {
        gameEngine = gameFactory.get();
        return stateJson();
    }

    // ---------------------------------------------------------------- serializacion JSON

    private String stateJson() {
        Board board = gameEngine.getBoard();
        StringBuilder json = new StringBuilder("{");
        json.append("\"width\":").append(board.getWidth()).append(',');
        json.append("\"height\":").append(board.getHeight()).append(',');
        json.append("\"board\":[");
        for (int row = 0; row < board.getHeight(); row++) {
            if (row > 0) json.append(',');
            json.append('[');
            for (int column = 0; column < board.getWidth(); column++) {
                if (column > 0) json.append(',');
                Piece piece = board.getSquare(new Position(row, column)).getPiece();
                json.append(piece == null ? "null" : pieceJson(piece));
            }
            json.append(']');
        }
        json.append("],");
        json.append("\"currentTurn\":\"").append(colorName(gameEngine.getCurrentTurn())).append("\",");
        json.append("\"capturedPieces\":[");
        List<Piece> captured = gameEngine.getCapturedPieces();
        for (int i = 0; i < captured.size(); i++) {
            if (i > 0) json.append(',');
            json.append(pieceJson(captured.get(i)));
        }
        json.append("],");
        json.append("\"check\":{\"white\":").append(gameEngine.isCheck(Color.WHITE))
                .append(",\"black\":").append(gameEngine.isCheck(Color.BLACK)).append('}');
        return json.append('}').toString();
    }

    private String pieceJson(Piece piece) {
        return "{\"color\":\"" + colorName(piece.getColor()) + "\",\"type\":\"" + pieceType(piece) + "\"}";
    }

    private String positionJson(Position position) {
        return "{\"row\":" + position.getRow() + ",\"column\":" + position.getColumn() + "}";
    }

    private String colorName(Color color) {
        return color == Color.WHITE ? "white" : "black";
    }

    /** Traduccion estrategia -> nombre que entiende el frontend (detalle de presentacion). */
    private String pieceType(Piece piece) {
        Object strategy = piece.getMovementStrategy();
        if (strategy instanceof KingMovementStrategy) return "king";
        if (strategy instanceof QueenMovementStrategy) return "queen";
        if (strategy instanceof RookMovementStrategy) return "rook";
        if (strategy instanceof BishopMovementStrategy) return "bishop";
        if (strategy instanceof KnightMovementStrategy) return "knight";
        if (strategy instanceof PawnMovementStrategy) return "pawn";
        return "unknown";
    }

    // ---------------------------------------------------------------- infraestructura HTTP

    private interface Endpoint {
        String respond(HttpExchange exchange) throws IOException;
    }

    /** Un solo hilo atiende pedidos (synchronized): la partida no se modifica en paralelo. */
    private synchronized void handle(HttpExchange exchange, String method, Endpoint endpoint) throws IOException {
        try {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!exchange.getRequestMethod().equalsIgnoreCase(method)) {
                send(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            send(exchange, 200, endpoint.respond(exchange));
        } catch (RuntimeException e) {
            send(exchange, 400, "{\"error\":\"Bad request\"}");
        } finally {
            exchange.close();
        }
    }

    private void send(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** Lee un entero de un JSON plano, p. ej. "fromRow": 6. */
    private Integer jsonInt(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?\\d+)").matcher(json);
        return matcher.find() ? Integer.valueOf(matcher.group(1)) : null;
    }

    private Map<String, Integer> parseQuery(String query) {
        java.util.HashMap<String, Integer> result = new java.util.HashMap<>();
        if (query == null) return result;
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2 && parts[1].matches("-?\\d+")) {
                result.put(parts[0], Integer.valueOf(parts[1]));
            }
        }
        return result;
    }
}
