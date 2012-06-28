package ping.pong.net.client;

import ping.pong.net.connection.DisconnectInfo;

/**
 * Listeners used for Connection events of a Client
 *
 * @author mfullen
 */
public interface ClientConnectionListener<MessageType>
{
    /**
     * Event for when a Client connects to a server
     *
     * @param client the client which connected
     */
    void clientConnected(Client<MessageType> client);

    /**
     * Event for when a Client Disconnects from a server
     *
     * @param client the client which disconnected
     * @param info information about the disconnection
     */
    void clientDisconnected(Client<MessageType> client, DisconnectInfo info);
}
