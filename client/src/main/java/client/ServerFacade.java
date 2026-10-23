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


    public void register() throws Exception {
        System.out.println(httpRequest("register", "POST", "{\"username\":\"fred\",\"password\":\"pass\",\"email\":\"testemail\"}", null));
    }

    protected String httpRequest(String endpoint, String method, String body,
            String authHeader) throws Exception {
        var urlString = "http://" + baseUrl + ":" + port + "/" + endpoint;
        System.out.println("FINDME: " + urlString);
        var url = new URL(urlString);
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
