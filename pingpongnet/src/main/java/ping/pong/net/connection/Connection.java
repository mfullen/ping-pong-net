package ping.pong.net.connection;

import ping.pong.net.server.Server;

/**
 *
 * @author mfullen
 */
public interface Connection<Message> extends
        MessageSender<Envelope<Message>>,
        Runnable
{
    /**
     * Closes the connection
     */
    void close();

    /**
     * If the connection is still connected to a source
     * @return true if connected, false if not connected
     */
    boolean isConnected();

    /**
     * The identification number of the connection
     * @return identification number of the connection
     */
    int getConnectionId();

    /**
     * Set the connection Id
     */
    void setConnectionId(int id);

    /**
     * Gets the server that is hosting the connection
     * @return
     */
    Server<Envelope<Message>> getServer();
}
