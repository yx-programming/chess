package dataaccess;

import chess.*;
import model.*;

import java.util.*;

public class MemGameDAO implements GameDAO {
    private Map<Integer, GameInfo> games;
    private int currentID;

    public MemGameDAO() {
        games = new HashMap<Integer, GameInfo>();
        currentID = 0;
    }

    public List<GameInfo> getGames() {
        return new ArrayList<GameInfo>(games.values());
    }

    public int newGame(String name) {
        games.put(currentID, new GameInfo(currentID, null, null, name));
        return currentID++;
    }

    public void joinGame(int id) {

    }

    public void clear() {
        games.clear();
    }
};
