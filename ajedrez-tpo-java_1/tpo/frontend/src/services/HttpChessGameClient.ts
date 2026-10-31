import type { ChessGameClient, GameState, MoveResult, PieceType, Position } from '../types/chess';

/**
 * Implementación HTTP de ChessGameClient: consume la API JSON del backend Java.
 * La URL base por defecto es "/api" (Vite la redirige a http://localhost:8080 en desarrollo).
 */
export class HttpChessGameClient implements ChessGameClient {
  private readonly baseUrl: string;

  constructor(baseUrl: string = import.meta.env.VITE_API_URL ?? '/api') {
    this.baseUrl = baseUrl;
  }

  public getState(): Promise<GameState> {
    return this.request<GameState>('/state');
  }

  public async getPossibleDestinations(from: Position): Promise<Position[]> {
    const response = await this.request<{ destinations: Position[] }>(
      `/moves?row=${from.row}&column=${from.column}`
    );
    return response.destinations;
  }

  public makeMove(from: Position, to: Position): Promise<MoveResult> {
    return this.request<MoveResult>('/move', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        fromRow: from.row,
        fromColumn: from.column,
        toRow: to.row,
        toColumn: to.column,
      }),
    });
  }

  public promote(pieceType: PieceType): Promise<MoveResult> {
    return this.request<MoveResult>('/promote', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ type: pieceType }),
    });
  }

  public undo(): Promise<MoveResult> {
    return this.request<MoveResult>('/undo', { method: 'POST' });
  }

  public redo(): Promise<MoveResult> {
    return this.request<MoveResult>('/redo', { method: 'POST' });
  }

  public resetGame(): Promise<GameState> {
    return this.request<GameState>('/reset', { method: 'POST' });
  }

  private async request<T>(path: string, init?: RequestInit): Promise<T> {
    const response = await fetch(`${this.baseUrl}${path}`, init);
    if (!response.ok) {
      throw new Error(`HTTP ${response.status} en ${path}`);
    }
    return (await response.json()) as T;
  }
}
