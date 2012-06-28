package ping.pong.net.server;

import ping.pong.net.connection.Connection;

/**
 *
 * @author mfullen
 */
public interface ServerConnectionListener<MessageType>
{
    /**
     *
     * @param server
     * @param conn
     */
    void connectionAdded(Server<MessageType> server, Connection<MessageType> conn);

    /**
     *
     * @param server
     * @param conn
     */
    void connectionRemoved(Server<MessageType> server, Connection<MessageType> conn);
}
