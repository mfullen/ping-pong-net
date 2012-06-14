package ping.pong.net.connection.io;

import java.net.DatagramSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ping.pong.net.connection.Connection;
import ping.pong.net.connection.ConnectionEvent;
import ping.pong.net.connection.RunnableEventListener;
import ping.pong.net.connection.config.ConnectionConfiguration;
import ping.pong.net.connection.messaging.DisconnectMessage;
import ping.pong.net.connection.messaging.Envelope;
import ping.pong.net.connection.messaging.MessageProcessor;

/**
 * AbstractIoConnection represents a basic Io Connection
 *
 * @author mfullen
 */
public abstract class AbstractIoConnection<MessageType> implements
        Connection<MessageType>,
        MessageProcessor<MessageType>
{
    /**
     * The logger to use in the class
     */
    protected static final Logger logger = LoggerFactory.getLogger(AbstractIoConnection.class);
    /**
     * The ConnectionConfiguration this connection is using
     */
    protected ConnectionConfiguration config = null;
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
    protected IoTcpReadRunnable<MessageType> ioTcpReadRunnable = null;
    /**
     * This connection's TcpWriteThread
     */
    protected IoTcpWriteRunnable<MessageType> ioTcpWriteRunnable = null;
    /**
     * This connections queue of received messages to process
     */
    protected BlockingQueue<MessageType> receiveQueue = new LinkedBlockingQueue<MessageType>();
    /**
     * The ExecutorService controls the thread pool for read and write
     */
    protected ExecutorService executorService = Executors.newFixedThreadPool(4);
    /**
     * The Udp Socket used for this connection
     */
    protected DatagramSocket udpSocket = null;
    /**
     * The Tcp Socket used for this connection
     */
    protected Socket tcpSocket = null;
    protected boolean usingCustomSerialization = true;
    /**
     * The Runnable Event Listener used for this connection
     */
    protected RunnableEventListener runnableEventListener = new RunnableEventListenerImpl();
    /**
     * Flag for whether the connection has been closed
     */
    protected boolean closed = false;
    /**
     *  Data reader.
     */
    private DataReader dataReader = null;
    /**
     *  Data Writer
     */
    private DataWriter dataWriter = null;

    public AbstractIoConnection(ConnectionConfiguration config, DataReader dataReader, DataWriter dataWriter, Socket tcpSocket, DatagramSocket udpSocket)
    {
        this.config = config;
        this.tcpSocket = tcpSocket;
        this.udpSocket = udpSocket;
        this.dataReader = dataReader;
        this.dataWriter = dataWriter;
        this.initTcp();
    }

    /**
     * Method to initialize a TCP connection. Creates read and Write threads for
     * TCP
     *
     * @return true if the initiation is a success, false otherwise.
     */
    protected final boolean initTcp()
    {
        boolean successful = false;

        //if the custom reader/writer are null, create default
        if (this.dataReader == null)
        {
            dataReader = new ReadObjectDataReader();
            this.usingCustomSerialization = false;
        }
        if (this.dataWriter == null)
        {
            dataWriter = new WriteObjectDataWriter();
            this.usingCustomSerialization = false;
        }

        this.ioTcpReadRunnable = new IoTcpReadRunnable<MessageType>(this, runnableEventListener, dataReader, tcpSocket);
        this.ioTcpWriteRunnable = new IoTcpWriteRunnable<MessageType>(runnableEventListener, dataWriter, tcpSocket);

        successful = true;

        return successful;
    }

    @Override
    public boolean isUsingCustomSerialization()
    {
        return usingCustomSerialization;
    }

    protected abstract void processMessage(MessageType message);

    /**
     * Fire the on socket created event for all listeners
     */
    protected synchronized void fireOnSocketCreated()
    {
        for (ConnectionEvent connectionEvent : connectionEventListeners)
        {
            connectionEvent.onSocketCreated();
        }
    }

    /**
     * Fire the on socketMessage Received event for all listeners
     * @param message
     */
    protected synchronized void fireOnSocketMessageReceived(MessageType message)
    {
        for (ConnectionEvent<MessageType> connectionEvent : connectionEventListeners)
        {
            connectionEvent.onSocketReceivedMessage(message);
        }
    }

    @Override
    public void run()
    {
        this.executorService.execute(ioTcpWriteRunnable);
        this.executorService.execute(ioTcpReadRunnable);
        this.connected = true;

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

    protected String getConnectionName()
    {
        return "Connection (" + this.getConnectionId() + "):";
    }

    protected boolean isAnyRunning()
    {
        return this.ioTcpReadRunnable.isRunning() || this.ioTcpWriteRunnable.isRunning();
    }

    @Override
    public synchronized void close()
    {
        if (this.ioTcpReadRunnable == null || this.ioTcpWriteRunnable == null)
        {
            logger.warn("Connection cannot be closed, it never started");
            return;
        }

        this.connected = false;
        if (this.isAnyRunning())
        {
            for (ConnectionEvent connectionEvent : this.connectionEventListeners)
            {
                connectionEvent.onSocketClosed();
            }

            this.receiveQueue.add((MessageType) new DisconnectMessage());
        }

        if (this.ioTcpWriteRunnable.isRunning())
        {
            this.ioTcpWriteRunnable.close();
        }
        if (this.ioTcpReadRunnable.isRunning())
        {
            this.ioTcpReadRunnable.close();
        }


        if (!isAnyRunning() && !isConnected() && !this.closed)
        {
            logger.info("Connection ({}) has been closed", this.getConnectionId());
            this.closed = true;
        }
    }

    @Override
    public synchronized boolean isConnected()
    {
        return this.connected;
    }

    @Override
    public synchronized int getConnectionId()
    {
        return this.connectionId;
    }

    @Override
    public synchronized void setConnectionId(int connectionId)
    {
        this.connectionId = connectionId;
    }

    @Override
    public void sendMessage(Envelope<MessageType> message)
    {
        this.enqueueMessageToWrite(message);
        logger.debug("Preparing to send Message {}", message);
    }

    /**
     * This method should enqueue a Message to the UDP write thread
     *
     * @param msg the message to enqueue for sending
     */
    protected void sendUdpMessage(MessageType msg)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * This method enqueues a message on the TcpWrite thread for sending
     *
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
            AbstractIoConnection.this.close();
        }
    }
}
