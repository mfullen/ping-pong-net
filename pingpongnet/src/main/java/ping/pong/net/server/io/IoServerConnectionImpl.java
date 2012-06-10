package ping.pong.net.server.io;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import ping.pong.net.client.io.IoTcpReadRunnable;
import ping.pong.net.client.io.IoTcpWriteRunnable;
import ping.pong.net.connection.Connection;
import ping.pong.net.connection.ConnectionConfiguration;
import ping.pong.net.connection.ConnectionEvent;
import ping.pong.net.connection.Envelope;
import ping.pong.net.connection.MessageProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ping.pong.net.connection.RunnableEventListener;

/**
 *
 * @author mfullen
 */
final class IoServerConnectionImpl<MessageType> implements
        Connection<MessageType>,
        MessageProcessor<MessageType>
{
    public static final Logger logger = LoggerFactory.getLogger(IoServerConnectionImpl.class);
    protected DatagramSocket udpSocket = null;
    protected Socket tcpSocket = null;
    protected ConnectionConfiguration config = null;
    protected boolean connected = false;
    protected int connectionId = -1;
    protected List<ConnectionEvent> connectionEventListeners = new ArrayList<ConnectionEvent>();
    protected IoTcpReadRunnable<MessageType> ioTcpReadRunnable = null;
    protected IoTcpWriteRunnable<MessageType> ioTcpWriteRunnable = null;
    /**
     * This connections queue of received messages to process
     */
    private BlockingQueue<MessageType> receiveQueue = new LinkedBlockingQueue<MessageType>();
    private ExecutorService executorService = Executors.newFixedThreadPool(4);
    /**
     * The input stream that receives data from the other side of the connection
     */
    protected ObjectInputStream inputstream;
    /**
     * The output stream that writes data to the other side of the connection
     */
    protected ObjectOutputStream outputstream;

    private IoServerConnectionImpl()
    {
    }

    public IoServerConnectionImpl(ConnectionConfiguration config, Socket tcpSocket, DatagramSocket udpSocket)
    {
        this.config = config;
        this.tcpSocket = tcpSocket;
        this.udpSocket = udpSocket;
        boolean initTcp = this.initTcp();
        logger.trace("Tcp Init {}", initTcp);
    }

    @Override
    public void close()
    {
        if (this.ioTcpReadRunnable == null || this.ioTcpWriteRunnable == null)
        {
            logger.warn("Connection cannot be closed, it never started");
            return;
        }
        if (this.ioTcpWriteRunnable.isRunning())
        {
            this.ioTcpWriteRunnable.close();
        }
        if (this.ioTcpReadRunnable.isRunning())
        {
            this.ioTcpReadRunnable.close();
        }

        this.connected = false;

        for (ConnectionEvent connectionEvent : this.connectionEventListeners)
        {
            connectionEvent.onSocketClosed();
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

    private String getConnectionName()
    {
        return "Connection (" + this.getConnectionId() + "):";
    }

    @Override
    public boolean isConnected()
    {
        return connected;
    }

    @Override
    public int getConnectionId()
    {
        return this.connectionId;
    }

    @Override
    public void setConnectionId(int connectionId)
    {
        this.connectionId = connectionId;
    }

    synchronized void fireOnSocketCreated()
    {
        for (ConnectionEvent connectionEvent : connectionEventListeners)
        {
            connectionEvent.onSocketCreated();
        }
    }

    synchronized void fireOnSocketMessageReceived(MessageType message)
    {
        for (ConnectionEvent<MessageType> connectionEvent : connectionEventListeners)
        {
            connectionEvent.onSocketReceivedMessage(message);
        }
    }

    /**
     * Method to initialize a TCP connection. Creates read and Write threads for TCP
     * @return true if the initiation is a success, false otherwise.
     */
    protected boolean initTcp()
    {
        boolean successful = false;

        RunnableEventListener runnableEventListener = new RunnableEventListenerImpl();
        this.ioTcpReadRunnable = new IoTcpReadRunnable<MessageType>(this, runnableEventListener, tcpSocket);
        this.ioTcpWriteRunnable = new IoTcpWriteRunnable<MessageType>(runnableEventListener, tcpSocket);

        successful = true;

        return successful;
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
                this.fireOnSocketMessageReceived(message);

            }
            catch (InterruptedException ex)
            {
                logger.error("Error processing Receive Message queue", ex);
            }
        }

        //Connection is done, try to properly close and cleanup
        logger.info("{} Main thread calling close", getConnectionName());
        this.close();
    }

    @Override
    public void sendMessage(Envelope<MessageType> message)
    {
        this.enqueueMessageToWrite(message);
    }

    private void sendTcpMessage(MessageType message)
    {
        if (this.ioTcpWriteRunnable != null)
        {
            logger.trace("Enqueued {} TCP Message to write", message);
            boolean enqueueMessage = this.ioTcpWriteRunnable.enqueueMessage(message);
            logger.trace("Message Enqueued {}", enqueueMessage);
        }
    }

    private void sendUdpMessage(MessageType message)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String toString()
    {
        return "DefaultIoServerConnection{" + "connected=" + connected + ", connectionId=" + connectionId + '}';
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

    class RunnableEventListenerImpl implements RunnableEventListener
    {
        private RunnableEventListenerImpl()
        {
        }

        @Override
        public void onRunnableClosed()
        {
            IoServerConnectionImpl.this.close();
        }
    }
}
