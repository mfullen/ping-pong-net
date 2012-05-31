package ping.pong.net.server;

import ping.pong.net.connection.Connection;

/**
 *
 * @author mfullen
 */
public interface ServerConnectionListener
{
    public void connectionAdded(Server server, Connection conn);

    public void connectionRemoved(Server server, Connection conn);
}
