package ping.pong.net.client.io;

import ping.pong.net.connection.messaging.MessageProcessor;
import ping.pong.net.connection.config.ConnectionConfiguration;
import ping.pong.net.connection.messaging.Envelope;
import ping.pong.net.connection.io.IoTcpReadRunnable;
import ping.pong.net.connection.io.IoTcpWriteRunnable;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ping.pong.net.connection.*;
import ping.pong.net.connection.messaging.ConnectionIdMessage;

/**
 * Connection Implementation for Io.
 * @author mfullen
 */
final class IoClientConnectionImpl<MessageType> implements
        Connection<MessageType>,
        MessageProcessor<MessageType>
{
    /**
     * The logger to use in the class
     */
    public static final Logger logger = LoggerFactory.getLogger(IoClientConnectionImpl.class);
    /**
     * The ConnectionConfiguration this connection is using
     */
    protected ConnectionConfiguration config = null;
    /**
     * The Client reference this connection is using
     */
    protected IoClientImpl<MessageType> client = null;
    /**
     * Flag for whether this connection is actually connected to a socket
     */
    protected boolean connected = false;
    /**
     * The id of the connection
     */
    protected int connectionId = -1;
    /**
     * A list of Connection Events
     */
    protected List<ConnectionEvent> connectionEventListeners = new ArrayList<ConnectionEvent>();
    /**
     * This connection's TcpReadThread
     */
    private IoTcpReadRunnable<MessageType> ioTcpReadRunnable = null;
    /**
     * This connection's TcpWriteThread
     */
    private IoTcpWriteRunnable<MessageType> ioTcpWriteRunnable = null;
    /**
     * This connections queue of received messages to process
     */
    private BlockingQueue<MessageType> receiveQueue = new LinkedBlockingQueue<MessageType>();

    /**
     * Constructor for the Client Implementation
     * @param client The client the connection is created from
     * @param config the configuration to use in initiating the client
     */
    public IoClientConnectionImpl(IoClientImpl<MessageType> client, ConnectionConfiguration config)
    {
        this.config = config;
        this.client = client;
    }

    /**
     * Method to initialize a TCP connection. Creates read and Write threads for TCP
     * @return true if the initiation is a success, false otherwise.
     */
    protected boolean initTcp()
    {
        boolean successful = false;
        try
        {
            SocketFactory factory = config.isSsl() ? SSLSocketFactory.getDefault() : SocketFactory.getDefault();
            Socket tcpSocket = factory.createSocket(config.getIpAddress(), config.getPort());
            RunnableEventListener runnableEventListener = new RunnableEventListenerImpl();
            this.ioTcpReadRunnable = new IoTcpReadRunnable<MessageType>(this, runnableEventListener, tcpSocket);
            this.ioTcpWriteRunnable = new IoTcpWriteRunnable<MessageType>(runnableEventListener, tcpSocket);
            successful = true;
        }
        catch (IOException ex)
        {
            logger.error("Error Creating socket", ex);
        }
        return successful;
    }

    protected boolean isAnyRunning()
    {
        return this.ioTcpReadRunnable.isRunning() || this.ioTcpWriteRunnable.isRunning();
    }

    @Override
    public void close()
    {
        if (this.ioTcpReadRunnable == null || this.ioTcpWriteRunnable == null)
        {
            logger.warn("Connection cannot be closed, it never started");
            return;
        }

        if (this.isAnyRunning())
        {
            this.client.onClientDisconnected(new DisconnectInfo()
            {
                @Override
                public String getReason()
                {
                    return "The close method was called";
                }

                @Override
                public DisconnectState getDisconnectState()
                {
                    return DisconnectState.ERROR;
                }
            });
        }

        if (this.ioTcpWriteRunnable.isRunning())
        {
            this.ioTcpWriteRunnable.close();
        }
        if (this.ioTcpReadRunnable.isRunning())
        {
            this.ioTcpReadRunnable.close();
        }
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

    /**
     * This method should enqueue a Message to the UDP write thread
     * @param msg the message to enqueue for sending
     */
    protected void sendUdpMessage(MessageType msg)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * This method enqueues a message on the TcpWrite thread for sending
     * @param msg the message to enqueue for sending
     */
    protected void sendTcpMessage(MessageType msg)
    {
        if (this.ioTcpWriteRunnable != null)
        {
            logger.trace("Enqueued {} TCP Message to write", msg);
            boolean enqueueMessage = this.ioTcpWriteRunnable.enqueueMessage(msg);
            logger.trace("Message Enqueued {}", enqueueMessage);
        }
        else
        {
            logger.trace("IoTcpWrite is null");
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

                    //fire client connected event
                    this.client.onClientConnected();
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

    class RunnableEventListenerImpl implements RunnableEventListener
    {
        private RunnableEventListenerImpl()
        {
        }

        @Override
        public void onRunnableClosed()
        {
            IoClientConnectionImpl.this.close();
        }
    }
}
