package ping.pong.net.client.io;

import ping.pong.net.connection.Connection;
import ping.pong.net.connection.ConnectionConfiguration;
import ping.pong.net.connection.Envelope;
import ping.pong.net.connection.MessageProcessor;

/**
 *
 * @author mfullen
 */
final class IoClientConnectionImpl<MessageType> implements
        Connection<MessageType>,
        MessageProcessor<MessageType>
{
    protected ConnectionConfiguration config = null;
    protected IoClientImpl<MessageType> client = null;
    protected boolean connected = false;
    protected int connectionId = -1;

    public IoClientConnectionImpl(IoClientImpl<MessageType> client, ConnectionConfiguration config)
    {
        this.config = config;
        this.client = client;
    }

    @Override
    public void close()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isConnected()
    {
        return this.connected;
    }

    @Override
    public int getConnectionId()
    {
        return this.connectionId;
    }

    @Override
    public void setConnectionId(int id)
    {
        this.connectionId = id;
    }

    @Override
    public void sendMessage(Envelope<MessageType> message)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void run()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ConnectionConfiguration getConnectionConfiguration()
    {
        return this.config;
    }

    @Override
    public void enqueueReceivedMessage(MessageType message)
    {
        //add to queue
        // client.
    }

    @Override
    public void enqueueMessageToWrite(Envelope<MessageType> message)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
