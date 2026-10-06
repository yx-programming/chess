package server;

import java.util.Map;

import com.google.gson.*;
import io.javalin.*;
import io.javalin.http.*;

import dataaccess.*;
import model.*;
import service.*;

public class Handler {
    private UserService userService;
    private AuthService authService;
    
    public Handler() {
        userService = new UserService();
        authService = new AuthService();
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

    }

    public void listGamesEndpoint(Context context) {

    }

    public void createGameEndpoint(Context context) {

    }

    public void joinGameEndpoint(Context context) {

    }

    public void clearDBEndpoint(Context context) {

    }
}
