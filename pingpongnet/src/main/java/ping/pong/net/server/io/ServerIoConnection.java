package ping.pong.net.server.io;

import java.net.DatagramSocket;
import java.net.Socket;
import ping.pong.net.connection.DataReader;
import ping.pong.net.connection.DataWriter;
import ping.pong.net.connection.config.ConnectionConfiguration;
import ping.pong.net.connection.io.AbstractIoConnection;
import ping.pong.net.connection.messaging.DisconnectMessage;

/**
 *
 * @author mfullen
 */
final class ServerIoConnection<MessageType> extends AbstractIoConnection<MessageType>
{
    public ServerIoConnection(ConnectionConfiguration config, DataReader dataReader, DataWriter dataWriter, Socket tcpSocket, DatagramSocket udpSocket)
    {
        super(config, dataReader, dataWriter, tcpSocket, udpSocket);
    }

    @Override
    protected void processMessage(MessageType message)
    {
        if (message instanceof DisconnectMessage)
        {
            this.connected = false;
        }
        else
        {
            this.fireOnSocketMessageReceived(message);
        }
    }

    @Override
    protected synchronized void fireOnSocketCreated()
    {
        super.fireOnSocketCreated();
    }
}
