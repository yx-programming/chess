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
        return "";
    }

    public ClientState help(String[] args) {
        return this;
    }

    public ClientState quit(String[] args) {
        return null;
    }

    public ClientState login(String[] args) {
        return this;
    }

    public ClientState register(String[] args) {
        return this;
    }
}
