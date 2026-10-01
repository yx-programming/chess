package server;

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
        } catch (DataAccessException e) {
            
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
