package ping.pong.net.server;

import ping.pong.net.connection.Connection;

/**
 *
 * @author mfullen
 */
public interface ServerConnectionListener
{
    void connectionAdded(Server server, Connection conn);

    void connectionRemoved(Server server, Connection conn);
}
