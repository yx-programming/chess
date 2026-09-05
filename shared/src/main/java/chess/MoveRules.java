package chess;

import java.util.ArrayList;
import java.util.List;

interface MoveRule {
    List<ChessMove> getMoves(ChessBoard board, ChessPosition pos);
}

class PawnRule implements MoveRule {
    @Override
    public List<ChessMove> getMoves(ChessBoard board, ChessPosition pos) {
        return new ArrayList<>();
    }
}
