package ping.pong.net;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.BeforeClass;
import ping.pong.net.server.Server;
import static org.junit.Assert.*;

/**
 *
 * @author mfullen
 */
public class AppTest {

    public AppTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of main method, of class App.
     */
    @Test
    public void serverSetupTest() {
        Server server = null;
        assertNotNull(server);
        assertFalse(server.isRunning());
        assertFalse(server.hasConnections());

        server.start();
        assertTrue(server.isRunning());
        assertFalse(server.hasConnections());

        server.close();
        assertFalse(server.isRunning());
    }
}
