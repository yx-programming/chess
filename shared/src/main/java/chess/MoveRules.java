package chess;

import java.util.ArrayList;
import java.util.List;

interface MoveRule {
    List<ChessMove> getMoves(ChessBoard board, ChessPosition pos);
}

class PawnRule implements MoveRule {
    private static final ChessPiece.PieceType[] promotionTypes = {
        ChessPiece.PieceType.ROOK,    
        ChessPiece.PieceType.KNIGHT,    
        ChessPiece.PieceType.BISHOP,    
        ChessPiece.PieceType.QUEEN,    
    };
    @Override
    public List<ChessMove> getMoves(ChessBoard board, ChessPosition pos) {
        var moves = new ArrayList<ChessMove>();
        final ChessPiece piece = board.getPiece(pos);
        if (piece == null) return moves;
        boolean isWhite = piece.getTeamColor() == ChessGame.TeamColor.WHITE;
        int dir = isWhite ? 1 : -1;
        int startRow = isWhite ? 2 : ChessBoard.BOARD_SIZE - 1;
        int finalRow = isWhite ? ChessBoard.BOARD_SIZE : 1;
        int[][] offsets = {{dir, 0}, {dir, 1}, {dir, -1}};
        for (int[] offset : offsets) {
            var target = pos.withOffset(offset[0], offset[1]);
            if (board.isValid(target)) {
                var otherPiece = board.getPiece(target);
                if ((otherPiece == null && offset[1] == 0) ||
                        (otherPiece != null &&
                        otherPiece.getTeamColor() != piece.getTeamColor() &&
                        offset[1] != 0)) {
                    // promove the piece if necessary
                    if (target.getRow() == finalRow) {
                        for (var type : promotionTypes) {
                            moves.add(new ChessMove(pos, target, type));
                        }
                    } else moves.add(new ChessMove(pos, target, null));
                    // check for a 2 square jump option
                    if (offset[1] == 0 && pos.getRow() == startRow) {
                        var oneMore = target.withOffset(dir, 0);
                        if (board.isValid(oneMore) &&
                                board.getPiece(oneMore) == null) {
                            moves.add(new ChessMove(pos, oneMore, null));
                        }
                    }
                }
            }            
        }
        return moves;
    }
}

class KingRule implements MoveRule {
    @Override
    public List<ChessMove> getMoves(ChessBoard board, ChessPosition pos) {
        var moves = new ArrayList<ChessMove>();
        int[][] offsets = { {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1},
            {0, -1}, {1, -1} };
        for (int[] offset : offsets) {
            var target = pos.withOffset(offset[0], offset[1]);
            if (board.isValid(target)) {
                var piece = board.getPiece(pos);
                if (piece == null) return moves;
                var otherPiece = board.getPiece(target);
                if (otherPiece == null ||
                        otherPiece.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(pos, target, null));
                }
            }
        }
        return moves;
    }
}
