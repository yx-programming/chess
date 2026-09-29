package server;

import chess.*;

public class ServerMain {
    public static void main(String[] args) {
        var port = 8080;
        var server = new Server();
        server.run(port);
        System.out.println("240 Chess Server Started on Port " + port);
    }
}
