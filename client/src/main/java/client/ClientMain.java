package client;

import chess.*;
import ui.*;

import java.util.*;

public class ClientMain {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("args required: host port");
            return;
        }
        var facade = new ServerFacade(args[0], Integer.parseInt(args[1]));
        
        ClientState state = new PreLoginState();
        state.log("SHALL WE PLAY A GAME?", UIWrapper.GREEN);
        state.log("Type help for instructions", UIWrapper.WHITE);
        Scanner scan = new Scanner(System.in);
        do {
            String command = null;
            String[] commandArgs = new String[]{};
            try {
                state.redraw();
                System.out.print("> ");
                String commandLine = scan.nextLine();
                String[] commandWithArgs = commandLine.split(" ");
                if (commandWithArgs.length == 0) {
                    throw new Exception("please input a command");
                }
                command = commandWithArgs[0];
                if (commandWithArgs.length > 1) {
                    commandArgs = Arrays.copyOfRange(commandWithArgs, 1,
                        commandWithArgs.length);
                }

                state = state.action(command, commandArgs, facade);
            } catch (Exception e) {
                state.log("error: " + e.getMessage(), UIWrapper.RED);
            }
        } while (state != null);
    }
}
