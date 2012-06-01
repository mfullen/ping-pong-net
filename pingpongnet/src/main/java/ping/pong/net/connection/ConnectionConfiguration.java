package ping.pong.net.connection;

/**
 *
 * @author mfullen
 */
public interface ConnectionConfiguration
{
    /**
     * Get the port the TCP connection uses
     * @return
     */
    int getPort();

    /**
     * Set the port the TCP connection will use
     * @param port
     */
    void setPort(int port);

    /**
     * Get the port the UDP connection uses
     * @return
     */
    int getUdpPort();

    /**
     * Set the port the UDP connection will use
     * @param port
     */
    void setUdpPort(int udpport);

    /**
     * Get the Ip Address
     * @return
     */
    String getIpAddress();

    /**
     * Set the Ip Address
     * @param ipAddress
     */
    void setIpAddress(String ipAddress);

    /**
     * Returns true if ssl is enabled false if not
     * @return
     */
    boolean isSsl();

    /**
     * Sets SSL security
     * @param sslEnabled
     */
    void setSsl(boolean sslEnabled);
}
