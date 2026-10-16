package chess.core.movement;

import chess.core.model.Board;
import chess.core.model.Color;
import chess.core.model.Position;
import java.util.List;

/**
 * «interface» MovementStrategy (UML) - patron Strategy.
 * Cada implementacion encapsula la regla de movimiento de UN tipo de pieza.
 * Devuelve los destinos que la pieza alcanza segun SUS propias reglas
 * (dentro del tablero, sin atravesar piezas, sin caer sobre piezas propias).
 * NO considera jaque: eso es responsabilidad de CheckDetector / MoveValidator.
 */
public interface MovementStrategy {
    List<Position> getPossibleDestinations(Position from, Board board, Color color);
}
