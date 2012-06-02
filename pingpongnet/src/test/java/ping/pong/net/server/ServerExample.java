package ping.pong.net.server;

import ping.pong.net.server.io.IoServerImpl;
import java.io.IOException;
import java.net.UnknownHostException;
import ping.pong.net.connection.Connection;

/**
 *
 * @author mfullen
 */
public class ServerExample
{
    public static void main(String[] args) throws UnknownHostException,
                                                  IOException,
                                                  InterruptedException
    {
        //Server<String> server = new DefaultIoServerConnection<String>(ConnectionFactory.createConnectionConfiguration(), new Socket("localhost", 5011), null);
        //Server<Envelope<String>> server = new IoServerImpl<String>();
        IoServerImpl<String> server = new IoServerImpl<String>();
        server.addConnectionListener(new ServerConnectionListener()
        {
            @Override
            public void connectionAdded(Server server, Connection conn)
            {
                System.out.println("Connection Added");
            }

            @Override
            public void connectionRemoved(Server server, Connection conn)
            {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });

//        server.start();
//
//        Thread.sleep(5000);
//        server.shutdown();

        // Thread.sleep(5000);
        server.start();
    }
}
