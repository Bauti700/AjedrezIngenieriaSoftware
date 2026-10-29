# AjedrezIngenieriaSoftware

TPO de Ingeniería de Software (3.4.214): ajedrez con arquitectura hexagonal.
El **núcleo y los adaptadores están en Java** (`backend/`); la **interfaz gráfica en React** (`frontend/`)
se comunica con el núcleo por HTTP.

```
backend/   Java 17 + Maven (núcleo, ConsoleUI, adaptador HTTP, tests JUnit 5)
frontend/  React + TypeScript + Vite (UI gráfica)
```

## Estado del proyecto

### Alcance obligatorio (consigna, punto 3)

| Requisito | Estado | Dónde |
| :-- | :-- | :-- |
| Tablero de 8x8 | ✅ Hecho (el tamaño es configurable) | `Board` |
| Las 6 piezas con su movimiento propio | ✅ Hecho | `*MovementStrategy` |
| Alternancia de turnos | ✅ Hecho | `TurnManager` |
| Captura de piezas | ✅ Hecho | `ChessGame.makeMove`, `getCapturedPieces` |
| Validación de movimientos inválidos | ✅ Hecho | `MoveValidator` |
| Detección de jaque | ✅ Hecho | `CheckDetector` |

Además de lo pedido, un jugador no puede hacer movimientos que dejen (o pongan) a su propio rey
en jaque: piezas clavadas, rey que camina hacia una casilla atacada, o estar en jaque sin resolverlo.

### Alcance opcional (consigna, punto 4)

| Funcionalidad | Estado |
| :-- | :-- |
| Interfaz gráfica más allá del mínimo | ✅ Hecho (React + `ChessHttpServer`) |
| Jaque mate | ✅ Hecho (`CheckDetector.isCheckmate`, fin de partida en `ChessGame` y aviso en UIs) |
| Enroque | ❌ Pendiente |
| Captura al paso | ❌ Pendiente |
| Promoción de peón | ✅ Hecho: el núcleo congela la jugada hasta que se elige dama, torre, alfil o caballo |
| Tablas (ahogado, repetición, 50 movimientos) | ❌ Pendiente |
| Oponente con IA | ❌ Pendiente |
| Deshacer / rehacer | ❌ Pendiente (camino natural: patrón Command) |

### Entregables (consigna, punto 6)

| Entregable | Estado |
| :-- | :-- |
| Código fuente completo | ✅ Hecho |
| Tests unitarios del núcleo | ✅ Hecho: 85 tests JUnit 5, sin infraestructura |
| Diagrama de clases (UML) | ⚠️ Existe la Versión 0 aprobada; hay que decidir si se hace una Versión 1 con las diferencias listadas más abajo |
| Documento de justificación (qué / por qué / cuándo romperlo) | ❌ Pendiente (este README tiene notas que sirven de base) |
| Presentación (PPTX) | ❌ Pendiente |

### Limitaciones conocidas

- En la UI React, las casillas resaltadas muestran los movimientos según las reglas propias de la
  pieza; no excluyen los que dejarían al rey en jaque. Si se intenta uno de esos, el núcleo lo
  rechaza y aparece el aviso de movimiento inválido.
- El servidor HTTP maneja una sola partida, compartida por todos los navegadores conectados.
- El historial de jugadas lo arma el frontend; si se recarga la página, se pierde (la partida no).

## Requisitos

- JDK 17 o superior y Maven (IntelliJ ya trae Maven integrado).
- Node.js 20.19+ o 22.12+ (solo para el frontend).

## Cómo correrlo

**Tests del núcleo** (sin levantar ninguna infraestructura):
```bash
cd backend
mvn test
```

**Jugar por consola** (adaptador `ConsoleUI`, el del diagrama):
```bash
cd backend
mvn compile exec:java -Dexec.args="console"
```
Las jugadas se escriben `e2 e4`. Mayúsculas = blancas, minúsculas = negras.

**Jugar con la interfaz gráfica** (dos terminales):
```bash
# Terminal 1: backend (queda escuchando en http://localhost:8080)
cd backend
mvn compile exec:java

# Terminal 2: frontend
cd frontend
npm install
npm run dev        # abrir http://localhost:5173
```
Vite redirige `/api` al backend (ver `frontend/vite.config.ts`), así que no hace falta configurar CORS.

