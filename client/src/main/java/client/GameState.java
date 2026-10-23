package client;

import chess.*;
import ui.*;

import java.util.*;

public class GameState extends ClientState {
    private boolean blackPerspective;
    // for the time being use a blank chessboard but we'll plug in the
    // real thing eventually
    private ChessBoard board;
    private String username;
    private String authToken;

    public GameState(boolean blackPerspective, String username, String authToken) {
        this.blackPerspective = blackPerspective;
        board = new ChessBoard();
        board.resetBoard();
        commands = Map.of(
            "help", this::help,
            "quit", this::quit
        );
        this.username = username;
        this.authToken = authToken;
    }

    public ClientState help(String[] args, ServerFacade facade) {
        System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE);
        System.out.println("help - list currently valid commands");
        System.out.println("quit - leave the game");
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
        return this;
    }

    public ClientState quit(String[] args, ServerFacade facade) {
        System.out.println("leaving game");
        return new PostLoginState(username, authToken);
    }

    public String boardString(ChessBoard board) {
        var sb = new StringBuilder();
        String lightColor = EscapeSequences.UNICODE_ESCAPE + "[48;5;136m";
        String darkColor = EscapeSequences.UNICODE_ESCAPE + "[48;5;130m";
        String[] colors = {lightColor, darkColor};
        char[] rows = {' ', '8', '7' ,'6', '5', '4', '3', '2', '1', ' '};
        char[] cols = {' ', 'a', 'b' ,'c', 'd', 'e', 'f', 'g', 'h', ' '};
        Map<ChessPiece.PieceType, Character> repr = Map.of(
            ChessPiece.PieceType.PAWN, 'P',
            ChessPiece.PieceType.KING, 'K',
            ChessPiece.PieceType.QUEEN, 'Q',
            ChessPiece.PieceType.ROOK, 'R',
            ChessPiece.PieceType.BISHOP, 'B',
            ChessPiece.PieceType.KNIGHT, 'N'
        );
        for (int i = 0; i < 10; i++) {
            sb.append(EscapeSequences.ERASE_LINE);
            for (int j = 0; j < 10; j++) {
                var color = (blackPerspective ? (i + j + 1) : (i + j)) %
                    colors.length;
                if (i == 0 || j == 0 || i == 9 || j == 9) {
                    char c;
                    if (blackPerspective) {
                        c = (j == 0 || j == 9) ? rows[9 - i] : cols[9 - j];
                    } else {
                        c = (j == 0 || j == 9) ? rows[i] : cols[j];
                    }
                    sb.append(EscapeSequences.SET_BG_COLOR_LIGHT_GREY + 
                        EscapeSequences.SET_TEXT_COLOR_BLACK + c);
                } else {
                    var pos = new ChessPosition(i, j);
                    var piece = board.getPiece(pos);
                    char c = (piece == null) ? ' ' :
                        repr.get(piece.getPieceType());
                    var pieceColor = (piece == null) ? "" :
                        piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                        EscapeSequences.SET_TEXT_COLOR_WHITE : 
                        EscapeSequences.SET_TEXT_COLOR_BLACK;
                    sb.append(colors[color] + pieceColor + c);
                }
                sb.append(EscapeSequences.RESET_TEXT_COLOR +
                    EscapeSequences.RESET_BG_COLOR);
            }
            sb.append('\n');
        }
        sb.append(EscapeSequences.ERASE_LINE);
        return sb.toString();
    }

    public String prompt() {
        return boardString(board);
    }
}
