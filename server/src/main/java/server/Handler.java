package server;

import java.util.Map;

import com.google.gson.*;
import io.javalin.*;
import io.javalin.http.*;

import chess.*;
import dataaccess.*;
import model.*;
import service.*;

public class Handler {
    private UserService userService;
    private AuthService authService;
    private GameService gameService;
    
    public Handler() {
        userService = new UserService();
        authService = new AuthService();
        gameService = new GameService();
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
            String errString = e.getMessage();
            if (errString.equals("already taken")) {
                context.status(403);
            } else if (errString.equals("bad request")) {
                context.status(400);
            } else {
                context.status(500);
            }
            String jsonNames = new Gson().toJson(Map.of("message",
                "Error: " + e.getMessage()));
            context.result(jsonNames);
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
            String errString = e.getMessage();
            if (errString.equals("unauthorized")) {
                context.status(401);
            } else if (errString.equals("bad request")) {
                context.status(400);
            } else {
                context.status(500);
            }
            String jsonNames = new Gson().toJson(Map.of("message",
                "Error: " + e.getMessage()));
            context.result(jsonNames);
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
            String errString = e.getMessage();
            if (errString.equals("unauthorized")) {
                context.status(401);
            } else if (errString.equals("bad request")) {
                context.status(400);
            } else {
                context.status(500);
            }
            String jsonNames = new Gson().toJson(Map.of("message",
                "Error: " + e.getMessage()));
            context.result(jsonNames);
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
            String errString = e.getMessage();
            if (errString.equals("unauthorized")) {
                context.status(401);
            } else if (errString.equals("bad request")) {
                context.status(400);
            } else {
                context.status(500);
            }
            String jsonNames = new Gson().toJson(Map.of("message",
                "Error: " + e.getMessage()));
            context.result(jsonNames);
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
            String errString = e.getMessage();
            if (errString.equals("unauthorized")) {
                context.status(401);
            } else if (errString.equals("bad request")) {
                context.status(400);
            } else {
                context.status(500);
            }
            String jsonNames = new Gson().toJson(Map.of("message",
                "Error: " + e.getMessage()));
            context.result(jsonNames);
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
            String errString = e.getMessage();
            if (errString.equals("unauthorized")) {
                context.status(401);
            } else if (errString.equals("bad request")) {
                context.status(400);
            } else if (errString.equals("already taken")) {
                context.status(403);
            } else {
                context.status(500);
            }
            String jsonNames = new Gson().toJson(Map.of("message",
                "Error: " + e.getMessage()));
            context.result(jsonNames);
        }
    }

    public void clearDBEndpoint(Context context) {
        context.contentType("application/json");
        userService.clear();
        authService.clear();
        gameService.clear();
        context.status(200);
        context.result("{}");
    }
}
