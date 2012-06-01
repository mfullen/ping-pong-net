package ping.pong.net.connection;

import ping.pong.net.server.DefaultIoServerConnection;
import java.net.DatagramSocket;
import java.net.Socket;

/**
 *
 * @author mfullen
 */
public final class ConnectionFactory
{
    public static <Message> Connection<Message> createDefaultIOServerConnection(ConnectionConfiguration config, Socket tcpSocket, DatagramSocket udpSocket)
    {
        return new DefaultIoServerConnection<Message>(config, tcpSocket, udpSocket);
    }

    public static ConnectionConfiguration createConnectionConfiguration(String ip, int tcpPort, int udpPort, boolean ssl)
    {
        return new DefaultConnectionConfiguration(udpPort, udpPort, ip, ssl);
    }

    public static ConnectionConfiguration createConnectionConfiguration()
    {
        return new DefaultConnectionConfiguration();
    }
}
