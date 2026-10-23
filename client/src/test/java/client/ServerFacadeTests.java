package client;

import org.junit.jupiter.api.*;
import server.Server;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() throws Exception {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("localhost", port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clear() throws Exception {
        facade.httpRequest("db", "DELETE", null, null);
    }

    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    public void registerTestPositive() throws Exception {
        Assertions.assertTrue(
            facade.register("fred", "pass", "email").length() == 36);
    }

    @Test
    public void registerTestNegative() throws Exception {
        facade.register("fred", "pass", "email");
        Assertions.assertThrows(Exception.class,
            () -> facade.register("fred", "pass", "email"));
    }

    @Test
    public void loginTestPositive() throws Exception {
        facade.register("fred", "pass", "email");
        Assertions.assertTrue(
            facade.login("fred", "pass").length() == 36);
    }

    @Test
    public void loginTestNegative() throws Exception {
        facade.register("fred", "pass", "email");
        Assertions.assertThrows(Exception.class,
            () -> facade.login("fred", "passfalse"));
    }

    @Test
    public void logoutTestPositive() throws Exception {
        String auth = facade.register("fred", "pass", "email");
        Assertions.assertDoesNotThrow(() -> facade.logout(auth));
    }

    @Test
    public void logoutTestNegative() throws Exception {
        String auth = facade.register("fred", "pass", "email");
        Assertions.assertThrows(Exception.class, () -> facade.logout("wrong"));
    }

    @Test
    public void createGameTestPositive() throws Exception {
        String auth = facade.register("fred", "pass", "email");
        int id = facade.createGame(auth, "test");
        Assertions.assertTrue(id != 0);
    }

    @Test
    public void createGameTestNegative() throws Exception {
        facade.register("fred", "pass", "email");
        Assertions.assertThrows(Exception.class,
            () -> facade.createGame("bad auth", "test"));
    }
}
