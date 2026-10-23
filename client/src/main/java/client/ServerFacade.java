package client;

import java.net.*;
import java.io.*;
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
        try (var in = conn.getInputStream()) {
            var rd = new BufferedReader(new InputStreamReader(in));
            var response = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
            }
            rd.close();
            String result = response.toString();
            if (result.contains("message")) {
                var err = new Gson().fromJson(result, ClientError.class);
                throw new Exception(err.message());
            }
            return result;
        }
    }
};
