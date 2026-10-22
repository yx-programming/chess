package ui;

import java.util.*;
import java.util.concurrent.*;

public abstract class UIState {
    private Map<String, Callable<UIState>>commands;
    public abstract void prompt();
    public UIState action(String actionName) throws Exception {
        var option = commands.get(actionName);
        if (option == null) {
            throw new Exception("unrecognized command: " + actionName);
        }
        return option.call();
    }
};
