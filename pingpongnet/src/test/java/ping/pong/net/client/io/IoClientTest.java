package ping.pong.net.client.io;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import ping.pong.net.client.Client;
import ping.pong.net.client.ClientConnectionListener;
import ping.pong.net.connection.messaging.Envelope;
import ping.pong.net.connection.messaging.MessageListener;

/**
 *
 * @author mfullen
 */
public class IoClientTest
{
    public IoClientTest()
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
     * Test of start method, of class IoClient.
     */
    @Test
    public void testStart()
    {
        System.out.println("start");
        IoClient instance = new IoClient();
        instance.start();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of close method, of class IoClient.
     */
    @Test
    public void testClose()
    {
        System.out.println("close");
        IoClient instance = new IoClient();
        instance.close();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isConnected method, of class IoClient.
     */
    @Test
    public void testIsConnected()
    {
        System.out.println("isConnected");
        IoClient instance = new IoClient();
        boolean expResult = false;
        boolean result = instance.isConnected();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getId method, of class IoClient.
     */
    @Test
    public void testGetId()
    {
        System.out.println("getId");
        IoClient instance = new IoClient();
        int expResult = 0;
        int result = instance.getId();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addMessageListener method, of class IoClient.
     */
    @Test
    public void testAddMessageListener()
    {
        System.out.println("addMessageListener");
        MessageListener listener = null;
        IoClient instance = new IoClient();
        instance.addMessageListener(listener);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeMessageListener method, of class IoClient.
     */
    @Test
    public void testRemoveMessageListener()
    {
        System.out.println("removeMessageListener");
        MessageListener listener = null;
        IoClient instance = new IoClient();
        instance.removeMessageListener(listener);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addConnectionListener method, of class IoClient.
     */
    @Test
    public void testAddConnectionListener()
    {
        System.out.println("addConnectionListener");
        ClientConnectionListener listener = null;
        IoClient instance = new IoClient();
        instance.addConnectionListener(listener);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeConnectionListener method, of class IoClient.
     */
    @Test
    public void testRemoveConnectionListener()
    {
        System.out.println("removeConnectionListener");
        ClientConnectionListener listener = null;
        IoClient instance = new IoClient();
        instance.removeConnectionListener(listener);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sendMessage method, of class IoClient.
     */
    @Test
    public void testSendMessage()
    {
        System.out.println("sendMessage");
        Envelope message = null;
        IoClient instance = new IoClient();
        instance.sendMessage(message);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
