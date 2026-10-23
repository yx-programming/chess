package client;

import java.net.*;
import java.io.*;

public class ServerFacade {
    private URL url;
    private int port;
    
    public ServerFacade(String url, int port) throws MalformedURLException {
        this.url = new URL(url);
        this.port = port;
    }

    private void httpRequest(String method, String body, String authHeader)
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

            var out = new DataOutputStream(conn.getOutputStream());
            out.writeBytes(body);
        }
    }
};
