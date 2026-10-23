package client;

import java.net.*;
import java.io.*;

public class ServerFacade {
    private URL url;
    private int port;
    
    public ServerFacade(String url, int port) throws MalformedURLException {
        this.url = new URL(url + ":" + port);
        this.port = port;
    }

    private String httpRequest(String method, String body, String authHeader)
            throws IOException {
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
