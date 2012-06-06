package ping.pong.net.client.io;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public static final Logger logger = LoggerFactory.getLogger(IoClientConnectionImpl.class);
    protected ConnectionConfiguration config = null;
    protected IoClientImpl<MessageType> client = null;
    protected boolean connected = false;
    protected int connectionId = -1;
    private IoTcpReadRunnable<MessageType> ioTcpReadRunnable = null;
    private ExecutorService executorService = Executors.newFixedThreadPool(4);
    private LinkedBlockingQueue<MessageType> receiveQueue = new LinkedBlockingQueue<MessageType>();
    private final Object connectLock = new Object();

    public IoClientConnectionImpl(IoClientImpl<MessageType> client, ConnectionConfiguration config)
    {
        this.config = config;
        this.client = client;
        this.ioTcpReadRunnable = new IoTcpReadRunnable<MessageType>(this, this);
    }

    @Override
    public void close()
    {
        this.ioTcpReadRunnable.close();
    }

    @Override
    public synchronized  boolean isConnected()
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

        if (id > 0)
        {
            synchronized (this.connectLock)
            {
                this.connected = true;
                this.connectLock.notifyAll();
            }
        }
    }

    @Override
    public void sendMessage(Envelope<MessageType> message)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void run()
    {
        this.executorService.execute(this.ioTcpReadRunnable);

        synchronized (this.connectLock)
        {
            try
            {
                this.connectLock.wait();
            }
            catch (InterruptedException ex)
            {
                logger.error("Connect lock error", ex);
            }
        }
        while (this.isConnected())
        {
            try
            {
                MessageType message = this.receiveQueue.take();
                this.client.handleMessageReceived(message);
            }
            catch (InterruptedException ex)
            {
                logger.error("Error processing Receive Message queue", ex);
            }
        }
    }

    @Override
    public ConnectionConfiguration getConnectionConfiguration()
    {
        return this.config;
    }

    @Override
    public void enqueueReceivedMessage(MessageType message)
    {
        this.receiveQueue.add(message);
    }

    @Override
    public void enqueueMessageToWrite(Envelope<MessageType> message)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
