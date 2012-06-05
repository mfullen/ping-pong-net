package ping.pong.net.server.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ping.pong.net.connection.Connection;
import ping.pong.net.connection.ConnectionConfiguration;
import ping.pong.net.connection.Envelope;
import ping.pong.net.server.ServerExceptionHandler;

/**
 *
 * @author mfullen
 */
final class IoServerConnectionImpl<MessageType> implements
        Connection<MessageType>
{
    public static final Logger logger = LoggerFactory.getLogger(IoServerConnectionImpl.class);
    protected DatagramSocket udpSocket = null;
    protected Socket tcpSocket = null;
    protected ConnectionConfiguration config = null;
    protected boolean connected = false;
    protected int connectionId = -1;
    protected IoServerImpl<MessageType> server = null;
    private final Object lock = new Object();
    private final Object tcpWriteLock = new Object();
    protected ConcurrentLinkedQueue<MessageType> readQueue = new ConcurrentLinkedQueue<MessageType>();
    protected ConcurrentLinkedQueue<MessageType> tcpWriteQueue = new ConcurrentLinkedQueue<MessageType>();
    protected ConcurrentLinkedQueue<MessageType> udpWriteQueue = new ConcurrentLinkedQueue<MessageType>();
    private ExecutorService executorService = Executors.newFixedThreadPool(4);
    /**
     * The input stream that receives data from the other side of the connection
     */
    protected ObjectInputStream inputstream;
    /**
     * The output stream that writes data to the other side of the connection
     */
    protected ObjectOutputStream outputstream;
    /**
     *
     */
    private Runnable tcpReceive = new Runnable()
    {
        boolean listening = true;

        @Override
        public void run()
        {
            try
            {
                inputstream = new ObjectInputStream(tcpSocket.getInputStream());
            }
            catch (IOException ex)
            {
                logger.error("{} Input stream initialization error", getConnectionName(), ex);
                this.listening = false;
            }

            while (listening)
            {
                if (tcpSocket.isClosed() || !tcpSocket.isConnected())
                {
                    logger.trace("{} TCP Socket is closed or not Connected. TCP Receive Thread shutting down", getConnectionName());
                    this.listening = false;
                }
                else
                {
                    //todo supply an interface to swap in different read implementations
                    Object readObject = null;
                    try
                    {
                        //blocks here
                        logger.trace("{} About to block for read Object", getConnectionName());
                        readObject = inputstream.readObject();
                        logger.trace("{} Read Object from Stream: {} ", getConnectionName(), readObject);

                        if (readObject != null)
                        {
                            enqueueMessage((MessageType) readObject);
                        }
                        else
                        {
                            logger.error("Read Object is null");
                        }
                    }
                    catch (EOFException eofException)
                    {
                        logger.trace("{} End of Stream. Socket was closed: ", getConnectionName());
                        this.listening = false;
                    }
                    catch (IOException ex)
                    {
                        logger.error("{} TCP Receive Class IO Exception: ", getConnectionName(), ex);
                        this.listening = false;
                    }
                    catch (ClassNotFoundException ex)
                    {
                        logger.error("{} TCP Receive Class NotFound: ", getConnectionName(), ex);
                        this.listening = false;
                    }
                }
            }

            //close connection
            logger.info("{} Receive thread calling close", getConnectionName());
            close();
        }
    };
    private final Runnable tcpWrite = new Runnable()
    {
        boolean listening = true;

        public boolean isListening()
        {
            return listening;
        }

        @Override
        public void run()
        {
            try
            {
                outputstream = new ObjectOutputStream(tcpSocket.getOutputStream());
                outputstream.flush();
            }
            catch (IOException ex)
            {
                logger.error("{} Output stream initialization error", getConnectionName(), ex);
                this.listening = false;
            }

            try
            {
                synchronized (tcpWrite)
                {
                    outputstream.writeInt(getConnectionId());
                    outputstream.flush();
                    logger.trace("{} Sending Client/Connection Id {}", getConnectionName(), getConnectionId());
                }
            }
            catch (IOException ex)
            {
                logger.error("{} Error Sending Client/Connection Id {}", getConnectionName(), getConnectionId());
                this.listening = false;
            }

            listeningLabel:
            while (this.listening)
            {
                if (tcpSocket.isClosed() || !tcpSocket.isConnected())
                {
                    logger.trace("{} TCP Socket is closed or not Connected. TCP Write Thread shutting down", getConnectionName());
                    this.listening = false;
                    break listeningLabel;
                }
                while (!tcpWriteQueue.isEmpty())
                {
                    //send through the socket
                    MessageType message = tcpWriteQueue.poll();

                    try
                    {
                        synchronized (tcpWrite)
                        {
                            outputstream.writeObject(message);
                            logger.trace("{} Output wrote object: {} ", getConnectionName(), message);
                        }
                    }
                    catch (Exception e)
                    {
                        logger.error("{} Output Stream WriteObject error: {} ", getConnectionName(), e);
                    }
                    finally
                    {
                        try
                        {
                            outputstream.flush();
                        }
                        catch (IOException ex)
                        {
                            logger.error("{} Output Stream Flush error: {} ", getConnectionName());
                            this.listening = false;
                        }
                    }
                }
                synchronized (tcpWriteLock)
                {
                    try
                    {
                        logger.trace("{} Tcp Write going into waiting", getConnectionName());
                        tcpWriteLock.wait();
                    }
                    catch (InterruptedException ex)
                    {
                        logger.error("{} WriteLock failed: ", getConnectionName(), ex);
                        this.listening = false;
                    }
                }
            }
            logger.info("{} Write thread calling close", getConnectionName());
            close();
        }
    };
    private Runnable udpReceive = new Runnable()
    {
        @Override
        public void run()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };
    private Runnable udpWrite = new Runnable()
    {
        @Override
        public void run()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };

    private IoServerConnectionImpl()
    {
    }

    public IoServerConnectionImpl(IoServerImpl<MessageType> server, ConnectionConfiguration config, Socket tcpSocket, DatagramSocket udpSocket)
    {
        this.server = server;
        this.config = config;
        this.tcpSocket = tcpSocket;
        this.udpSocket = udpSocket;
    }

    public void enqueueMessage(MessageType message)
    {
        boolean added = this.readQueue.add(message);
        synchronized (this.lock)
        {
            this.lock.notifyAll();
        }
        logger.trace("{} Message enqueued {}", this.getConnectionName(), added ? "Successfully." : "Failed.");
    }

    private void closeTcpSocket()
    {
        try
        {
            if (this.outputstream != null)
            {
                this.outputstream.flush();
                this.outputstream.close();
            }
        }
        catch (IOException ex)
        {
            logger.error("{} TCP Output stream failed to close", this.getConnectionName(), ex);
        }

        try
        {
            if (this.inputstream != null)
            {
                this.inputstream.close();
            }
        }
        catch (IOException ex)
        {
            logger.error("{} TCP input stream failed to close", this.getConnectionName(), ex);
        }

        try
        {
            if (!tcpSocket.isClosed())
            {
                logger.trace("{} TCP Socket Closing...", this.getConnectionName());
                tcpSocket.close();
                logger.trace("{} TCP Socket Closed", this.getConnectionName());
            }
            logger.trace("{} TCP Socket Already Closed", this.getConnectionName());
        }
        catch (IOException ex)
        {
            logger.error("{} TCP Socket closed error: ", getConnectionName());
            ServerExceptionHandler.handleException(ex, logger);
        }
    }

    private void closeUdpSocket()
    {
    }

    @Override
    public void close()
    {
        this.closeTcpSocket();
        this.tcpReceive = null;

        this.closeUdpSocket();
        // this.tcpWrite = null;

        this.connected = false;

        //remove the connection from the server
        if (this.server.hasConnections())
        {
            Connection connection = this.server.getConnection(this.connectionId);
            if (connection != null)
            {
                this.server.removeConnection(this);
            }
        }

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

    @Override
    public void run()
    {
        this.connected = true;

        //start read and write threads
        this.executorService.execute(tcpWrite);
        this.executorService.execute(tcpReceive);


        while (connected)
        {
            while (!this.readQueue.isEmpty())
            {
                logger.info("{} Processing Queue", this.getConnectionName());
                //todo fix this to actually process the messages to the listeners
                MessageType poll = this.readQueue.poll();
                logger.info("{} {}", this.getConnectionName(), poll);
            }

            synchronized (this.lock)
            {
                try
                {
                    logger.trace("{} Finished Processing going into waiting...", this.getConnectionName());
                    this.lock.wait();
                }
                catch (InterruptedException ex)
                {
                    logger.error("{} Lock failed: ", this.getConnectionName(), ex);
                }
            }
        }
        //Connection is done, try to properly close and cleanup
        logger.info("{} Main thread calling close", getConnectionName());
        this.close();
    }

    @Override
    public void sendMessage(Envelope<MessageType> message)
    {
        MessageType msg = message.getMessage();

        if (message.isReliable())
        {
            sendTcpMessage(msg);
        }
        else
        {
            sendUdpMessage(msg);
        }
    }

    private void sendTcpMessage(MessageType message)
    {
        synchronized (tcpWriteLock)
        {
            boolean added = tcpWriteQueue.add(message);
            logger.trace("{} Message {} to the write queue", this.getConnectionName(), added ? "successfully added" : "failed to add");
            tcpWriteLock.notifyAll();
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
}
