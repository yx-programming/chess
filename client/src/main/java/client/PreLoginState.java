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
        System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE);
        System.out.println("help - list currently valid commands");
        System.out.println("quit - exit the application");
        System.out.println("login <USERNAME> <PASSWORD> - login to the server");
        System.out.print("register <USERNAME> <PASSWORD> <EMAIL> - ");
        System.out.println("register a new user on the server");
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
        return this;
    }

    public ClientState quit(String[] args, ServerFacade facade) {
        System.out.println("goodbye");
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
        return new PostLoginState(username, authToken);
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
        return new PostLoginState(username, authToken);
    }
}
