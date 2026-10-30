package server;

import java.util.Map;

import com.google.gson.*;
import io.javalin.*;
import io.javalin.http.*;
import io.javalin.websocket.*;

import chess.*;
import dataaccess.*;
import model.*;
import service.*;
import websocket.commands.*;
import websocket.messages.*;

import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

public class Handler {
    private UserService userService;
    private AuthService authService;
    private GameService gameService;

    private Map<WsContext, Integer> contextToGameID = new HashMap<>();

    public Handler() {
        try {
            userService = new UserService();
            authService = new AuthService();
            gameService = new GameService();
        } catch (DataAccessException dae) {
            System.out.println(dae.getMessage());
            System.exit(1);
        }
    }

    private static final Map<String, Integer> ERROR_CODES =
        Map.of("bad request", 400, "already taken", 403, "unauthorized", 401);

    private void handleErrors(Context context, Exception e) {
        Integer code = ERROR_CODES.get(e.getMessage());
        if (code == null) {
            context.status(500);
        } else {
            context.status(code);
        }
        String jsonError = new Gson().toJson(Map.of("message",
            "Error: " + e.getMessage()));
        context.result(jsonError);
    }

    public void newUserEndpoint(Context context) {
        context.contentType("application/json");
        UserData body = new Gson().fromJson(context.body(), UserData.class);
        try {
            if (body.username() == null || body.password() == null) {
                throw new Exception("bad request");
            }
            userService.newUser(body);
            context.status(200);
            String jsonResult = new Gson().toJson(
                authService.newAuth(body.username()));
            context.result(jsonResult);
        } catch (Exception e) {
            handleErrors(context, e);
        }
    }

    public void loginEndpoint(Context context) {
        context.contentType("application/json");
        UserData body = new Gson().fromJson(context.body(), UserData.class);
        try {
            if (body.username() == null || body.password() == null) {
                throw new Exception("bad request");
            }
            userService.validateUser(body);
            context.status(200);
            String jsonResult = new Gson().toJson(
                authService.newAuth(body.username()));
            context.result(jsonResult);
        } catch (Exception e) {
            handleErrors(context, e);
        }
    }

    public void logoutEndpoint(Context context) {
        context.contentType("application/json");
        String authToken = context.header("authorization");
        try {
            var auth = authService.getAuthFromToken(authToken);
            authService.removeAuth(auth);
            context.status(200);
            context.result("{}");
        } catch (Exception e) {
            handleErrors(context, e);
        }
    }

    public void listGamesEndpoint(Context context) {
        context.contentType("application/json");
        String authToken = context.header("authorization");
        try {
            authService.getAuthFromToken(authToken);
            var games = gameService.getGames();
            context.status(200);
            context.result("{ \"games\": " + new Gson().toJson(games) + " }");
        } catch (Exception e) {
            handleErrors(context, e);
        }
    }

    private record CreateGameRequest(String gameName) {}

    public void createGameEndpoint(Context context) {
        context.contentType("application/json");
        String authToken = context.header("authorization");
        try {
            authService.getAuthFromToken(authToken);
            CreateGameRequest game = new Gson().fromJson(context.body(),
                CreateGameRequest.class);
            if (game.gameName() == null) {
                throw new Exception("bad request");
            }
            int id = gameService.newGame(game.gameName());
            context.status(200);
            context.result("{ \"gameID\": " + id + " }");
        } catch (Exception e) {
            handleErrors(context, e);
        }
    }

    private record JoinGameRequest(String playerColor, int gameID) {}

    public void joinGameEndpoint(Context context) {
        context.contentType("application/json");
        String authToken = context.header("authorization");
        try {
            var auth = authService.getAuthFromToken(authToken);
            JoinGameRequest game = new Gson().fromJson(context.body(),
                JoinGameRequest.class);
            if (game == null || (!"WHITE".equals(game.playerColor()) &&
                    !"BLACK".equals(game.playerColor())) ||
                    game.gameID() <= 0) {
                throw new Exception("bad request");
            }
            gameService.joinGame(game.gameID,
                game.playerColor().equals("WHITE") ? ChessGame.TeamColor.WHITE :
                ChessGame.TeamColor.BLACK, auth.username());
            context.status(200);
            context.result("{}");
        } catch (Exception e) {
            handleErrors(context, e);
        }
    }

