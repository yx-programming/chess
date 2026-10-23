package client;

public class PostLoginState extends ClientState {
    private String authToken;
    
    public PostLoginState(String authToken) {
        this.authToken = authToken;
    }

    public String prompt() {
        return "";
    }
}
