package client;

import jakarta.websocket.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class WebsocketClient extends Endpoint {
    public Session session;

    public WebsocketClient(String host, int port) throws Exception {
        var uri = new URI("ws://" + host + ":" + port + "/ws");
        var container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(this, uri);
        session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                System.out.println("recv");
            }
        });
        send("testing");
    }

    public void send(String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    public void onOpen(Session session, EndpointConfig config) {
    }
}
