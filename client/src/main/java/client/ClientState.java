package client;

import java.util.*;

import chess.*;
import ui.*;

public abstract class ClientState {
    protected interface Command {
        public ClientState apply(String[] args, ServerFacade facade)
            throws Exception;
    }
    
    protected Map<String, Command>commands = Map.of();
    protected UIWrapper ui;
    protected ClientState previous;

    public ClientState action(String commandName, String[] commandArgs,
            ServerFacade facade) throws Exception {
        var command = commands.get(commandName);
        if (command == null) {
            throw new Exception("unrecognized command: " + commandName);
        }
        return command.apply(commandArgs, facade);
    }

    public void log(String message, String color) {
        ui.log(message, color);
    }

    public void clear() {
        ui.clear();
    }

    public void redraw() {
        ui.redraw();
    }

    public void highlight(ChessPosition pos) {
        ui.redraw(pos);
    }
};
