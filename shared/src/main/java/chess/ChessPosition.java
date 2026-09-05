package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    private int row;
    private int col;

    public ChessPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    // equality/hashing
    @Override public int hashCode() {
        return Objects.hash(this.row, this.col);
    }

    @Override public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;
        ChessPosition other = (ChessPosition)obj;
        return this.row == other.getRow() && this.col == other.getColumn();
    }

    // debugging help
    @Override public String toString() {
        return this.row + ", " + this.col;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return this.row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left column
     */
    public int getColumn() {
        return this.col;
    }

    // useful for piece moverules
    public ChessPosition withOffset(int row, int col) {
        return new ChessPosition(this.row + row, this.col + col);
    }
}
