package dataaccess;

import chess.*;
import model.*;

import java.util.*;

public class MemGameDAO implements GameDAO {
    private Map<Integer, GameInfo> games;
    private int currentID;

    public MemGameDAO() throws DataAccessException {
        games = new HashMap<Integer, GameInfo>();
        currentID = 1;
    }

    public List<GameInfo> getGames() throws DataAccessException {
        return new ArrayList<GameInfo>(games.values());
    }

    public int newGame(String name) throws DataAccessException {
        games.put(currentID, new GameInfo(currentID, null, null, name));
        return currentID++;
    }

    public void joinGame(int id, ChessGame.TeamColor color, String username)
            throws DataAccessException {
        GameInfo gameInfo = games.get(id);
        if (gameInfo == null) {
            throw new DataAccessException("game does not exist");
        }
        if (color == ChessGame.TeamColor.WHITE) {
            if (gameInfo.getWhiteUser() != null &&
                    gameInfo.getWhiteUser() != username) {
                throw new DataAccessException("already taken");
            }
            gameInfo.setWhiteUser(username);
        } else {
            if (gameInfo.getBlackUser() != null &&
                    gameInfo.getBlackUser() != username) {
                throw new DataAccessException("already taken");
            }
            gameInfo.setBlackUser(username);
        } 
    }

    public void clear() throws DataAccessException {
        games.clear();
    }
};
