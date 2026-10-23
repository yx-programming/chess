package client;

import chess.*;

import java.net.*;
import java.io.*;
import java.util.*;
import com.google.gson.*;

public class ServerFacade {
    private record ClientError(String message) {}
    private String baseUrl;
    private int port;

    public ServerFacade(String url, int port) {
        baseUrl = url;
        this.port = port;
    }

    public String register(String username, String password, String email)
            throws Exception {
        return extractAuthToken(httpRequest("user", "POST",
            "{\"username\":\"" + username + "\",\"password\":\"" + password +
            "\",\"email\":\"" + email + "\"}", null));
    }

    public String login(String username, String password) throws Exception {
        return extractAuthToken(httpRequest("session", "POST",
            "{\"username\":\"" + username + "\",\"password\":\"" + password +
            "\"}", null));
    }

    public void logout(String authToken) throws Exception {
        httpRequest("session", "DELETE", null, authToken);
    }

    private record GameId(int gameID) {}
    public int createGame(String authToken, String gameName) throws Exception {
        String result = httpRequest("game", "POST", 
            "{\"gameName\":\"" + gameName + "\"}", authToken);
        return new Gson().fromJson(result, GameId.class).gameID(); 
    }

    public record GameInfo(int gameID, String whiteUsername,
        String blackUsername, String gameName) {}
    private record Games(List<GameInfo> games) {}
    public List<GameInfo> listGames(String authToken) throws Exception {
        String result = httpRequest("game", "GET", null, authToken);
        return new Gson().fromJson(result, Games.class).games();
    }

    public void joinGame(String authToken, ChessGame.TeamColor color, int id)
            throws Exception {
        var colorStr = (color == ChessGame.TeamColor.WHITE) ? "WHITE" :
            "BLACK";
        httpRequest("game", "PUT",
            "{\"playerColor\":\"" + colorStr + "\",\"gameID\":\"" + id + "\"}",
                authToken);
    }

    private record AuthResult(String username, String authToken) {}
    private String extractAuthToken(String json) {
        return new Gson().fromJson(json, AuthResult.class).authToken();
    }

    protected String httpRequest(String endpoint, String method, String body,
            String authHeader) throws Exception {
        var url = new URL("http://" + baseUrl + ":" + port + "/" + endpoint);
        var conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod(method);
        if (authHeader != null) {
            conn.setRequestProperty("authorization", authHeader);
        }
        if (body != null) {
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setUseCaches(false);
            conn.setDoOutput(true);

            try (var out = new DataOutputStream(conn.getOutputStream())) {
                out.writeBytes(body);
            }

        }
        String line;
        var response = new StringBuilder();
        try (var in = conn.getInputStream()) {
            var rd = new BufferedReader(new InputStreamReader(in));
            while ((line = rd.readLine()) != null) {
                response.append(line);
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            var in = conn.getErrorStream();
            var rd = new BufferedReader(new InputStreamReader(in));
            while ((line = rd.readLine()) != null) {
                response.append(line);
            }
            var err = new Gson().fromJson(response.toString(),
                ClientError.class);
            throw new Exception(err.message());
        }
    }
};
