package websocket.messages;

import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 * <p>
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
    ServerMessageType serverMessageType;

    private String game = null;
    private String message = null;
    private String errorMessage = null;
    
    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION,
    }

    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
    }

    public void addDataGame(String s) {
        if (game == null) {
            game = "";
        }
        game += s;
    }

    public void addDataMessage(String s) {
        if (message == null) {
            message = "";
        }
        message += s;
    }

    public void addDataErrorMessage(String s) {
        if (errorMessage == null) {
            errorMessage = "";
        }
        errorMessage += s;
    }

    public String getData() {
        if (game != null) {
            return game;
        } else if (message != null) {
            return message;
        } else if (errorMessage != null) {
            return errorMessage;
        }
        return "";
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerMessage that)) {
            return false;
        }
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }
}
