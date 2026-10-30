package server;

import com.google.gson.*;
import io.javalin.*;
import io.javalin.http.*;

import model.*;

public class Server {

    private final Javalin javalin;
    private Handler handler;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        handler = new Handler();

        // Register your endpoints and exception handlers here.
        javalin.post("/user", handler::newUserEndpoint);
        javalin.post("/session", handler::loginEndpoint);
        javalin.delete("/session", handler::logoutEndpoint);
        javalin.get("/game", handler::listGamesEndpoint);
        javalin.post("/game", handler::createGameEndpoint);
        javalin.put("/game", handler::joinGameEndpoint);
        javalin.delete("/db", handler::clearDBEndpoint);

        javalin.ws("/ws", ws -> {
            ws.onConnect(ctx -> ctx.enableAutomaticPings());
            ws.onMessage(handler::wsMessage);
            ws.onClose(handler::wsClose);
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
