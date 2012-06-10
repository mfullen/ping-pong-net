package ping.pong.net.client.io;

import java.net.DatagramSocket;
import java.net.Socket;
import ping.pong.net.connection.config.ConnectionConfiguration;
import ping.pong.net.connection.io.AbstractIoConnection;
import ping.pong.net.connection.messaging.ConnectionIdMessage;

/**
 *
 * @author mfullen
 */
public final class ClientIoConnection<MessageType> extends AbstractIoConnection<MessageType>
{
    public ClientIoConnection(ConnectionConfiguration config, Socket tcpSocket, DatagramSocket udpSocket)
    {
        super(config, tcpSocket, udpSocket);
    }

    @Override
    protected void processMessage(MessageType message)
    {
        if (message instanceof ConnectionIdMessage.ResponseMessage)
        {
            int id = ((ConnectionIdMessage.ResponseMessage) message).getId();
            this.setConnectionId(id);
            logger.trace("Got Id from server {}", this.getConnectionId());

            //fire client connected event
            this.fireOnSocketCreated();
        }
        else
        {
            logger.trace("({}) Message taken to be processed ({})", this.getConnectionId(), message);
            this.fireOnSocketMessageReceived(message);
        }
    }
}
