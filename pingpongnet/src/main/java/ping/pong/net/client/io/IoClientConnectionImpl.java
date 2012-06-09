package ping.pong.net.client.io;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ping.pong.net.connection.*;
import ping.pong.net.connection.messages.ConnectionIdMessage;

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
    protected List<ConnectionEvent> connectionEventListeners = new ArrayList<ConnectionEvent>();
    private IoTcpReadRunnable<MessageType> ioTcpReadRunnable = null;
    private IoTcpWriteRunnable<MessageType> ioTcpWriteRunnable = null;
    private LinkedBlockingQueue<MessageType> receiveQueue = new LinkedBlockingQueue<MessageType>();

    public IoClientConnectionImpl(IoClientImpl<MessageType> client, ConnectionConfiguration config)
    {
        this.config = config;
        this.client = client;
    }

    protected boolean initTcp()
    {
        boolean successful = false;
        try
        {
            SocketFactory factory = config.isSsl() ? SSLSocketFactory.getDefault() : SocketFactory.getDefault();
            Socket tcpSocket = factory.createSocket(config.getIpAddress(), config.getPort());
            this.ioTcpReadRunnable = new IoTcpReadRunnable<MessageType>(this, tcpSocket);
            this.ioTcpWriteRunnable = new IoTcpWriteRunnable<MessageType>(tcpSocket);
            successful = true;
        }
        catch (IOException ex)
        {
            logger.error("Error Creating socket", ex);
        }
        return successful;
    }

    @Override
    public void close()
    {
        if (this.ioTcpReadRunnable == null || this.ioTcpWriteRunnable == null)
        {
            logger.warn("Connection cannot be closed, it never started");
            return;
        }
        this.ioTcpReadRunnable.close();
        this.ioTcpWriteRunnable.close();
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
    }

    @Override
    public void sendMessage(Envelope<MessageType> message)
    {
        this.enqueueMessageToWrite(message);
    }

    protected void sendUdpMessage(MessageType msg)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    protected void sendTcpMessage(MessageType msg)
    {
        if (this.ioTcpWriteRunnable != null && this.ioTcpWriteRunnable.isRunning())
        {
            logger.trace("Enqueued {} TCP Message to write", msg);
            boolean enqueueMessage = this.ioTcpWriteRunnable.enqueueMessage(msg);
            logger.trace("Message Enqueued {}", enqueueMessage);
        }
    }

    @Override
    public void run()
    {
        boolean initTcp = this.initTcp();
        if (initTcp)
        {
            Thread tcpreadThread = new Thread(this.ioTcpReadRunnable, "IoTcpReadThread");
            tcpreadThread.setDaemon(true);
            tcpreadThread.start();
            
            Thread tcpWriteThread = new Thread(this.ioTcpWriteRunnable, "IoTcpWriteThread");
            tcpWriteThread.setDaemon(true);
            tcpWriteThread.start();

            this.connected = true;
        }

        while (this.isConnected())
        {
            try
            {
                logger.trace("({}) About to block to Take message off queue", this.getConnectionId());
                MessageType message = this.receiveQueue.take();

                if (message instanceof ConnectionIdMessage.ResponseMessage)
                {
                    int id = ((ConnectionIdMessage.ResponseMessage) message).getId();
                    this.setConnectionId(id);
                    logger.trace("Got Id from server {}", this.getConnectionId());
                }
                else
                {
                    logger.trace("({}) Message taken to be processed ({})", this.getConnectionId(), message);
                    this.client.handleMessageReceived(message);
                }
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
        boolean add = this.receiveQueue.add(message);
        logger.trace("Enqueued message {}", add);
    }

    @Override
    public void enqueueMessageToWrite(Envelope<MessageType> message)
    {
        if (message.isReliable())
        {
            sendTcpMessage(message.getMessage());
        }
        else
        {
            sendUdpMessage(message.getMessage());
        }
    }

    @Override
    public void addConnectionEventListener(ConnectionEvent listener)
    {
        this.connectionEventListeners.add(listener);
    }

    @Override
    public void removeConnectionEventListener(ConnectionEvent listener)
    {
        this.connectionEventListeners.remove(listener);
    }
}
