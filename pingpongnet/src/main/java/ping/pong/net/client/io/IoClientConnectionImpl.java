package ping.pong.net.client.io;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ping.pong.net.connection.*;

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
    private LinkedBlockingQueue<MessageType> receiveQueue = new LinkedBlockingQueue<MessageType>();
    private final Object connectLock = new Object();

    public IoClientConnectionImpl(IoClientImpl<MessageType> client, ConnectionConfiguration config)
    {
        this.config = config;
        this.client = client;
        this.init();
    }

    protected void init()
    {
        try
        {
            SocketFactory factory = config.isSsl() ? SSLSocketFactory.getDefault() : SocketFactory.getDefault();
            Socket tcpSocket = factory.createSocket(config.getIpAddress(), config.getPort());
            this.ioTcpReadRunnable = new IoTcpReadRunnable<MessageType>(this, this, tcpSocket);
        }
        catch (IOException ex)
        {
            logger.error("Error Creating socket", ex);
        }
    }

    @Override
    public void close()
    {
        this.ioTcpReadRunnable.close();
    }

    @Override
    public synchronized boolean isConnected()
    {
        return this.connected;
    }

    @Override
    public int getConnectionId()
    {
        return this.connectionId;
    }

    @Override
    public synchronized void setConnectionId(int id)
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
        Thread tcpreadThread = new Thread(this.ioTcpReadRunnable, "IoReadThread");
        tcpreadThread.setDaemon(true);
        tcpreadThread.start();

        logger.trace("Waiting for ID from server");
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
        logger.trace("Got Id from server {}", this.getConnectionId());

        while (this.isConnected())
        {
            try
            {
                logger.trace("({}) About to block to Take message off queue", this.getConnectionId());
                MessageType message = this.receiveQueue.take();
                logger.trace("({}) Message taken to be processed ({})", this.getConnectionId(), message);
                this.client.handleMessageReceived(message);
            }
            catch (InterruptedException ex)
            {
                logger.error("Error processing Receive Message queue", ex);
            }
        }
        logger.info("Client Connection thread ended.");
        this.close();
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

    @Override
    public void addConnectionEventListener(ConnectionEvent listener)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeConnectionEventListener(ConnectionEvent listener)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
