import type { ChessGameAPI, Color, Piece, Position } from '../types/chess';

/**
 * MockChessGame implementa ChessGameAPI (del diagrama UML).
 * Permite que el frontend funcione de forma autónoma e interactiva,
 * y podrá ser reemplazado directamente por la instancia real del núcleo en Java/C#/TS/Python.
 */
export class MockChessGame implements ChessGameAPI {
  private board: (Piece | null)[][];
  private currentTurn: Color;
  private capturedPieces: Piece[];

  constructor() {
    this.currentTurn = 'white';
    this.capturedPieces = [];
    this.board = this.initializeBoard();
  }

  private initializeBoard(): (Piece | null)[][] {
    const grid: (Piece | null)[][] = Array(8)
      .fill(null)
      .map(() => Array(8).fill(null));

    // Peones
    for (let c = 0; c < 8; c++) {
      grid[1][c] = { color: 'black', type: 'pawn' };
      grid[6][c] = { color: 'white', type: 'pawn' };
    }

    // Piezas mayores
    const backRow: Piece['type'][] = ['rook', 'knight', 'bishop', 'queen', 'king', 'bishop', 'knight', 'rook'];
    for (let c = 0; c < 8; c++) {
      grid[0][c] = { color: 'black', type: backRow[c] };
      grid[7][c] = { color: 'white', type: backRow[c] };
    }

    return grid;
  }

  public getBoard(): (Piece | null)[][] {
    return this.board.map(row => [...row]);
  }

  public getCurrentTurn(): Color {
    return this.currentTurn;
  }

  public getCapturedPieces(): Piece[] {
    return [...this.capturedPieces];
  }

  public isCheck(color: Color): boolean {
    // Verificación básica para demostración en frontend
    const kingPos = this.findKing(color);
    if (!kingPos) return false;

    const opponentColor: Color = color === 'white' ? 'black' : 'white';
    for (let r = 0; r < 8; r++) {
      for (let c = 0; c < 8; c++) {
        const piece = this.board[r][c];
        if (piece && piece.color === opponentColor) {
          const moves = this.getBasicMoves({ row: r, column: c }, piece);
          if (moves.some(m => m.row === kingPos.row && m.column === kingPos.column)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private findKing(color: Color): Position | null {
    for (let r = 0; r < 8; r++) {
      for (let c = 0; c < 8; c++) {
        const p = this.board[r][c];
        if (p && p.type === 'king' && p.color === color) {
          return { row: r, column: c };
        }
      }
    }
    return null;
  }

  public getPossibleDestinations(from: Position): Position[] {
    const piece = this.board[from.row][from.column];
    if (!piece || piece.color !== this.currentTurn) return [];

    return this.getBasicMoves(from, piece).filter(dest => {
      // Regla básica: no capturar piezas propias
      const target = this.board[dest.row][dest.column];
      return !target || target.color !== piece.color;
    });
  }

  public makeMove(from: Position, to: Position): boolean {
    const piece = this.board[from.row][from.column];
    if (!piece) return false;
    if (piece.color !== this.currentTurn) return false;

    const validMoves = this.getPossibleDestinations(from);
    const isValid = validMoves.some(m => m.row === to.row && m.column === to.column);

    if (!isValid) return false;

    // Ejecutar captura si hay pieza contraria
    const target = this.board[to.row][to.column];
    if (target) {
      this.capturedPieces.push(target);
    }

    // Mover pieza
    this.board[to.row][to.column] = piece;
    this.board[from.row][from.column] = null;

    // Alternar turno
    this.currentTurn = this.currentTurn === 'white' ? 'black' : 'white';
    return true;
  }

  public resetGame(): void {
    this.board = this.initializeBoard();
    this.currentTurn = 'white';
    this.capturedPieces = [];
  }

  private getBasicMoves(from: Position, piece: Piece): Position[] {
    const moves: Position[] = [];
    const { row: r, column: c } = from;

    const addIfValid = (targetR: number, targetC: number) => {
      if (targetR >= 0 && targetR < 8 && targetC >= 0 && targetC < 8) {
        const dest = this.board[targetR][targetC];
        if (!dest) {
          moves.push({ row: targetR, column: targetC });
          return true; // libre, continúa raycast
        } else if (dest.color !== piece.color) {
          moves.push({ row: targetR, column: targetC });
          return false; // captura, corta raycast
        }
      }
      return false;
    };

    if (piece.type === 'pawn') {
      const dir = piece.color === 'white' ? -1 : 1;
      const startRow = piece.color === 'white' ? 6 : 1;
      
      // Avance 1 casilla
      if (r + dir >= 0 && r + dir < 8 && !this.board[r + dir][c]) {
        moves.push({ row: r + dir, column: c });
        // Avance doble inicial
        if (r === startRow && !this.board[r + 2 * dir][c]) {
          moves.push({ row: r + 2 * dir, column: c });
        }
      }
      // Capturas diagonales
      for (const dc of [-1, 1]) {
        const nr = r + dir;
        const nc = c + dc;
        if (nr >= 0 && nr < 8 && nc >= 0 && nc < 8) {
          const target = this.board[nr][nc];
          if (target && target.color !== piece.color) {
            moves.push({ row: nr, column: nc });
          }
        }
      }
    } else if (piece.type === 'knight') {
      const knightOffsets = [
        [-2, -1], [-2, 1], [-1, -2], [-1, 2],
        [1, -2], [1, 2], [2, -1], [2, 1]
      ];
      for (const [dr, dc] of knightOffsets) {
        addIfValid(r + dr, c + dc);
      }
    } else if (piece.type === 'bishop' || piece.type === 'queen') {
      const diags = [[-1, -1], [-1, 1], [1, -1], [1, 1]];
      for (const [dr, dc] of diags) {
        let step = 1;
        while (addIfValid(r + dr * step, c + dc * step)) {
          step++;
        }
      }
    }

    if (piece.type === 'rook' || piece.type === 'queen') {
      const straights = [[-1, 0], [1, 0], [0, -1], [0, 1]];
      for (const [dr, dc] of straights) {
        let step = 1;
        while (addIfValid(r + dr * step, c + dc * step)) {
          step++;
        }
      }
    }

    if (piece.type === 'king') {
      const offsets = [
        [-1, -1], [-1, 0], [-1, 1],
        [0, -1],           [0, 1],
        [1, -1],  [1, 0],  [1, 1]
      ];
      for (const [dr, dc] of offsets) {
        addIfValid(r + dr, c + dc);
      }
    }

    return moves;
  }
}
