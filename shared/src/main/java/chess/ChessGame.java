package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor turn;
    private ChessBoard board;

    public ChessGame() {
        turn = TeamColor.WHITE;
        board = new ChessBoard();
        board.resetBoard();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ChessGame)) {
            return false;
        }
        ChessGame other = (ChessGame)obj;
        return board.equals(other.board) && turn == other.turn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(turn, board, turn);
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Sets which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        turn = team;
    }

    /**
     * Enum identifying the 3 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    // we put this in a function because it seems cleaner than replicating the
    // logic in both functions
    private void movePiece(ChessMove move) {
        var start = move.getStartPosition();
        var end = move.getEndPosition();

        var piece = board.getPiece(start);
        board.addPiece(end, move.getPromotionPiece() != null ?
            new ChessPiece(piece.getTeamColor(),
            move.getPromotionPiece()) : piece);
        board.addPiece(start, null);
    }

    /**
     * Gets all valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        var piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        var moves = piece.pieceMoves(board, startPosition);
        var color = piece.getTeamColor();
        var valid = new HashSet<ChessMove>();
        for (var move : moves) {
            // check each move for validity
            var possibleCapture = board.getPiece(move.getEndPosition());
            movePiece(move);
            if (!isInCheck(color)) {
                valid.add(move);
            }
            movePiece(move.reverse());
            // restore any pawn state or potential captures from the move
            board.addPiece(move.getEndPosition(), possibleCapture);
        }
        return valid;
    }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (move == null) {
            throw new InvalidMoveException("empty move");
        }
        if (!(board.isValid(move.getStartPosition()) &&
                board.isValid(move.getEndPosition()))) {
            throw new InvalidMoveException("move out of bounds");
        }
        var start = move.getStartPosition();
        var moves = validMoves(start);
        if (moves == null) {
            throw new InvalidMoveException("no valid moves");
        }
        var color = board.getPiece(start).getTeamColor();
        if (color != turn) {
            throw new InvalidMoveException("not your turn");
        }
        if (moves.contains(move)) {
            movePiece(move);
        } else {
            throw new InvalidMoveException("illegal move");
        }
        turn = turn == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    // I'm taking a similar approach to what I did for moves, but in reverse
    // We'll check all of the possible locations that the king could be hit from
    private static int[][] offsets = { {1, -2}, {2, -1}, {2, 1}, {1, 2},
        {-1, 2}, {-2, 1}, {-2, -1}, {-1, -2} }; // for checking knights
    private static int[][] directions = { {1, 0}, {1, 1}, {0, 1}, {-1, 1},
        {-1, 0}, {-1, -1}, {0, -1}, {1, -1} }; // for checking other power pieces
    private Collection<ChessPosition> getThreats(ChessPosition piecePos) {
        var threats = new HashSet<ChessPosition>();
        var startPiece = board.getPiece(piecePos);
        if (startPiece == null) {
            return threats;
        }
        var teamColor = startPiece.getTeamColor();
        int dir = teamColor == TeamColor.WHITE ? 1 : -1;       
        // checking for pawns
        for (int i = -1; i <= 1; i += 2) {
            var pos = piecePos.withOffset(dir, i);
            if (!board.isValid(pos)) {
                continue;
            }
            var piece = board.getPiece(pos);
            if (piece != null && piece.getTeamColor() != teamColor &&
                    piece.getPieceType() == ChessPiece.PieceType.PAWN) {
                threats.add(pos);
            }
        }
        // knight checks
        for (var offset : offsets) {
            var pos = piecePos.withOffset(offset[0], offset[1]);
            if (!board.isValid(pos)) {
                continue;
            }
            var piece = board.getPiece(pos);
            if (piece != null && piece.getTeamColor() != teamColor &&
                    piece.getPieceType() == ChessPiece.PieceType.KNIGHT) {
                threats.add(pos);
            }
        }
        // power piece checks
        for (var direction : directions) {
            var pos = piecePos.withOffset(direction[0], direction[1]);
            // we check the king because we also want to check if the
            // position would be valid if a king moved there
            // (since kings can't be near each other)
            var possibleKing = board.getPiece(pos);
            if (possibleKing != null && possibleKing.getPieceType() ==
                    ChessPiece.PieceType.KING) {
                threats.add(pos);
            }
            // the king can only move one space, but everything else moves more
            while (board.isValid(pos)) {
                var piece = board.getPiece(pos);
                if (piece == null) {
                    pos = pos.withOffset(direction[0], direction[1]);
                    continue;
                }
                if (piece.getTeamColor() == teamColor ||
                        piece.getPieceType() == ChessPiece.PieceType.KNIGHT) {
                    break;
                }
                // queen can attack from any direction
                if (piece.getPieceType() == ChessPiece.PieceType.QUEEN) {
                    threats.add(pos);
                // rook can only on horizontal
                } else if ((direction[0] == 0 || direction[1] == 0) &&
                        piece.getPieceType() == ChessPiece.PieceType.ROOK) {
                    threats.add(pos);
                // and bishop only diagonal
                } else if (direction[0] != 0 && direction[1] != 0 &&
                        piece.getPieceType() == ChessPiece.PieceType.BISHOP) {
                    threats.add(pos);
                }
                break;
           }
        }
        return threats;
    }
    public boolean isInCheck(TeamColor teamColor) {
        var kingPos = board.getKing(teamColor);
        var threats = getThreats(kingPos);
        return threats.size() > 0;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        var kingPos = board.getKing(teamColor);
        var threats = getThreats(kingPos);
        if (threats.size() == 0) {
            return false;
        }
        int resolvedThreats = 0;
        for (var threat : threats) {
            var savingThreats = getThreats(threat);
            for (var savingThreat : savingThreats) {
                if (validMoves(savingThreat).size() > 0) {
                    resolvedThreats++;
                }
            }
        }
        return resolvedThreats < threats.size();
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        // if none of that color's pieces can move, then we're in stalemate
        for (int row = 0; row < ChessBoard.BOARD_SIZE; row++) {
            for (int col = 0; col < ChessBoard.BOARD_SIZE; col++) {
                var pos = new ChessPosition(row, col);
                var piece = board.getPiece(pos);
                if (piece == null) {
                    continue;
                }
                var color = piece.getTeamColor();
                if (color == teamColor &&
                        validMoves(pos).size() > 0) {
                    return false;
                }
            }
        }
        return !isInCheck(teamColor);
    }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
