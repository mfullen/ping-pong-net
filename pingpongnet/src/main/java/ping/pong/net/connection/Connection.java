package ping.pong.net.connection;

import ping.pong.net.connection.messaging.MessageSender;
import ping.pong.net.connection.config.ConnectionConfiguration;
import ping.pong.net.connection.messaging.Envelope;

/**
 * A Connection interface represent what an Active connection between sockets
 * is.
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
     *
     * @return true if connected, false if not connected
     */
    boolean isConnected();

    /**
     * The identification number of the connection
     *
     * @return identification number of the connection
     */
    int getConnectionId();

    /**
     * Set the connection Id
     */
    void setConnectionId(int id);

    /**
     * Get the Connections Configuration used for creating the sockets
     *
     * @return ConnectionConfiguration
     */
    ConnectionConfiguration getConnectionConfiguration();

    /**
     * Add a ConnectionEvent listener
     *
     * @param listener the listener to add
     */
    void addConnectionEventListener(ConnectionEvent listener);

    /**
     * Remove a Connection Event listener
     *
     * @param listener the listener to add
     */
    void removeConnectionEventListener(ConnectionEvent listener);

    boolean isUsingCustomSerialization();
}
