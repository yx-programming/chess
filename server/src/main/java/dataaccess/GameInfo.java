package dataaccess;

import chess.*;
import model.*;

public class GameInfo {
    private int gameID;
    private String whiteUsername, blackUsername, gameName;
    private ChessGame game;

    public GameInfo(int id, String whiteUser, String blackUser, String name,
            ChessGame game) {
        gameID = id;
        whiteUsername = whiteUser;
        blackUsername = blackUser;
        gameName = name;
        game = game;
    }

    public int getID() { return gameID; }

    public String getWhiteUser() { return whiteUsername; }

    public void setWhiteUser(String name) { whiteUsername = name; }

    public String getBlackUser() { return blackUsername; }

    public void setBlackUser(String name) { blackUsername = name; }

    public String getName() { return gameName; }

    public ChessGame getGame() { return game; }

}
