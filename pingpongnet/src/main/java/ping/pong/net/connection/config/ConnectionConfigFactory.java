package ping.pong.net.connection.config;

/**
 *
 * @author mfullen
 */
public final class ConnectionConfigFactory
{
    private ConnectionConfigFactory()
    {
    }

    /**
     * Create a configuration for a client connection.
     * @param ip the ip to connect to
     * @param tcpPort the tcpport to connect to
     * @param udpPort the udp port to connect to
     * @param ssl use ssl for the tcp connection
     * @return
     */
    public static ConnectionConfiguration createConnectionConfiguration(String ip, int tcpPort, int udpPort, boolean ssl)
    {
        return new DefaultConnectionConfiguration(tcpPort, udpPort, ip, ssl);
    }

    public static ConnectionConfiguration createConnectionConfiguration()
    {
        return new DefaultConnectionConfiguration();
    }
}
