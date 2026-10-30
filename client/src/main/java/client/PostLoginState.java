package client;

import chess.*;
import ui.*;
import websocket.commands.*;

import java.util.*;

public class PostLoginState extends ClientState {
    private String username;
    private String authToken;

    private List<ServerFacade.GameInfo> games;
    
    public PostLoginState(String username, String authToken,
            ClientState previous) {
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
        this.ui = new UIWrapper("[" + username + "]");
        this.previous = previous;
        log("you are now logged in", UIWrapper.GREEN);
    }

    private ClientState help(String[] args, ServerFacade facade) {
        log("help - list currently valid commands", UIWrapper.BLUE);
        log("logout - logout of the current session", UIWrapper.BLUE);
        log("list - list all games", UIWrapper.BLUE);
        log("join <ID> [WHITE|BLACK] - join a game", UIWrapper.BLUE);
        log("watch <ID> - watch a game", UIWrapper.BLUE);
        return this;
    }

    private ClientState logout(String[] args, ServerFacade facade)
            throws Exception{
        facade.logout(authToken);
        previous.log("logged out", UIWrapper.YELLOW);
        return previous;
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
            log("GAMES:", UIWrapper.WHITE);
        } else {
            log("no games are currently live", UIWrapper.WHITE);
        }
        int i = 1;
        for (var game : games) {
            String gameInfo = " " + i + ". " + game.gameName() + " - WHITE: ";
            if (game.whiteUsername() == null) {
                gameInfo += EscapeSequences.SET_TEXT_COLOR_GREEN +
                    "Available, ";
            } else if (game.whiteUsername().equals(username)) {
                gameInfo += EscapeSequences.SET_TEXT_COLOR_YELLOW +
                    "Available (" + game.whiteUsername() + "), ";
            } else {
                gameInfo += EscapeSequences.SET_TEXT_COLOR_RED +
                    "Unavailable (" + game.whiteUsername() + "), ";
            }
            gameInfo += EscapeSequences.RESET_TEXT_COLOR + "BLACK: ";
            if (game.blackUsername() == null) {
                gameInfo += EscapeSequences.SET_TEXT_COLOR_GREEN +
                    "Available";
            } else if (game.blackUsername().equals(username)) {
                gameInfo += EscapeSequences.SET_TEXT_COLOR_YELLOW +
                    "Available (" + game.blackUsername() + ")";
            } else {
                gameInfo += EscapeSequences.SET_TEXT_COLOR_RED +
                    "Unavailable (" + game.blackUsername() + ")";
            }
            log(gameInfo, UIWrapper.WHITE);
            i++;
        }
        return this;
    }

    private int validateGameID(String listId) throws Exception {
        if (games == null) {
            throw new Exception("no games have been listed to select from");
        }
        int id;
        try {
            id = Integer.parseInt(listId) - 1;
        } catch (Exception e) {
            throw new Exception(
                "game id must be a number (look at the game list)");
        }
        if (id < 0 || id > games.size() - 1) {
            throw new Exception("id must be in the game list");
        }
        return id;
    }

    private ClientState joinGame(String[] args, ServerFacade facade)
            throws Exception {
        if (args.length < 1 || args.length > 2) {
            throw new Exception("expected game id, with optional team color");
        } 
        int id = validateGameID(args[0]);
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
                    game.whiteUsername().equals(username)) {
                color = ChessGame.TeamColor.WHITE;
            } else if (game.blackUsername() == null ||
                    game.blackUsername().equals(username)) {
                color = ChessGame.TeamColor.BLACK;
            }
        }
        facade.joinGame(authToken, color, game.gameID());
        return wsConnect(facade, true, color, game);
    }

    private ClientState watchGame(String[] args, ServerFacade facade)
            throws Exception {
        if (args.length == 0) {
            throw new Exception("expected a game ID (check the game list)");
        }
        int gameID = validateGameID(args[0]);
        var game = games.get(gameID);
        if (game == null) {
            throw new Exception("game not listed");
        }
        return wsConnect(facade, false, null, game);
    }
    private ClientState wsConnect(ServerFacade facade, boolean playing,
            ChessGame.TeamColor color, ServerFacade.GameInfo game)
            throws Exception {
        facade.connectWS();
        var state = new GameState(color == ChessGame.TeamColor.BLACK, username,
            authToken, game.gameID(), playing, this);
        var command = new UserGameCommand(UserGameCommand.CommandType.CONNECT,
            authToken, game.gameID());
        facade.sendWS(command);
        facade.registerObserver(state);
        return state;
    }
}
