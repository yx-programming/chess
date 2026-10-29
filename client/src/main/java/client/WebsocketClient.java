package client;

import jakarta.websocket.*;
import java.io.*;
import java.net.*;
import java.util.*;

import com.google.gson.*;

import websocket.commands.*;

interface Observer {
    public UserGameCommand message(String message) throws Exception;
}

public class WebsocketClient extends Endpoint implements
        MessageHandler.Whole<String> {
    private Session session;
    private Collection<Observer> observers;
        
    public void connect(String host, int port) throws Exception {
        var uri = new URI("ws://" + host + ":" + port + "/ws");
        var container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(this, uri);
        observers = new HashSet();

        session.addMessageHandler(this);
    }

    @Override
    public void onMessage(String message) {
        for (var observer : observers) {
            try {
                var command = observer.message(message);
                if (command != null) {
                    send(command);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
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
