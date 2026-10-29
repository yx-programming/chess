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
    private ChessPosition highlightPos;

    public GameState(boolean boardFlipped, String username,
            String authToken, int gameID, boolean isPlaying) throws Exception {
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
        this.ui = new UIWrapper("[" + username + "] " +
            (boardFlipped ? "BLACK" : "WHITE"), boardFlipped);
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

    public ClientState quit(String[] args, ServerFacade facade) {
        log("leaving game", UIWrapper.BLUE);
        return new PostLoginState(username, authToken);
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

    public ClientState move(String[] args, ServerFacade facade)
            throws Exception {
        if (args.length != 2) {
            throw new Exception("expected two board positions (start and end)");
        }
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
