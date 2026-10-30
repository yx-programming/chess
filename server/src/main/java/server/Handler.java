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

import java.util.*;
import java.util.concurrent.*;

public class Handler {
    private UserService userService;
    private AuthService authService;
    private GameService gameService;

    private Map<String, WsMessageContext> authToContext = new HashMap<>();
    private boolean done = false;

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

    private interface Command {
        public void call(WsMessageContext context, UserGameCommand command)
            throws Exception;
    }
    private void connect(WsMessageContext context, UserGameCommand command)
            throws Exception {
        authService.getAuthFromToken(command.getAuthToken());
        var gameInfo = gameService.getGame(command.getGameID());
        var game = gameInfo.getGame();
        var name = command.getData();
        authToContext.put(command.getAuthToken(), context);
        var message = new ServerMessage(
            ServerMessage.ServerMessageType.LOAD_GAME);
        message.addData(game.serialize());
        send(message, context);
        message = new ServerMessage(
            ServerMessage.ServerMessageType.NOTIFICATION);
        message.addData(name + " has joined");
        if (name.equals(gameInfo.getWhiteUser())) {
            message.addData(" as white");
        } else if (name.equals(gameInfo.getBlackUser())) {
            message.addData(" as black");
        }
        sendToAll(message);
    }

    private void makeMove(WsMessageContext context, UserGameCommand command)
            throws Exception {
        authService.getAuthFromToken(command.getAuthToken());
        if (done) {
            var message = new ServerMessage(
                ServerMessage.ServerMessageType.ERROR);
            message.addData("the game has ended");
            send(message, context);
            return;
        }
        var gameInfo = gameService.getGame(command.getGameID());
        var game = gameInfo.getGame();
        ChessMove move = new Gson().fromJson(command.getData(),
            ChessMove.class);
        try {
            var color = game.getBoard().getPiece(move.getStartPosition())
                .getTeamColor();
            game.makeMove(move);
            gameInfo.setGame(game);
            var name = color == ChessGame.TeamColor.WHITE ?
                gameInfo.getWhiteUser() : gameInfo.getBlackUser();
            var otherColor = color == ChessGame.TeamColor.WHITE ?
                ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE ;
            var otherName = otherColor == ChessGame.TeamColor.WHITE ?
                gameInfo.getWhiteUser() : gameInfo.getBlackUser();
            var message = new ServerMessage(
                ServerMessage.ServerMessageType.NOTIFICATION);
            message.addData(name + " made a move (" + move + ")");
            sendToAll(message);
            if (game.isInCheckmate(otherColor)) {
                message = new ServerMessage(
                    ServerMessage.ServerMessageType.NOTIFICATION);
                message.addData(otherName + " is in checkmate, " + name +
                    " wins!");
                sendToAll(message);
                done = true;
            } else if (game.isInCheck(otherColor)) {
                message = new ServerMessage(
                    ServerMessage.ServerMessageType.NOTIFICATION);
                message.addData(otherName + " is in check");
                sendToAll(message);
            }
            
            if (done) {
                message = new ServerMessage(
                    ServerMessage.ServerMessageType.DONE);
                sendToAll(message);
                gameService.deleteGame(gameInfo);
            } else {
                gameService.updateGame(gameInfo);
            }
            message = new ServerMessage(
                ServerMessage.ServerMessageType.LOAD_GAME);
            message.addData(game.serialize());
            sendToAll(message);
        } catch (Exception e) {
            var message = new ServerMessage(
                ServerMessage.ServerMessageType.ERROR);
            message.addData(e.getMessage());
            send(message, context);
        }
    }

    private void leave(WsMessageContext context, UserGameCommand command)
            throws Exception {
        authService.getAuthFromToken(command.getAuthToken());
        var message = new ServerMessage(
            ServerMessage.ServerMessageType.NOTIFICATION);
        var username = command.getData();
        authToContext.remove(command.getAuthToken());
        context.closeSession();
        message.addData(username + " left");
        sendToAll(message);
    }

    private void resign(WsMessageContext context, UserGameCommand command)
            throws Exception {
        authService.getAuthFromToken(command.getAuthToken());
        var name = command.getData();
        var game = gameService.getGame(command.getGameID());
        var otherName = game.getWhiteUser().equals(name) ? game.getBlackUser() :
            game.getWhiteUser();
        var message = new ServerMessage(
            ServerMessage.ServerMessageType.NOTIFICATION);
        message.addData(name + " resigned, " + otherName +
            " wins!");
        sendToAll(message);
        gameService.deleteGame(game);
        message = new ServerMessage(
            ServerMessage.ServerMessageType.DONE);
        sendToAll(message);
    }
    
    private Map<UserGameCommand.CommandType, Command> commands = Map.of(
        UserGameCommand.CommandType.CONNECT, this::connect,
        UserGameCommand.CommandType.MAKE_MOVE, this::makeMove,
        UserGameCommand.CommandType.LEAVE, this::leave,
        UserGameCommand.CommandType.RESIGN, this::resign
    );
    public void wsMessage(WsMessageContext context) throws Exception {
        var command = new Gson().fromJson(context.message(),
            UserGameCommand.class);
        commands.get(command.getCommandType()).call(context, command);
    }

    private void sendToAll(ServerMessage message) {
        for (var context : authToContext.values()) {
            send(message, context);
        }
    }

    private void send(ServerMessage message, WsMessageContext context) {
        var messageString = new Gson().toJson(message);
        context.send(messageString);
    }
}
