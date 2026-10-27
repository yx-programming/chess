package client;

import jakarta.websocket.*;
import java.io.*;
import java.net.*;
import java.util.*;

import com.google.gson.*;

import websocket.commands.*;

interface Observer {
    public void message(String message);
}

public class WebsocketClient extends Endpoint {
    private Session session;
    private Collection<Observer> observers;

    public WebsocketClient(String host, int port) throws Exception {
        var uri = new URI("ws://" + host + ":" + port + "/ws");
        var container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(this, uri);
        observers = new HashSet();

        session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                for (var observer : observers) {
                    observer.message(message);
                }
            }
        });
    }

    public void subscribe(Observer observer) {
        observers.add(observer);
    }

    public void unsubscribe(Observer observer) {
        observers.remove(observer);
    }

    public void send(UserGameCommand command) throws IOException {
        String message = new Gson().toJson(command);
        session.getBasicRemote().sendText(message);
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {

    }
}
