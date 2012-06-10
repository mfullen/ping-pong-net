package ping.pong.net.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import ping.pong.net.connection.Connection;
import ping.pong.net.connection.messaging.MessageListener;
import ping.pong.net.server.io.IoServer;

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

    @Test
    @Ignore //replace when Client Object is made, we can't block in the unit test thread
    public void serverTcpReadAndWrite() throws UnknownHostException, IOException,
                                               InterruptedException
    {
        Server server = new IoServer();
        server.addConnectionListener(new ServerConnectionListener()
        {
            int count = 0;

            @Override
            public void connectionAdded(Server server, Connection conn)
            {
                assertNotNull(server.getConnection(conn.getConnectionId()));
                count++;
                System.out.println("count: " + count);
            }

            @Override
            public void connectionRemoved(Server server, Connection conn)
            {
                count--;
                if (!server.hasConnections())
                {
                    server.shutdown();
                    assertFalse(server.isListening());
                }
                else
                {
                    assertEquals(count, server.getConnections().size());
                }
            }
        });

        server.start();
        {
            Socket client = new Socket("localhost", 5011);

            ObjectInputStream inputStream = new ObjectInputStream(client.getInputStream());
            boolean useReadObject = true;

            if (useReadObject)
            {
                int readInt = inputStream.readInt();
                // int readObj = inputStream.readInt();
                assertEquals(readInt, 1);
            }
            else
            {
                int data = -1;
                while ((data = inputStream.read()) != -1)
                {
                    System.out.println("Data: " + data);
                }
            }

            ObjectOutputStream outputStream = new ObjectOutputStream(client.getOutputStream());
            outputStream.flush();
            outputStream.writeObject(new byte[]
                    {
                        1, 3, 2, 3
                    });
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            client.close();
        }
    }

    @Test
    @Ignore // we arent really suppose to use these as unit tests, they are more example cases
    public void serverStartUpAndShutDown() throws InterruptedException
    {
        Server server = new IoServer();
        server.addConnectionListener(new ServerConnectionListener()
        {
            int count = 0;

            @Override
            public void connectionAdded(Server server, Connection conn)
            {
                assertNotNull(server.getConnection(conn.getConnectionId()));
                count++;
                System.out.println("count: " + count);
            }

            @Override
            public void connectionRemoved(Server server, Connection conn)
            {
                count--;
                if (!server.hasConnections())
                {
                    server.shutdown();
                    assertFalse(server.isListening());
                }
                else
                {
                    assertEquals(count, server.getConnections().size());
                }
            }
        });

        server.start();
        //Thread.sleep(500);
        server.shutdown();
    }
}
