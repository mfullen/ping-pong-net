package ping.pong.net;

import ping.pong.net.client.Client;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.BeforeClass;
import static org.junit.Assert.*;

/**
 *
 * @author mfullen
 */
public class AppTest
{
    public AppTest()
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

    /**
     * Test of main method, of class App.
     */
    @Test
    public void clientSetupTest()
    {
        Client client = null;
        assertNotNull(client);
        assertFalse(client.isConnected());
        assertFalse(client.isRunning());

        client.start();
        assertTrue(client.isRunning());
        assertFalse(client.isConnected());

        client.close();
        assertFalse(client.isRunning());
        assertFalse(client.isConnected());
    }
}
