// Tipos que viajan entre el backend Java (adaptador HTTP) y esta UI.
// Reflejan el JSON que produce ChessHttpServer.

export type Color = 'white' | 'black';

export type PieceType = 'pawn' | 'rook' | 'knight' | 'bishop' | 'queen' | 'king';

export interface Position {
  row: number; // 0 = fila superior (rank 8 en 8x8)
  column: number; // 0 = columna "a"
}

export interface Piece {
  color: Color;
  type: PieceType;
}

/** Promoción pendiente: el peón ya llegó a la última fila y falta elegir la pieza. */
export interface PromotionState {
  required: boolean;
  row: number | null;
  column: number | null;
  color: Color | null;
  options: PieceType[];
}

/** Estado completo de la partida, tal como lo devuelve GET /api/state. */
export interface GameState {
  width: number;
  height: number;
  board: (Piece | null)[][];
  currentTurn: Color;
  capturedPieces: Piece[];
  check: { white: boolean; black: boolean };
  checkmate?: { white: boolean; black: boolean };
  gameOver?: boolean;
  promotion?: PromotionState;
  /** Hay al menos una jugada completa para deshacer. */
  canUndo?: boolean;
  /** Hay al menos una jugada deshecha para rehacer. */
  canRedo?: boolean;
  /** Registro de jugadas que mantiene el núcleo (fuente única de verdad). */
  history?: string[];
}

export interface MoveResult {
  success: boolean;
  state: GameState;
}

/**
 * Puerto del lado del frontend: la UI depende de esta interfaz, no de fetch ni de URLs.
 * Hoy la implementa HttpChessGameClient (habla con el núcleo Java por HTTP);
 * se podría reemplazar por otra implementación sin tocar los componentes.
 */
export interface ChessGameClient {
  getState(): Promise<GameState>;
  getPossibleDestinations(from: Position): Promise<Position[]>;
  makeMove(from: Position, to: Position): Promise<MoveResult>;
  promote(pieceType: PieceType): Promise<MoveResult>;
  undo(): Promise<MoveResult>;
  redo(): Promise<MoveResult>;
  resetGame(): Promise<GameState>;
}
