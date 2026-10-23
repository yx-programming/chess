package client;

public class PostLoginState extends ClientState {
    private String username;
    private String authToken;
    
    public PostLoginState(String username, String authToken) {
        this.username = username;
        this.authToken = authToken;
    }

    public String prompt() {
        return "[" + username + "]";
    }
}
