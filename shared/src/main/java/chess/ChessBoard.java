package chess;

import java.util.Arrays;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private static final int BOARD_SIZE = 8;
    // left to right, the piece layouts of the standard board for white
    private static final ChessPiece.PieceType[] pieceLayout = {
        ChessPiece.PieceType.ROOK,
        ChessPiece.PieceType.KNIGHT,
        ChessPiece.PieceType.BISHOP,
        ChessPiece.PieceType.KING,
        ChessPiece.PieceType.QUEEN,
        ChessPiece.PieceType.BISHOP,
        ChessPiece.PieceType.KNIGHT,
        ChessPiece.PieceType.ROOK,
    };
    private ChessPiece[][] board;
    
    public ChessBoard() {
        this.board = new ChessPiece[BOARD_SIZE][BOARD_SIZE];
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        // shift back one because the positions are 1-8, not 0-7
        int row = position.getRow() - 1;
        int col = position.getColumn() - 1;
        this.board[row][col] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        int row = position.getRow() - 1;
        int col = position.getColumn() - 1;
        return this.board[row][col];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        // place pawns 1 row from each side
        for (int i = 0; i < BOARD_SIZE; i++) {
            this.board[1][i] = new ChessPiece(ChessGame.TeamColor.BLACK,
                ChessPiece.PieceType.PAWN);
            this.board[BOARD_SIZE - 2][i] = new ChessPiece(
                ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        }
        // place power pieces
        for (int i = 0; i < BOARD_SIZE; i++) {
            this.board[BOARD_SIZE - 1][i] = new ChessPiece(
                ChessGame.TeamColor.WHITE, pieceLayout[i]);
            // black's layout is actually mirrored from white's, although
            // from the perspective of each player it's the same
            this.board[0][BOARD_SIZE - 1 - i] = new ChessPiece(
                ChessGame.TeamColor.BLACK, pieceLayout[i]);
        }
    }
}
