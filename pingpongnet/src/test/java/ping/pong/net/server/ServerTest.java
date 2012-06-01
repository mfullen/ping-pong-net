package ping.pong.net.server;

import java.util.Collection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import ping.pong.net.connection.Connection;
import ping.pong.net.connection.MessageListener;

/**
 *
 * @author mfullen
 */
public class ServerTest
{
    public ServerTest()
    {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }

    @Test
    public void serverSetupTest()
    {
        Server server = new DefaultServer();
        assertNotNull(server);
        assertFalse(server.isListening());
        assertFalse(server.hasConnections());

        server.start();
        assertTrue(server.isListening());
        assertFalse(server.hasConnections());

        server.shutdown();
        assertFalse(server.isListening());
    }
}
