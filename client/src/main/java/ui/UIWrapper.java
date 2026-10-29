package ui;

import chess.*;

import java.util.*;

public class UIWrapper {
    private ChessBoard board;
    private List<String> log;
    private String title;
    private boolean boardFlipped;

    public UIWrapper(String title, boolean flipped) {
        this.title = title;
        log = new ArrayList<String>();
        board = null;
        boardFlipped = flipped;
    }

    public UIWrapper(String title) {
        this(title, false);
    }

    public static String WHITE = EscapeSequences.SET_TEXT_COLOR_WHITE;
    public static String GREEN = EscapeSequences.SET_TEXT_COLOR_GREEN;
    public static String RED = EscapeSequences.SET_TEXT_COLOR_RED;
    public static String YELLOW = EscapeSequences.SET_TEXT_COLOR_YELLOW;
    public static String BLUE = EscapeSequences.SET_TEXT_COLOR_BLUE;
    public void log(String message, String color) {
        // split the message into equal sized lines (70 lets us do anything in
        // 80 char width without issue (copied from SO, but I didn't feel like
        // writing a custom solution
        var lines = message.split("(?<=\\G.{70})");
        for (var line : lines) {
            // spaces add pading for the ui to look a bit nicer
            log.add(color + line + EscapeSequences.RESET_TEXT_COLOR);
        }
        redraw();
    }

    public void clear() {
        log.clear();
        redraw();
    }

    private Map<ChessPiece.PieceType, Character> pieceChars = Map.of(
        ChessPiece.PieceType.PAWN, 'P',
        ChessPiece.PieceType.KING, 'K',
        ChessPiece.PieceType.QUEEN, 'Q',
        ChessPiece.PieceType.ROOK, 'R',
        ChessPiece.PieceType.BISHOP, 'B',
        ChessPiece.PieceType.KNIGHT, 'N'
    );
    private String lightSquare = EscapeSequences.UNICODE_ESCAPE + "[48;5;136m";
    private String darkSquare = EscapeSequences.UNICODE_ESCAPE + "[48;5;130m";
    private String hiLightSquare = EscapeSequences.UNICODE_ESCAPE + "[48;5;99m";
    private String hiDarkSquare = EscapeSequences.UNICODE_ESCAPE + "[48;5;98m";
    private String[] squares = {lightSquare, darkSquare, hiLightSquare,
        hiDarkSquare};
    private char[] rows = {'1', '2', '3', '4', '5', '6', '7', '8' };

    public void redraw(ChessPosition highlightPos) {
        System.out.print(
            EscapeSequences.ERASE_SCREEN +
            EscapeSequences.moveCursorToLocation(1, 1));
            System.out.println(title);
            System.out.println(EscapeSequences.SET_BG_COLOR_LIGHT_GREY + 
            EscapeSequences.SET_TEXT_COLOR_BLACK + " abcdefgh " +
            EscapeSequences.RESET_TEXT_COLOR + EscapeSequences.RESET_BG_COLOR);
        for (int i = 0; i < ChessBoard.BOARD_SIZE; i++) {
            int row = boardFlipped ? i : (7 - i);
            System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY + 
                EscapeSequences.SET_TEXT_COLOR_BLACK + rows[row]);
            System.out.print(EscapeSequences.RESET_TEXT_COLOR +
                EscapeSequences.RESET_BG_COLOR);
            var highlightPositions = getHighlightedSquares(highlightPos);
            for (int j = 0; j < ChessBoard.BOARD_SIZE; j++) {
                var pos = new ChessPosition(row + 1, j + 1);
                var color = (i + j + (boardFlipped ? 1 : 0)) %
                    (squares.length / 2) +
                    (highlightPositions.contains(pos) ? 2 : 0);
                var piece = (board == null ? null : board.getPiece(pos));
                char c = (piece == null) ? ' ' :
                    pieceChars.get(piece.getPieceType());
                var pieceColor = (piece == null) ? "" :
                    piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                    EscapeSequences.SET_TEXT_COLOR_WHITE : 
                    EscapeSequences.SET_TEXT_COLOR_BLACK;
                System.out.print(squares[color] + pieceColor + c);
            }
            System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY + 
                EscapeSequences.SET_TEXT_COLOR_BLACK + rows[row]);
            System.out.println(EscapeSequences.RESET_TEXT_COLOR +
                EscapeSequences.RESET_BG_COLOR);
        }
        System.out.print(EscapeSequences.ERASE_LINE +
            EscapeSequences.SET_BG_COLOR_LIGHT_GREY + 
            EscapeSequences.SET_TEXT_COLOR_BLACK + " abcdefgh ");

        System.out.println(EscapeSequences.RESET_TEXT_COLOR + EscapeSequences.RESET_BG_COLOR);
        printLog();
    }
    public void redraw() {
        redraw(null);
    }

    private Collection<ChessPosition> getHighlightedSquares(ChessPosition pos) {
        var highlightPositions = new HashSet<ChessPosition>();
        if (pos != null && board != null) {
            var piece = board.getPiece(pos);
            if (piece != null) {
                highlightPositions.add(pos);
                var moves = piece.pieceMoves(board, pos);
                for (var move : moves) {
                    highlightPositions.add(move.getEndPosition());
                }
            }
        }
        return highlightPositions;
    }

    private void printLog() {
        int lowerBound = log.size() < ChessBoard.BOARD_SIZE ? 0 :
            log.size() - ChessBoard.BOARD_SIZE;
        for (int i = lowerBound; i < log.size(); i++) {
            System.out.print(EscapeSequences.moveCursorToLocation(12, 2 + i) + 
                log.get(i));
        }
        // reset state for prompts
        System.out.print(EscapeSequences.moveCursorToLocation(0, 12));
    }
}