    public void clearDBEndpoint(Context context) {
        context.contentType("application/json");
        try {
            userService.clear();
            authService.clear();
            gameService.clear();
            context.status(200);
            context.result("{}");
        } catch (Exception e) {
            handleErrors(context, e);
        }
    }

    public void wsClose(WsContext context) {
        contextToGameID.remove(context);
    }

    private interface Command {
        public void call(WsMessageContext context, UserGameCommand command)
            throws ClosedChannelException, Exception;
    }
    private void connect(WsMessageContext context, UserGameCommand command)
            throws ClosedChannelException, Exception {
        AuthData auth;
        try {
            auth = authService.getAuthFromToken(command.getAuthToken());
        } catch (Exception e) {
            var message = new ServerMessage(
                ServerMessage.ServerMessageType.ERROR);
            message.addDataErrorMessage("unauthorized");
            send(message, context);
            return;
        }
        var name = auth.username();
        var gameInfo = gameService.getGame(command.getGameID());
        var game = gameInfo.getGame();
        var message = new ServerMessage(
            ServerMessage.ServerMessageType.LOAD_GAME);
        message.addDataGame(game.serialize());
        send(message, context);
        message = new ServerMessage(
            ServerMessage.ServerMessageType.NOTIFICATION);
        message.addDataMessage(name + " has joined");
        if (name.equals(gameInfo.getWhiteUser())) {
            message.addDataMessage(" as white");
        } else if (name.equals(gameInfo.getBlackUser())) {
            message.addDataMessage(" as black");
        }
        sendToAllInGame(message, gameInfo.getID());
        contextToGameID.put(context, gameInfo.getID());
    }

    private void unauthorized(WsContext context)
            throws ClosedChannelException, Exception {
        var message = new ServerMessage(
            ServerMessage.ServerMessageType.ERROR);
        message.addDataErrorMessage("unauthorized");
        send(message, context);
    }

    private void makeMove(WsMessageContext context, UserGameCommand command)
            throws ClosedChannelException, Exception {
        AuthData auth;
        try {
            auth = authService.getAuthFromToken(command.getAuthToken());
        } catch (Exception e) {
            unauthorized(context);
            return;
        }
        var gameInfo = gameService.getGame(command.getGameID());
        var game = gameInfo.getGame();
        var name = auth.username();
        if (!(name.equals(gameInfo.getWhiteUser()) &&
                game.getTeamTurn() == ChessGame.TeamColor.WHITE) &&
                !(name.equals(gameInfo.getBlackUser()) &&
                game.getTeamTurn() == ChessGame.TeamColor.BLACK)) {
            unauthorized(context);
            return;
        }
        if (game.isDone()) {
            var message = new ServerMessage(
                ServerMessage.ServerMessageType.ERROR);
            message.addDataErrorMessage("the game has ended");
            send(message, context);
            return;
        }
        var move = command.getMove();
        try {
            var color = game.getBoard().getPiece(move.getStartPosition())
                .getTeamColor();
            game.makeMove(move);
            gameInfo.setGame(game);
            var otherColor = color == ChessGame.TeamColor.WHITE ?
                ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE ;
            var otherName = otherColor == ChessGame.TeamColor.WHITE ?
                gameInfo.getWhiteUser() : gameInfo.getBlackUser();
            var message = new ServerMessage(
                ServerMessage.ServerMessageType.NOTIFICATION);
            message.addDataMessage(name + " made a move (" + move + ")");
            sendToAllInGameExcept(message, gameInfo.getID(), context);
            if (game.isInCheckmate(otherColor)) {
                message = new ServerMessage(
                    ServerMessage.ServerMessageType.NOTIFICATION);
                message.addDataMessage(otherName + " is in checkmate, " + name +
                    " wins!");
                sendToAllInGame(message, gameInfo.getID());
            } else if (game.isInCheck(otherColor)) {
                message = new ServerMessage(
                    ServerMessage.ServerMessageType.NOTIFICATION);
                message.addDataMessage(otherName + " is in check");
                sendToAllInGame(message, gameInfo.getID());
            }
                        
            gameService.updateGame(gameInfo);
            message = new ServerMessage(
                ServerMessage.ServerMessageType.LOAD_GAME);
            message.addDataGame(game.serialize());
            sendToAllInGame(message, gameInfo.getID());
        } catch (ClosedChannelException e) {
            
        } catch (Exception e) {
            var message = new ServerMessage(
                ServerMessage.ServerMessageType.ERROR);
            message.addDataErrorMessage(e.getMessage());
            send(message, context);
        }
    }

