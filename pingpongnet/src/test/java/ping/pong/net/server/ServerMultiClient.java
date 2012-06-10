package ping.pong.net.server;

import java.util.Timer;
import java.util.TimerTask;
import ping.pong.net.client.Client;
import ping.pong.net.client.io.IoClientImpl;
import ping.pong.net.connection.Connection;
import ping.pong.net.connection.ConnectionFactory;
import ping.pong.net.connection.Envelope;
import ping.pong.net.connection.EnvelopeFactory;
import ping.pong.net.connection.MessageListener;
import ping.pong.net.server.io.IoServerImpl;

public class ServerMultiClient
{
    public static void main(String[] args)
    {
        final IoServerImpl<String> server = new IoServerImpl<String>(ConnectionFactory.createConnectionConfiguration("localhost", 5011, 5012, false));
        server.addConnectionListener(new ServerConnectionListener()
        {
            @Override
            public void connectionAdded(Server server, Connection conn)
            {
                System.out.println("Connection Added");
                //server.broadcast(EnvelopeFactory.createTcpEnvelope("Test"));
            }

            @Override
            public void connectionRemoved(Server server, Connection conn)
            {
                System.out.println("Connection removed was " + conn.getConnectionId());
            }
        });
        server.addMessageListener(new MessageListener<Connection, String>()
        {
            @Override
            public void messageReceived(Connection source, String message)
            {
                System.out.println("Message Received On Server From: " + source.getConnectionId());
                System.out.println("Message: " + message);
            }
        });

        server.start();

        boolean heartBeat = false;

        if (heartBeat)
        {
            Timer timer = new Timer("HeatBeat", true);
            timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    server.broadcast(new Envelope<String>()
                    {
                        @Override
                        public boolean isReliable()
                        {
                            return true;
                        }

                        @Override
                        public String getMessage()
                        {
                            return "HeartBeat";
                        }
                    });
                }
            }, 500, 1000);
        }

    }
}
