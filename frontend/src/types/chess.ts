// Tipos según el Diagrama de Clases (Versión 0)

export type Color = 'white' | 'black';

export type PieceType = 'pawn' | 'rook' | 'knight' | 'bishop' | 'queen' | 'king';

export interface Position {
  row: number; // 0 a 7
  column: number; // 0 a 7
}

export interface Piece {
  color: Color;
  type: PieceType;
}

export interface Square {
  position: Position;
  piece: Piece | null;
}

export interface Move {
  from: Position;
  to: Position;
  movedPiece: Piece;
  capturedPiece?: Piece | null;
}

// Interfaz del Núcleo expuesta hacia los Adaptadores/UI (ChessGameAPI en el UML)
export interface ChessGameAPI {
  makeMove(from: Position, to: Position): boolean;
  isCheck(color: Color): boolean;
  getCurrentTurn(): Color;
  getCapturedPieces(): Piece[];
  getBoard(): (Piece | null)[][];
  getPossibleDestinations?(from: Position): Position[];
  resetGame?(): void;
}
