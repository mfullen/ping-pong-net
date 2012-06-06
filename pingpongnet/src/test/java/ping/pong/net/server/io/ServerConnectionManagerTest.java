package ping.pong.net.server.io;

import java.io.File;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.SSLSocket;
import java.net.DatagramSocket;
import java.io.IOException;
import java.net.Socket;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
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
        //assertNotNull(instance.executorService);
        instance.shutdown();
        assertNull(instance.tcpServerSocket);
        assertNull(instance.udpServerSocket);
        //assertNull(instance.executorService);
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
    public void testRunSSL()
    {
        ConnectionConfiguration createConnectionConfiguration = ConnectionFactory.createConnectionConfiguration("localhost", 4011, 4012, true);
        ServerConnectionManager instance = new ServerConnectionManager(createConnectionConfiguration, new IoServerImpl(createConnectionConfiguration));
        Thread th = new Thread(instance);
        th.start();
        assertTrue(instance.isListening());
        instance.shutdown();
        assertFalse(instance.isListening());
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
            wait(100);
        }
        Socket client = SocketFactory.getDefault().createSocket("localhost", 6011);

        th.join(1500);
        assertTrue(instance.isListening());
        instance.shutdown();
        assertFalse(instance.isListening());
    }

    @Test
    public void testRunAcceptedSocketSSL() throws IOException,
                                                  InterruptedException,
                                                  URISyntaxException,
                                                  NoSuchAlgorithmException,
                                                  KeyStoreException,
                                                  CertificateException,
                                                  KeyManagementException
    {
        ConnectionConfiguration createConnectionConfiguration = ConnectionFactory.createConnectionConfiguration("localhost", 8011, 8012, true);
        ServerConnectionManager instance = new ServerConnectionManager(createConnectionConfiguration, new IoServerImpl(createConnectionConfiguration));
        Thread th = new Thread(instance);
        th.start();

        synchronized (this)
        {
            wait(500);
        }

        KeyStore ks = KeyStore.getInstance("JKS");
        String absolutePath = new File(Thread.currentThread().getContextClassLoader().getResource(ConnectionConfiguration.DEFAULT_KEY_STORE).toURI()).getAbsolutePath();
        ks.load(new FileInputStream(absolutePath), (ConnectionConfiguration.DEFAULT_KEY_STORE_PASSWORD).toCharArray());
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);
        SSLContext sslcontext = SSLContext.getInstance("SSLv3");
        sslcontext.init(null, tmf.getTrustManagers(), null);

        SSLSocketFactory factory = (SSLSocketFactory) sslcontext.getSocketFactory();
        SSLSocket client = (SSLSocket) factory.createSocket("localhost", 8011);
        client.startHandshake();

        th.join(1500);
        assertTrue(instance.isListening());
        instance.shutdown();
        assertFalse(instance.isListening());
    }
}