    private void leave(WsMessageContext context, UserGameCommand command)
            throws ClosedChannelException, Exception {
        AuthData auth;
        try {
            auth = authService.getAuthFromToken(command.getAuthToken());
        } catch (Exception e) {
            var message = new ServerMessage(
                ServerMessage.ServerMessageType.ERROR);
            message.addDataErrorMessage("unauthorized");
            send(message, context);
            return;
        }
        var username = auth.username();
        var message = new ServerMessage(
            ServerMessage.ServerMessageType.NOTIFICATION);
        contextToGameID.remove(context);
        context.closeSession();
        message.addDataMessage(username + " left");
        sendToAllInGame(message, command.getGameID());

        var game = gameService.getGame(command.getGameID());
        if (username.equals(game.getWhiteUser())) {
            game.setWhiteUser(null);
        } else if (username.equals(game.getBlackUser())) {
            game.setBlackUser(null);
        }
        gameService.updateGame(game);
    }

    private void resign(WsMessageContext context, UserGameCommand command)
            throws ClosedChannelException, Exception {
        AuthData auth;
        try {
            auth = authService.getAuthFromToken(command.getAuthToken());
        } catch (Exception e) {
            var message = new ServerMessage(
                ServerMessage.ServerMessageType.ERROR);
            message.addDataErrorMessage("unauthorized");
            send(message, context);
            return;
        }
        var game = gameService.getGame(command.getGameID());
        var name = auth.username();
        if (!name.equals(game.getWhiteUser()) &&
                !name.equals(game.getBlackUser())) {
            unauthorized(context);
            return;
        }
        if (game.getGame().isDone()) {
            var message = new ServerMessage(
                ServerMessage.ServerMessageType.ERROR);
            message.addDataErrorMessage("game is already over");
            send(message, context);
            return;
        }
        var otherName = game.getWhiteUser().equals(name) ? game.getBlackUser() :
            game.getWhiteUser();
        var message = new ServerMessage(
            ServerMessage.ServerMessageType.NOTIFICATION);
        message.addDataMessage(name + " resigned, " + otherName +
            " wins!");
        sendToAllInGame(message, command.getGameID());
        game.getGame().finish();
        gameService.updateGame(game);
    }
    
    private Map<UserGameCommand.CommandType, Command> commands = Map.of(
        UserGameCommand.CommandType.CONNECT, this::connect,
        UserGameCommand.CommandType.MAKE_MOVE, this::makeMove,
        UserGameCommand.CommandType.LEAVE, this::leave,
        UserGameCommand.CommandType.RESIGN, this::resign
    );
    public void wsMessage(WsMessageContext context) throws Exception {
        try {
            var command = new Gson().fromJson(context.message(),
                UserGameCommand.class);
            commands.get(command.getCommandType()).call(context, command);
        } catch (ClosedChannelException e) {
            System.out.println("context was dropped, closing " + e.getMessage());
            contextToGameID.remove(context);
            return;
        } catch (Exception e) {
            var message = new ServerMessage(
                ServerMessage.ServerMessageType.ERROR);
            message.addDataErrorMessage(e.toString());
            send(message, context);
        }
    }

    private void sendToAllInGame(ServerMessage message, int id)
            throws ClosedChannelException, Exception {
        for (var otherContext : contextToGameID.keySet()) {
            if (contextToGameID.get(otherContext) == id) {
                send(message, otherContext);
            }
        }
    }

    private void sendToAllInGameExcept(ServerMessage message, int id,
            WsContext exclude) throws ClosedChannelException, Exception {
        for (var otherContext : contextToGameID.keySet()) {
            if (contextToGameID.get(otherContext) == id &&
                    !otherContext.equals(exclude)) {
                send(message, otherContext);
            }
        }
    }

    private void send(ServerMessage message, WsContext context)
            throws ClosedChannelException, Exception {
        var messageString = new Gson().toJson(message);
        context.send(messageString);
        }
}
