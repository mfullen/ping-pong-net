package ping.pong.net.server;

import ping.pong.net.client.Client;
import ping.pong.net.client.ClientConnectionListener;
import ping.pong.net.client.io.IoClientImpl;
import ping.pong.net.connection.config.ConnectionConfigFactory;
import ping.pong.net.connection.DisconnectInfo;
import ping.pong.net.connection.messaging.EnvelopeFactory;
import ping.pong.net.connection.messaging.MessageListener;

public class MyClient
{
    public static void main(String[] args) throws InterruptedException
    {
        runOneClient();
        // runMulitClients();
    }

    public static void runMulitClients()
    {
        for (int i = 0; i < 5; i++)
        {
            IoClientImpl<String> client = new IoClientImpl<String>(ConnectionConfigFactory.createConnectionConfiguration());
            client.addMessageListener(new MessageListener<Client, String>()
            {
                @Override
                public void messageReceived(Client source, String message)
                {
                    System.out.println("CLient Id: " + source.getId());
                    System.out.println("Message: " + message);

                    source.sendMessage(EnvelopeFactory.createTcpEnvelope("Hello I am Connection: " + source.getId()));
                }
            });
            client.start();
        }
    }

    public static void runOneClient() throws InterruptedException
    {
        IoClientImpl<String> client = new IoClientImpl<String>(ConnectionConfigFactory.createConnectionConfiguration());
        client.addMessageListener(new MessageListener<Client, String>()
        {
            @Override
            public void messageReceived(Client source, String message)
            {
                System.out.println("CLient Id: " + source.getId());
                System.out.println("Message: " + message);


            }
        });
        client.addConnectionListener(new ClientConnectionListener()
        {
            @Override
            public void clientConnected(Client client)
            {
                client.sendMessage(EnvelopeFactory.createTcpEnvelope("Hello I am Connection: " + client.getId()));
            }

            @Override
            public void clientDisconnected(Client client, DisconnectInfo info)
            {
                System.out.println("I have been disconnected");
            }
        });
        client.start();
        //Thread.sleep(500);

    }
}
