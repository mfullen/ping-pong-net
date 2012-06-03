package ping.pong.net.server.io;

import java.net.DatagramSocket;
import java.io.IOException;
import java.net.Socket;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import ping.pong.net.connection.ConnectionConfiguration;
import ping.pong.net.connection.ConnectionFactory;
import static org.junit.Assert.*;

/**
 *
 * @author mfullen
 */
public class ServerConnectionManagerTest
{
    public ServerConnectionManagerTest()
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
     * Test of shutdown method, of class ServerConnectionManager.
     */
    @Test
    public void testShutdown()
    {
        ServerConnectionManager instance = new ServerConnectionManager(ConnectionFactory.createConnectionConfiguration(), new IoServerImpl());
        assertNull(instance.tcpServerSocket);
        assertNull(instance.udpServerSocket);
        assertNotNull(instance.executorService);
        instance.shutdown();
        assertNull(instance.tcpServerSocket);
        assertNull(instance.udpServerSocket);
        assertNull(instance.executorService);
        instance.shutdown();
    }

    @Test
    public void testShutdownWithNotNulLSockets() throws IOException
    {
        ServerConnectionManager instance = new ServerConnectionManager(ConnectionFactory.createConnectionConfiguration("localhost", 20111, 20112, false), new IoServerImpl());
        instance.tcpServerSocket = ServerSocketFactory.getDefault().createServerSocket();
        instance.udpServerSocket = new DatagramSocket(instance.configuration.getUdpPort());
        assertNotNull(instance.tcpServerSocket);
        assertNotNull(instance.udpServerSocket);
        instance.shutdown();
    }

    /**
     * Test of isListening method, of class ServerConnectionManager.
     */
    @Test
    public void testIsListening()
    {
        ServerConnectionManager instance = new ServerConnectionManager(ConnectionFactory.createConnectionConfiguration(), new IoServerImpl());
        assertTrue(instance.isListening());
        instance.shutdown();
        assertFalse(instance.isListening());
    }

    @Test
    @Ignore
    public void testRunSSL()
    {
        ConnectionConfiguration createConnectionConfiguration = ConnectionFactory.createConnectionConfiguration("localhost", 5011, 5012, true);
        ServerConnectionManager instance = new ServerConnectionManager(createConnectionConfiguration, new IoServerImpl(createConnectionConfiguration));
        instance.run();
        instance.shutdown();
        fail("Need to add SSL Certificates");
    }

    @Test
    public void testRunCreateUDPSocketError() throws IOException
    {
        ConnectionConfiguration createConnectionConfiguration = ConnectionFactory.createConnectionConfiguration("localhost", 7011, 7012, false);
        ServerConnectionManager instance = new ServerConnectionManager(createConnectionConfiguration, new IoServerImpl(createConnectionConfiguration));
        instance.tcpServerSocket = ServerSocketFactory.getDefault().createServerSocket(7011);
        instance.udpServerSocket = new DatagramSocket(instance.configuration.getUdpPort());
        assertTrue(instance.isListening());
        instance.run();
        assertFalse(instance.isListening());
        instance.shutdown();
    }

    @Test
    public void testRunAcceptedSocket() throws IOException, InterruptedException
    {
        ConnectionConfiguration createConnectionConfiguration = ConnectionFactory.createConnectionConfiguration("localhost", 6011, 6012, false);
        ServerConnectionManager instance = new ServerConnectionManager(createConnectionConfiguration, new IoServerImpl(createConnectionConfiguration));
        Thread th = new Thread(instance);
        th.start();

        synchronized (this)
        {
            wait(500);
        }
        Socket client = SocketFactory.getDefault().createSocket("localhost", 6011);

        th.join(1500);
        assertTrue(instance.isListening());
        instance.shutdown();
        assertFalse(instance.isListening());
    }

    @Test
    @Ignore //need SSL certs
    public void testRunAcceptedSocketSSL() throws IOException,
                                                  InterruptedException
    {
        ConnectionConfiguration createConnectionConfiguration = ConnectionFactory.createConnectionConfiguration("localhost", 6011, 6012, true);
        ServerConnectionManager instance = new ServerConnectionManager(createConnectionConfiguration, new IoServerImpl(createConnectionConfiguration));
        Thread th = new Thread(instance);
        th.start();

        synchronized (this)
        {
            wait(500);
        }
        Socket client = SocketFactory.getDefault().createSocket("localhost", 6011);

        th.join(1500);
        assertTrue(instance.isListening());
        instance.shutdown();
        assertFalse(instance.isListening());
    }
}
