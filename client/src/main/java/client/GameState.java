package client;

import chess.*;
import ui.*;
import websocket.commands.*;

import java.io.*;
import java.net.*;
import java.util.*;

import com.google.gson.*;

import jakarta.websocket.*;

public class GameState extends ClientState implements Observer {
    private boolean blackPerspective;
    // for the time being use a blank chessboard but we'll plug in the
    // real thing eventually
    private ChessBoard board;
    private String username;
    private String authToken;
    private boolean isPlaying;
    private int gameID;
    private Collection<ChessPosition> highlightPositions;
    private WebsocketClient ws;

    public GameState(boolean blackPerspective, String username,
            String authToken, int gameID, boolean isPlaying) throws Exception {
        this.blackPerspective = blackPerspective;
        this.gameID = gameID;
        this.isPlaying = isPlaying;
        board = new ChessBoard();
        board.resetBoard();
        if (isPlaying) {
            commands = Map.of(
                "help", this::help,
                "redraw", this::redraw,
                "quit", this::quit,
                "move", this::move,
                "resign", this::resign,
                "highlight", this::highlight
            );
        } else {
            commands = Map.of(
                "help", this::help,
                "redraw", this::redraw,
                "quit", this::quit
            );
        }
        this.username = username;
        this.authToken = authToken;
        highlightPositions = new HashSet();
        // TODO: thread host/port values into constructor
        ws = new WebsocketClient("localhost", 8080);
        ws.subscribe(this);
        var connect = new UserGameCommand(UserGameCommand.CommandType.CONNECT,
            authToken, gameID);
        ws.send(connect);
    }

    public void message(String message) {
        System.out.println(message);
    }

    public ClientState help(String[] args, ServerFacade facade) {
        System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE);
        System.out.println("help - list currently valid commands");
        System.out.println("redraw - redraw the board");
        System.out.println("quit - leave the game");
        if (isPlaying) {
            System.out.println("move <move> - move your piece (algebraic notation");
            System.out.println("resign - forfeit the match");
            System.out.println("highlight <position> - show valid moves for the piece at <position>");
        }
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
        return this;
    }

    public ClientState quit(String[] args, ServerFacade facade) {
        System.out.println("leaving game");
        return new PostLoginState(username, authToken);
    }

    public ClientState redraw(String[] args, ServerFacade facade) {
        System.out.println(boardString(this.board));
        return this;
    }

    public ClientState move(String[] args, ServerFacade facade)
            throws Exception {
        return this;
    }

    public ClientState resign(String[] args, ServerFacade facade)
            throws Exception {
        return this;
    }

    public ClientState highlight(String[] args, ServerFacade facade)
            throws Exception {
        if (args.length != 1 || args[0].length() != 2) {
            throw new Exception("expected one board position [ex: e4]");
        }
        int col = args[0].charAt(0) - 'a' + 1;
        if (col < 1 || col > 8) {
            throw new Exception("file must be a-h");
        }
        int row = args[0].charAt(1) - '0';
        if (row < 1 || row > 8) {
            throw new Exception("rank must be 1-8");
        }
        var pos = new ChessPosition(row, col);
        var piece = board.getPiece(new ChessPosition(row, col));
        if (piece == null) {
            throw new Exception("no piece at " + args[0]);
        }
        if ((piece.getTeamColor() == ChessGame.TeamColor.BLACK) !=
                blackPerspective) {
            throw new Exception("that's not your piece");
        }
        var moves = piece.pieceMoves(board, pos);
        highlightPositions.add(pos);
        for (var move : moves) {
            highlightPositions.add(move.getEndPosition());
        }
        System.out.println(boardString(board));
        highlightPositions.clear();
        return this;
    }

    private String boardString(ChessBoard board) {
        var sb = new StringBuilder();
        String lightColor = EscapeSequences.UNICODE_ESCAPE + "[48;5;136m";
        String darkColor = EscapeSequences.UNICODE_ESCAPE + "[48;5;130m";
        String hiLightColor = EscapeSequences.UNICODE_ESCAPE + "[48;5;99m";
        String hiDarkColor = EscapeSequences.UNICODE_ESCAPE + "[48;5;98m";
        String[] colors = {lightColor, darkColor, hiLightColor, hiDarkColor};
        char[] rows = {'1', '2', '3', '4', '5', '6', '7', '8' };
        Map<ChessPiece.PieceType, Character> repr = Map.of(
            ChessPiece.PieceType.PAWN, 'P',
            ChessPiece.PieceType.KING, 'K',
            ChessPiece.PieceType.QUEEN, 'Q',
            ChessPiece.PieceType.ROOK, 'R',
            ChessPiece.PieceType.BISHOP, 'B',
            ChessPiece.PieceType.KNIGHT, 'N'
        );
        sb.append(EscapeSequences.ERASE_LINE +
            EscapeSequences.SET_BG_COLOR_LIGHT_GREY + 
            EscapeSequences.SET_TEXT_COLOR_BLACK + " abcdefgh \n");
        sb.append(EscapeSequences.RESET_TEXT_COLOR +
            EscapeSequences.RESET_BG_COLOR);
        for (int i = 0; i < 8; i++) {
            sb.append(EscapeSequences.ERASE_LINE);
            int row = blackPerspective ? i : (7 - i);
            sb.append(EscapeSequences.ERASE_LINE +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + 
                EscapeSequences.SET_TEXT_COLOR_BLACK + rows[row]);
            sb.append(EscapeSequences.RESET_TEXT_COLOR +
                EscapeSequences.RESET_BG_COLOR);
            for (int j = 0; j < 8; j++) {
                var color = (blackPerspective ? (i + j + 1) : (i + j)) %
                    (colors.length / 2);
                var pos = new ChessPosition(row + 1, j + 1);
                if (highlightPositions.contains(pos)) {
                    color += 2;
                }
                var piece = board.getPiece(pos);
                char c = (piece == null) ? ' ' :
                    repr.get(piece.getPieceType());
                var pieceColor = (piece == null) ? "" :
                    piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                    EscapeSequences.SET_TEXT_COLOR_WHITE : 
                    EscapeSequences.SET_TEXT_COLOR_BLACK;
                sb.append(colors[color] + pieceColor + c);
            }
            sb.append(EscapeSequences.SET_BG_COLOR_LIGHT_GREY + 
                EscapeSequences.SET_TEXT_COLOR_BLACK + rows[row]);
            sb.append(EscapeSequences.RESET_TEXT_COLOR +
                EscapeSequences.RESET_BG_COLOR);
            sb.append('\n');
        }
        sb.append(EscapeSequences.ERASE_LINE +
            EscapeSequences.SET_BG_COLOR_LIGHT_GREY + 
            EscapeSequences.SET_TEXT_COLOR_BLACK + " abcdefgh ");
        sb.append(EscapeSequences.RESET_TEXT_COLOR +
            EscapeSequences.RESET_BG_COLOR);
        return sb.toString();
    }

    public String prompt() {
        return "[" + username + "]";
    }
}
