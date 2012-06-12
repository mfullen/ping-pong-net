package ping.pong.net.client.io;

import java.net.DatagramSocket;
import java.net.Socket;
import ping.pong.net.connection.config.ConnectionConfiguration;
import ping.pong.net.connection.io.AbstractIoConnection;
import ping.pong.net.connection.messaging.ConnectionIdMessage;
import ping.pong.net.connection.messaging.DisconnectMessage;

/**
 * Client Io Connection class. This class extends from AbstractIoConnection
 * @author mfullen
 */
final class ClientIoNonPPNConnection<MessageType> extends AbstractIoConnection<MessageType>
{
    public ClientIoNonPPNConnection(ConnectionConfiguration config, Socket tcpSocket, DatagramSocket udpSocket)
    {
        super(config, tcpSocket, udpSocket);
    }

    /**
     * ProcessMessage on the client first checks if the message is of type
     * ConnectionIdMessage.ResponseMessage. ConnectionIdMessage.ResponseMessage
     * indicates that the Connection has received an Identifier from the server and
     * can be considered connected
     * @param message the message being processed
     */
    @Override
    protected void processMessage(MessageType message)
    {
        if (message instanceof DisconnectMessage)
        {
            this.connected = false;
        }
        else
        {
            logger.trace("({}) Message taken to be processed ({})", this.getConnectionId(), message);
            this.fireOnSocketMessageReceived(message);
        }
    }

    @Override
    public void run()
    {

        this.executorService.execute(ioTcpWriteRunnable);
        this.executorService.execute(ioTcpReadRunnable);
        this.connected = true;

        this.fireOnSocketCreated();

        while (this.isConnected())
        {
            try
            {
                logger.trace("({}) About to block to Take message off queue", this.getConnectionId());
                MessageType message = this.receiveQueue.take();

                logger.trace("({}) Message taken to be processed ({})", this.getConnectionId(), message);
                this.processMessage(message);
            }
            catch (InterruptedException ex)
            {
                logger.error("Error processing Receive Message queue", ex);
            }
        }

        //Connection is done, try to properly close and cleanup
        logger.debug("{} Main thread calling close", getConnectionName());
        this.close();
    }
}
