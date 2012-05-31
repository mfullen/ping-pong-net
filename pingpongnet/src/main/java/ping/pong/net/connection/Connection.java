package ping.pong.net.connection;

/**
 *
 * @author mfullen
 */
public interface Connection extends MessageSender<Object>
{
    /**
     * Closes the connection
     */
    void close();

    /**
     * If the connection is still connected to a source
     * @return true if connected, false if not connected
     */
    public boolean isConnected();

    /**
     * The identification number of the connection
     * @return identification number of the connection
     */
    public int getConnectionID();
}
