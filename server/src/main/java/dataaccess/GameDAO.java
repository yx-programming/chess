package dataaccess;

import java.util.*;

import chess.*;
import model.*;

public interface GameDAO {
    public List<ChessGame> getGames();
    public void newGame();
    public void joinGame();
}
