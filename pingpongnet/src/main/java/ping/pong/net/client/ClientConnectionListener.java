package ping.pong.net.client;

import ping.pong.net.connection.DisconnectInfo;

/**
 *
 * @author mfullen
 */
public interface ClientConnectionListener
{
    void clientConnected(Client client);

    void clientDisconnected(Client client, DisconnectInfo info);
}
