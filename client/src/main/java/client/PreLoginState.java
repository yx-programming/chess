package client;

import ui.*;

import java.util.*;

public class PreLoginState extends ClientState {
    public PreLoginState() {
        commands = Map.of(
            "help", this::help,
            "quit", this::quit,
            "login", this::login,
            "register", this::register
        );
        this.ui = new UIWrapper("[logged out]");
    }

    public ClientState help(String[] args, ServerFacade facade) {
        log("help - list currently valid commands", UIWrapper.BLUE);
        log("quit - exit the application", UIWrapper.BLUE);
        log("login <USERNAME> <PASSWORD> - login to the server",
            UIWrapper.BLUE);
        log("register <username> <password> <email> - register an account",
            UIWrapper.BLUE);
        return this;
    }

    public ClientState quit(String[] args, ServerFacade facade) {
        log("goodbye", UIWrapper.WHITE);
        return null;
    }

    public ClientState login(String[] args, ServerFacade facade)
            throws Exception {
        if (args.length != 2) {
            throw new Exception("expected username and password");
        }
        var username = args[0];
        var password = args[1];
        var authToken = facade.login(username, password);
        return new PostLoginState(username, authToken, this);
    }

    public ClientState register(String[] args, ServerFacade facade)
            throws Exception {
        if (args.length != 3) {
            throw new Exception("expected username, password, and email");
        }
        var username = args[0];
        var password = args[1];
        var email = args[2];
        var authToken = facade.register(username, password, email);
        return new PostLoginState(username, authToken, this);
    }
}
