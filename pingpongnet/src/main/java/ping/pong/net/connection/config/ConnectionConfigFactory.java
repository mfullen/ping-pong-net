package ping.pong.net.connection.config;

/**
 *
 * @author mfullen
 */
public final class ConnectionConfigFactory
{
    public static ConnectionConfiguration createConnectionConfiguration(String ip, int tcpPort, int udpPort, boolean ssl)
    {
        return new DefaultConnectionConfiguration(tcpPort, udpPort, ip, ssl);
    }

    public static ConnectionConfiguration createConnectionConfiguration(String ip, int tcpPort, int udpPort, boolean ssl, boolean usingPingPongServer)
    {
        DefaultConnectionConfiguration defaultConnectionConfiguration = new DefaultConnectionConfiguration(tcpPort, udpPort, ip, ssl);
        defaultConnectionConfiguration.setUsingPingPongNetSerialization(usingPingPongServer);
        return defaultConnectionConfiguration;
    }

    public static ConnectionConfiguration createConnectionConfiguration()
    {
        return new DefaultConnectionConfiguration();
    }
}
