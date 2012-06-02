package ping.pong.net.server;

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

/**
 *
 * @author mfullen
 */
public final class DefaultIoServerConnection<MessageType> implements
        Connection<MessageType>
{
    public static final Logger logger = LoggerFactory.getLogger(DefaultIoServerConnection.class);
    protected DatagramSocket udpSocket = null;
    protected Socket tcpSocket = null;
    protected ConnectionConfiguration config = null;
    protected boolean connected = false;
    protected int connectionId = -1;
    protected IoServerImpl<MessageType> server = null;
    private final Object lock = new Object();
    protected ConcurrentLinkedQueue<MessageType> queue = new ConcurrentLinkedQueue<MessageType>();
    private ExecutorService executorService = Executors.newFixedThreadPool(4);
    /**
     * The input stream that receives data from the other side of the connection
     */
    protected ObjectInputStream inputstream;
    /**
     * The output stream that writes data to the other side of the connection
     */
    protected ObjectOutputStream outputstream;
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
                logger.error("{} Input stream error fool", getConnectionName(), ex);
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
                    catch (IOException ex)
                    {

                        try
                        {
                            tcpSocket.close();
                        }
                        catch (IOException ex1)
                        {
                        }
                        finally
                        {
                            logger.error("{} TCP Receive: ", getConnectionName());
                            ServerExceptionHandler.handleException(ex);
                        }
                    }
                    catch (ClassNotFoundException ex)
                    {
                        logger.error("{} TCP Receive Class NotFound: ", getConnectionName(), ex);
                    }
                }
            }
        }
    };
    private Runnable tcpWrite = new Runnable()
    {
        @Override
        public void run()
        {
            throw new UnsupportedOperationException("Not supported yet.");
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

    private DefaultIoServerConnection()
    {
    }

    public DefaultIoServerConnection(IoServerImpl<MessageType> server, ConnectionConfiguration config, Socket tcpSocket, DatagramSocket udpSocket)
    {
        this.server = server;
        this.config = config;
        this.tcpSocket = tcpSocket;
        this.udpSocket = udpSocket;
    }

    public void enqueueMessage(MessageType message)
    {
        boolean added = this.queue.add(message);
        synchronized (this.lock)
        {
            this.lock.notifyAll();
        }
        logger.trace("{} Message enqueued {}", this.getConnectionName(), added ? "Successfully." : "Failed.");
    }

    public synchronized ObjectOutputStream getOutputstream()
    {
        return outputstream;
    }

    public synchronized ObjectInputStream getInputstream()
    {
        return inputstream;
    }

    @Override
    public void close()
    {
        if (this.inputstream != null)
        {
            try
            {
                this.inputstream.close();
            }
            catch (IOException ex)
            {
                logger.error("{} TCP input stream failed to close", this.getConnectionName(), ex);
            }
        }

        //remove the connection from the server
        this.server.removeConnection(this);
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

        //send connection id
        //sendthis.getConnectionId();


        //start read and write threads
        this.executorService.execute(tcpReceive);


        while (connected)
        {
            while (!this.queue.isEmpty())
            {
                logger.info("{} Processing Queue", this.getConnectionName());
                //todo fix this to actually process the messages to the listeners
                MessageType poll = this.queue.poll();
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
        this.close();
    }

    @Override
    public Server<Envelope<MessageType>> getServer()
    {
        return this.server;
    }

    @Override
    public void sendMessage(Envelope<MessageType> message)
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

    private void sendTcpMessage(MessageType message)
    {
    }

    private void sendUdpMessage(MessageType message)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
