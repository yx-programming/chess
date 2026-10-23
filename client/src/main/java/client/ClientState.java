package client;

import java.util.*;

public abstract class ClientState {
    protected interface Command {
        public ClientState apply(String[] args, ServerFacade facade)
            throws Exception;
    }
    
    protected Map<String, Command>commands = Map.of();
    public abstract String prompt();
    public ClientState action(String commandName, String[] commandArgs,
            ServerFacade facade) throws Exception {
        var command = commands.get(commandName);
        if (command == null) {
            throw new Exception("unrecognized command: " + commandName);
        }
        return command.apply(commandArgs, facade);
    }
};
