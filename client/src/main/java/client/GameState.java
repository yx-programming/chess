package client;

import chess.*; import ui.*;
import websocket.commands.*;
import websocket.messages.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import com.google.gson.*;

import jakarta.websocket.*;

public class GameState extends ClientState implements Observer {
    private boolean boardFlipped;
    private String username;
    private String authToken;
    private boolean isPlaying;
    private int gameID;
    private boolean done = false;
    private ChessPosition highlightPos;

    public GameState(boolean boardFlipped, String username, String authToken,
            int gameID, boolean isPlaying, ClientState previous)
            throws Exception {
        this.boardFlipped = boardFlipped;
        this.gameID = gameID;
        this.isPlaying = isPlaying;
        highlightPos = null;
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
        this.previous = previous;
        this.ui = new UIWrapper("[" + username + "] " +
            (isPlaying ? (boardFlipped ? "BLACK" : "WHITE") : ""),
            boardFlipped);
        log("sucessfully joined", UIWrapper.GREEN);
    }

    private interface Message {
        public UserGameCommand call(ServerMessage message) throws Exception;
    }
    private Map<ServerMessage.ServerMessageType, Message> messages = Map.of(
        ServerMessage.ServerMessageType.LOAD_GAME, message -> {
            ui.setBoard(
                ChessGame.fromSerialized(message.getData()).getBoard());
            return null;
        },
        ServerMessage.ServerMessageType.ERROR, message -> {
            var error = message.getData().toLowerCase();
            if (error.contains("error: ")) {
                log(message.getData(), UIWrapper.RED);
            } else {
                log("error: " + message.getData(), UIWrapper.RED);
            }
            redraw();
            return null;
        },
        ServerMessage.ServerMessageType.NOTIFICATION, message -> {
            log(message.getData(), UIWrapper.YELLOW);
            redraw();
            return null;
        },
        ServerMessage.ServerMessageType.DONE,  message -> {
            done = true;
            return null;
        }
    );
    public UserGameCommand message(String message) throws Exception {
        var serverMessage = new Gson().fromJson(message, ServerMessage.class);
        var response = messages.get(serverMessage.getServerMessageType())
            .call(serverMessage);
        redraw();
        System.out.print("> ");
        return response;
    }

    public ClientState help(String[] args, ServerFacade facade) {
        log("help - list currently valid commands", UIWrapper.BLUE);
        log("redraw - redraw the board", UIWrapper.BLUE);
        log("quit - leave the game", UIWrapper.BLUE);
        if (isPlaying) {
            log("move <start> <end> - move your piece (algebraic notation",
                UIWrapper.BLUE);
            log("resign - forfeit the match", UIWrapper.BLUE);
            log("highlight <position> - show moves from <position>",
                UIWrapper.BLUE);
        }
        log(EscapeSequences.RESET_TEXT_COLOR, UIWrapper.BLUE);
        return this;
    }

    public ClientState quit(String[] args, ServerFacade facade)
            throws Exception {
        previous.log("left game", UIWrapper.YELLOW);
        var message = new UserGameCommand(UserGameCommand.CommandType.LEAVE,
            authToken, gameID);
        message.addData(username);
        facade.sendWS(message);
        facade.unsubscribe(this);
        return previous;
    }

    public ClientState redraw(String[] args, ServerFacade facade) {
        highlightPos = null;
        redraw();
        return this;
    }

    @Override
    public void redraw() {
        highlight(highlightPos);
    }

    private Map<Character, ChessPiece.PieceType> pieceMap = Map.of(
        'q', ChessPiece.PieceType.QUEEN,
        'r', ChessPiece.PieceType.ROOK,
        'b', ChessPiece.PieceType.BISHOP,
        'n', ChessPiece.PieceType.KNIGHT
    );
    public ClientState move(String[] args, ServerFacade facade)
            throws Exception {
        if (done) {
            throw new Exception("game is finished");
        }
        if (args.length < 2 || args.length > 3) {
            throw new Exception(
                "expected two board positions (start and end), and an " +
                "optional promotion piece");
        }
        var start = ChessPosition.fromAlgebraic(args[0]);
        var end = ChessPosition.fromAlgebraic(args[1]);
        var piece = ui.getBoard().getPiece(start);
        if (piece == null) {
            throw new Exception("no piece there");
        }
        if (piece.getTeamColor() != (boardFlipped ? ChessGame.TeamColor.BLACK :
                ChessGame.TeamColor.WHITE)) {
            throw new Exception("not your piece");
        }
        ChessPiece.PieceType promotion = null;
        if (args.length == 3) {
            if (piece.getPieceType() != ChessPiece.PieceType.PAWN) {
                throw new Exception("can only promote pawns");
            }
            var promotionChar = args[2].substring(0);
            if (!"qrbn".contains(promotionChar)) {
                throw new Exception("promotion must be a valid piece letter");
            }
            promotion = pieceMap.get(promotionChar);
        }
        var move = new ChessMove(start, end, promotion);
        var json = new Gson().toJson(move);
        var command = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE,
            authToken, gameID);
        command.addData(json);
        facade.sendWS(command);
        return this;
    }

    public ClientState resign(String[] args, ServerFacade facade)
            throws Exception {
        if (done) {
            throw new Exception("game is finished");
        }
        var command = new UserGameCommand(UserGameCommand.CommandType.RESIGN,
            authToken, gameID);
        command.addData(username);
        facade.sendWS(command);
        return this;
    }

    public ClientState highlight(String[] args, ServerFacade facade)
            throws Exception {
        if (args.length != 1 || args[0].length() != 2) {
            throw new Exception("expected one board position [ex: e4]");
        }
        var pos = ChessPosition.fromAlgebraic(args[0]);
        var piece = ui.getBoard().getPiece(pos);
        if (piece == null) {
            throw new Exception("no piece at " + args[0]);
        }
        if ((piece.getTeamColor() == ChessGame.TeamColor.BLACK) !=
                boardFlipped) {
            throw new Exception("that's not your piece");
        }
        highlightPos = pos;
        return this;
    }
}
