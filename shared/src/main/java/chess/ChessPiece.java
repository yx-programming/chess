package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor color;
    private ChessPiece.PieceType type;

    private static int[][] kingQueenSlots = { {1, 0}, {1, 1}, {0, 1}, {-1, 1},
        {-1, 0}, {-1, -1}, {0, -1}, {1, -1} };
    private static int[][] knightSlots = { {1, -2}, {2, -1}, {2, 1}, {1, 2},
        {-1, 2}, {-2, 1}, {-2, -1}, {-1, -2} };

    private static Map<PieceType, MoveRule> registry = Map.of(
        PieceType.PAWN, new PawnRule(),
        PieceType.ROOK, new PawnRule(),
        PieceType.KNIGHT, new StaticRule(knightSlots),
        PieceType.BISHOP, new PawnRule(),
        PieceType.QUEEN, new LineRule(kingQueenSlots),
        PieceType.KING, new StaticRule(kingQueenSlots)
    );

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.color = pieceColor;
        this.type = type;
    }

    // equality/hashcodes for use in collections
    @Override public int hashCode() {
        return Objects.hash(this.color, this.type);
    }

    @Override public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;
        ChessPiece other = (ChessPiece)obj;
        return this.color == other.getTeamColor() &&
            this.type == other.getPieceType();
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return registry.get(this.type).getMoves(board, myPosition);
     }
}
