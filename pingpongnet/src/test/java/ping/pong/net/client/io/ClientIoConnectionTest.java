package ping.pong.net.client.io;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mfullen
 */
public class ClientIoConnectionTest
{
    public ClientIoConnectionTest()
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
     * Test of processMessage method, of class ClientIoConnection.
     */
    @Test
    public void testProcessMessage()
    {
        System.out.println("processMessage");
        Object message = null;
        ClientIoConnection instance = null;
        instance.processMessage(message);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
