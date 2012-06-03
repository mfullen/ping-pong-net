package ping.pong.net.connection;

/**
 *
 * @author mfullen
 */
public final class ConnectionFactory
{
//    public static Connection<? extends Envelope> createDefaultIOServerConnection(ConnectionConfiguration config, Socket tcpSocket, DatagramSocket udpSocket)
//    {
//        return new DefaultIoServerConnection(config, tcpSocket, udpSocket);
//    }

    public static ConnectionConfiguration createConnectionConfiguration(String ip, int tcpPort, int udpPort, boolean ssl)
    {
        return new DefaultConnectionConfiguration(tcpPort, udpPort, ip, ssl);
    }

    public static ConnectionConfiguration createConnectionConfiguration()
    {
        return new DefaultConnectionConfiguration();
    }
}
