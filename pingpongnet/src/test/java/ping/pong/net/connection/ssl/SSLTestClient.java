package ping.pong.net.connection.ssl;

import javax.net.ssl.*;
import ping.pong.net.client.Client;
import ping.pong.net.client.ClientConnectionListener;
import ping.pong.net.client.io.IoClient;
import ping.pong.net.connection.Connection;
import ping.pong.net.connection.DisconnectInfo;
import ping.pong.net.connection.config.ConnectionConfigFactory;
import ping.pong.net.connection.config.ConnectionConfiguration;
import ping.pong.net.connection.messaging.EnvelopeFactory;
import ping.pong.net.connection.messaging.MessageListener;
import ping.pong.net.server.Server;
import ping.pong.net.server.ServerConnectionListener;

/**
 *
 * @author mfullen
 */
public class SSLTestClient
{
    public static void main(String[] args)
    {
        usePPNClient();
        //clientWithKeyStores();
        // clientWithoutKeyStore();
    }

    public static void usePPNClient()
    {
        Client<String> client = new IoClient<String>(ConnectionConfigFactory.createPPNClientConfig("localhost", 2011, true));
        client.addConnectionListener(new ClientConnectionListener<String>()
        {
            @Override
            public void clientConnected(Client<String> client)
            {
                client.sendMessage(EnvelopeFactory.createTcpEnvelope("Test From Client"));
            }

            @Override
            public void clientDisconnected(Client<String> client, DisconnectInfo info)
            {
            }
        });
        client.addMessageListener(new MessageListener<Client, String>()
        {
            @Override
            public void messageReceived(Client source, String message)
            {
                System.out.println("Received Message: " + message);
            }
        });
        client.start();
    }

    public static void clientWithoutKeyStore()
    {
        try
        {
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket clientSocket = (SSLSocket) factory.createSocket("localhost", 5011);
            clientSocket.setSoTimeout(5000);
            clientSocket.addHandshakeCompletedListener(new HandshakeCompletedListener()
            {
                @Override
                public void handshakeCompleted(HandshakeCompletedEvent hce)
                {
                    System.out.println("Handshake complete");
                }
            });
            clientSocket.startHandshake();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void clientWithKeyStores()
    {
        try
        {
            ConnectionConfiguration config = ConnectionConfigFactory.createPPNServerConfig(2011, true);
            SSLContext ctx = SSLUtils.createSSLContext("SSLv3", config);// SSLContext.getInstance("SSLv3");
            ctx.getClientSessionContext().setSessionTimeout(5);
            SSLSocketFactory factory = ctx.getSocketFactory();
            SSLSocket clientSocket = (SSLSocket) factory.createSocket("localhost", 2011);
            clientSocket.setSoTimeout(5000);
            clientSocket.addHandshakeCompletedListener(new HandshakeCompletedListener()
            {
                @Override
                public void handshakeCompleted(HandshakeCompletedEvent hce)
                {
                    System.out.println("Handshake complete");
                }
            });
            clientSocket.startHandshake();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
