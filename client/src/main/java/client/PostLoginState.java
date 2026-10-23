package client;

import chess.*;
import ui.*;

import java.util.*;

public class PostLoginState extends ClientState {
    private String username;
    private String authToken;

    private List<ServerFacade.GameInfo> games;
    
    public PostLoginState(String username, String authToken) {
        this.username = username;
        this.authToken = authToken;
        commands = Map.of(
            "help", this::help,
            "logout", this::logout,
            "create", this::createGame,
            "list", this::listGames,
            "join", this::joinGame,
            "watch", this::watchGame
        );
    }

    public String prompt() {
        return "[" + username + "]";
    }

    private ClientState help(String[] args, ServerFacade facade) {
        System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE);
        System.out.println("help - list currently valid commands");
        System.out.println("logout - logout of the current session");
        System.out.println("list - list all games");
        System.out.println("join <ID> [WHITE|BLACK] - join a game");
        System.out.println("watch <ID> - watch a game");
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
        return this;
    }

    private ClientState logout(String[] args, ServerFacade facade)
            throws Exception{
        facade.logout(authToken);
        return new PreLoginState();
    }

    private ClientState createGame(String[] args, ServerFacade facade)
            throws Exception {
        if (args.length != 1) {
            throw new Exception("expected game name");
        }
        var name = args[0];
        facade.createGame(authToken, name);
        return this;
    }

    private ClientState listGames(String[] args, ServerFacade facade)
            throws Exception {
        games = facade.listGames(authToken);
        if (games.size() > 0) {
            System.out.println("GAMES:");
        } else {
            System.out.println("no games are currently live");
        }
        int i = 1;
        for (var game : games) {
            System.out.print("\t" + i + ". " + game.gameName() + " - ");
            if (game.whiteUsername() == null) {
                System.out.print("WHITE: " +
                    EscapeSequences.SET_TEXT_COLOR_GREEN + "Available, ");
            } else if (game.whiteUsername().equals(username)) {
                System.out.print("WHITE: " +
                    EscapeSequences.SET_TEXT_COLOR_YELLOW + "Available (" +
                    game.whiteUsername() + "), ");
            } else {
                System.out.print("WHITE: " +
                    EscapeSequences.SET_TEXT_COLOR_RED + "Unavailable (" +
                    game.whiteUsername() + "), ");
            }
            System.out.print(EscapeSequences.RESET_TEXT_COLOR);

            if (game.blackUsername() == null) {
                System.out.println("BLACK: " +
                    EscapeSequences.SET_TEXT_COLOR_GREEN + "Available");
            } else if (game.blackUsername().equals(username)) {
                System.out.print("BLACK: " +
                    EscapeSequences.SET_TEXT_COLOR_YELLOW + "Available (" +
                    game.blackUsername() + "), ");
            } else {
                System.out.print("BLACK: " +
                    EscapeSequences.SET_TEXT_COLOR_RED + "Unavailable (" +
                    game.blackUsername() + "), ");
            }
            System.out.print(EscapeSequences.RESET_TEXT_COLOR);
            i++;
        }
        return this;
    }

    private ClientState joinGame(String[] args, ServerFacade facade)
            throws Exception {
        if (args.length < 1 || args.length > 2) {
            throw new Exception("expected game id, with optional team color");
        } 
        var id = Integer.parseInt(args[0]) - 1;
        var game = games.get(id);
        if (game == null) {
            throw new Exception("game with id " + id + " doesn't exist");
        }
        ChessGame.TeamColor color = null;
        if (args.length == 2) {
            if (!args[1].equals("white") && !args[1].equals("black")) {
                throw new Exception("team color must be 'white' or 'black'");
            } else {
                color = args[1].equals("white") ? ChessGame.TeamColor.WHITE :
                    ChessGame.TeamColor.BLACK;
            }
        } else {
            if (game.whiteUsername() == null ||
                    game.whiteUsername.equals(username)) {
                color = ChessGame.TeamColor.WHITE;
            } else if (game.whiteUsername() == null ||
                    game.whiteUsername.equals(username)) {
            }
        }
        facade.joinGame(authToken, color, game.gameID());
        return new GameState();
    }

    private ClientState watchGame(String[] args, ServerFacade facade)
            throws Exception {
        return new GameState();
    }
}