## Estructura del backend

```
chess/
  core/                    NÚCLEO: solo Java estándar (java.base), sin frameworks
    model/                 Color, Position, Piece, Square, Board, Move,
                           PieceType, PieceFactory, StandardPieceFactory
    movement/              MovementStrategy + 6 estrategias (patrón Strategy)
    rules/                 TurnManager, CheckDetector, MoveValidator,
                           PromotionPolicy, StandardPromotionPolicy
    game/                  ChessGameAPI (puerto), ChessGame y PendingPromotion
  adapters/
    console/ConsoleUI      adaptador de consola (está en el diagrama)
    http/ChessHttpServer   adaptador HTTP/JSON para React (NO está en el diagrama v0)
  ChessApplication         raíz de composición: único lugar con los `new` y el main
```

Las dependencias apuntan siempre hacia adentro: `core` no importa nada de `adapters`
ni de `ChessApplication`. El núcleo compila con `java.base` únicamente.

### API HTTP (`ChessHttpServer`)

| Método | Ruta | Respuesta |
| :-- | :-- | :-- |
| GET | `/api/state` | estado completo (tablero, turno, capturadas, jaque) |
| GET | `/api/moves?row=R&column=C` | destinos de la pieza según sus reglas |
| POST | `/api/move` `{fromRow, fromColumn, toRow, toColumn}` | `{success, state}` |
| POST | `/api/promote` `{type}` | `{success, state}`; `type` = `queen`, `rook`, `bishop` o `knight` |
| POST | `/api/reset` | estado de una partida nueva |

Cuando un peón llega a la última fila, `state.promotion.required` queda en `true` (con `row`,
`column`, `color` y `options`), el turno no avanza y `makeMove` rechaza otras jugadas hasta que
el adaptador llame a `POST /api/promote`. La consola pide la pieza por entrada estándar y la UI
React muestra un selector modal.

## Correspondencia con el Diagrama de Clases (Versión 0)

Cada clase e interfaz del diagrama es un archivo `.java` con el mismo nombre, atributos y métodos.
Para la defensa conviene conocer estos detalles:

- **Getters de atributos existentes.** El diagrama no los dibuja; por ejemplo no aparece
  `Move.getFrom()`, que es imprescindible. Se agregaron solo getters de atributos que ya
  están en el diagrama: `Square.getPosition`, `Board.getWidth/getHeight`,
  `Move.getFrom/getTo/getMovedPiece`, `Piece.getMovementStrategy`, `MoveValidator.getCheckDetector`.
- **`ChessGameAPI.getBoard()`** es el único método nuevo del puerto. Sin él, `ConsoleUI.render()`
  no tiene forma de saber qué dibujar.
- **`getCapturedPieces`**: en el diagrama la interfaz dice `getCapturePieces` y `ChessGame` dice
  `getCapturedPieces`; en Java tienen que llamarse igual. También se corrigió la ortografía de
  `movePiedce` / `Positiomn`.
- **`Position`** sobrescribe `equals/hashCode/toString` (heredados de `Object`) para comportarse
  como objeto de valor.
- **Identificación del rey**: `CheckDetector` lo reconoce por su estrategia
  (`instanceof KingMovementStrategy`), porque `Piece` solo tiene color y estrategia.
- **Lógica repetida en las estrategias**: torre, alfil y dama comparten el recorrido "deslizante",
  y caballo y rey el de "salto". Se implementó como método privado en cada clase para no agregar
  clases que no están en el diagrama. *Cuándo romperlo*: si se agregan más piezas deslizantes,
  conviene extraerlo a una clase auxiliar y reflejarla en el diagrama.
- **Clases fuera del diagrama**: `ChessApplication` (raíz de composición / `main`) y
  `ChessHttpServer` (adaptador para la UI React). Si se entrega la UI React, `ChessHttpServer`
  debería incorporarse al diagrama como un adaptador más, igual que `ConsoleUI`.
- **Promoción**: se agregaron `PieceType`, `PieceFactory`/`StandardPieceFactory`,
  `PromotionPolicy`/`StandardPromotionPolicy` y `PendingPromotion`. `ChessGame` recibe la
  política y la fábrica por constructor y, mientras hay una promoción pendiente, no cambia el
  turno ni acepta otras jugadas. Falta reflejar estas clases en el UML.
