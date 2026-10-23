package client;

import java.net.*;
import java.io.*;

public class ServerFacade {
    private String baseUrl;
    private int port;
    
    public ServerFacade(String url, int port) {
        baseUrl = url;
        this.port = port;
    }


    public String register(String username, String password, String email)
            throws Exception {
        return httpRequest("user", "POST",
            "{\"username\":\"" + username + "\",\"password\":\"" + password +
            "\",\"email\":\"" + email + "\"}", null);
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
            return response.toString();
        }
    }
};
