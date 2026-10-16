package chess.core.rules;

import chess.core.model.Color;

/** TurnManager (UML): unica responsabilidad, saber de quien es el turno y alternarlo. */
public class TurnManager {
    private Color currentTurn;

    public TurnManager(Color initialTurn) {
        this.currentTurn = initialTurn;
    }

    /** Por defecto empiezan las blancas. */
    public TurnManager() {
        this(Color.WHITE);
    }

    public Color getCurrentTurn() {
        return currentTurn;
    }

    public void switchTurn() {
        currentTurn = currentTurn == Color.WHITE ? Color.BLACK : Color.WHITE;
    }
}
