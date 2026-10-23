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
        
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + 
            "SHALL WE PLAY A GAME?" + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println("Type help for instructions");
        Scanner scan = new Scanner(System.in);
        ClientState state = new PreLoginState();
        do {
            String command = null;
            String[] commandArgs = new String[]{};
            try {
                System.out.print(state.prompt() + "> ");
                String commandLine = scan.nextLine().toLowerCase();
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
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED +
                    "error: " + EscapeSequences.RESET_TEXT_COLOR +
                    e.getMessage());
            }
        } while (state != null);
    }
}
