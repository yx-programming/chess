package chess;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    public static final int BOARD_SIZE = 8;
    // left to right, the piece layouts of the standard board for white
    private static final ChessPiece.PieceType[] PIECE_LAYOUT = {
        ChessPiece.PieceType.ROOK,
        ChessPiece.PieceType.KNIGHT,
        ChessPiece.PieceType.BISHOP,
        ChessPiece.PieceType.QUEEN,
        ChessPiece.PieceType.KING,
        ChessPiece.PieceType.BISHOP,
        ChessPiece.PieceType.KNIGHT,
        ChessPiece.PieceType.ROOK,
    };
    private ChessPiece[][] board;
    private Map<ChessGame.TeamColor, ChessPosition> kings;
    
    public ChessBoard() {
        this.board = new ChessPiece[BOARD_SIZE][BOARD_SIZE];
        kings = new HashMap<ChessGame.TeamColor, ChessPosition>();
    }

    // equals and hashcode
    @Override public int hashCode() {
        return Objects.hash(this.board[0][0], this.board[0][BOARD_SIZE - 1]);
    }
    @Override public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        ChessBoard other = (ChessBoard)obj;
        return Arrays.deepEquals(this.board, other.getBoard());
    }

    public ChessPiece[][] getBoard() {
        return this.board;
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        // shift back one because the positions are 1-8, not 0-7
        int col = position.getColumn() - 1;
        // flip the row so that 1 goes on the bottom
        int row = BOARD_SIZE - position.getRow();
        this.board[row][col] = piece;
        if (piece == null) {
            return;
        }
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            kings.put(piece.getTeamColor(), position);
        }
    }

    public ChessPosition getKing(ChessGame.TeamColor color) {
        return kings.get(color);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ChessPiece[] row : this.board) {
            for (ChessPiece piece : row) {
                if (piece == null) {
                    sb.append(' ');
                    continue;
                }
                var type = piece.getPieceType();
                char c = (type == ChessPiece.PieceType.KING) ? 'K' :
                    (type == ChessPiece.PieceType.QUEEN) ? 'Q' :
                    (type == ChessPiece.PieceType.PAWN) ? 'P' :
                    (type == ChessPiece.PieceType.ROOK) ? 'R' :
                    (type == ChessPiece.PieceType.KNIGHT) ? 'N' :
                    (type == ChessPiece.PieceType.BISHOP) ? 'B' : ' ';
                if (piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                    c += 'a' - 'A';
                }
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        int col = position.getColumn() - 1;
        int row = BOARD_SIZE - position.getRow();
        return isValid(position) ? this.board[row][col] : null;
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        for (int i = 1; i <= BOARD_SIZE; i++) {
            // place pawns 1 row from each side
            this.addPiece(new ChessPosition(2, i), new ChessPiece(
                ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            this.addPiece(new ChessPosition(BOARD_SIZE - 1, i), new ChessPiece(
                ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
            // power pieces
            this.addPiece(new ChessPosition(1, i), new ChessPiece(
                ChessGame.TeamColor.WHITE, PIECE_LAYOUT[i - 1]));
            this.addPiece(new ChessPosition(BOARD_SIZE, i), new ChessPiece(
                ChessGame.TeamColor.BLACK, PIECE_LAYOUT[i - 1]));
        }
    }

    // useful for piece moverules
    public boolean isValid(ChessPosition pos) {
        int col = pos.getColumn();
        int row = pos.getRow();
        return col >= 1 && col <= BOARD_SIZE && row >= 1 && row <= BOARD_SIZE; 
    }
}
