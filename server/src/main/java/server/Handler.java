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
    
    public Handler() {
        userService = new UserService();
    }

    public void newUserEndpoint(Context context) {
        UserData body = new Gson().fromJson(context.body(), UserData.class);
        try {
            userService.newUser(body);
            context.status(200);
        } catch (DataAccessException e) {
            String errString = e.getMessage();
            if (errString.equals("already taken")) {
                context.status(403);
            } else if (errString.equals("bad request")) {
                context.status(400);
            } else {
                context.status(500);
            }
            context.contentType("application/json");
            String jsonNames = new Gson().toJson(Map.of("message",
                "Error: " + e.getMessage()));
            context.result(jsonNames);
        }
    }

    public void loginEndpoint(Context context) {

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
