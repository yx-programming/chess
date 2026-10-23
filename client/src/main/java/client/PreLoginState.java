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
    }

    public String prompt() {
        return "[logged out]";
    }

    public ClientState help(String[] args, ServerFacade facade) {
        return this;       
    }

    public ClientState quit(String[] args, ServerFacade facade) {
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
        return new PostLoginState(authToken);
    }

    public ClientState register(String[] args, ServerFacade facade)
            throws Exception {
        return this;
    }
}
