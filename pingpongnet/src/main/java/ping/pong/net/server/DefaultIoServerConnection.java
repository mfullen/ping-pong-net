package ping.pong.net.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ping.pong.net.connection.Connection;
import ping.pong.net.connection.ConnectionConfiguration;
import ping.pong.net.connection.Envelope;

/**
 *
 * @author mfullen
 */
public class DefaultIoServerConnection<Message> implements
        Connection
{
    public static final Logger logger = LoggerFactory.getLogger(DefaultIoServerConnection.class);
    protected DatagramSocket udpSocket = null;
    protected Socket tcpSocket = null;
    protected ConnectionConfiguration config = null;
    protected boolean connected = false;
    private final Object lock = new Object();
    protected ConcurrentLinkedQueue<Message> queue = new ConcurrentLinkedQueue<Message>();
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
        @Override
        public void run()
        {
            try
            {
                inputstream = new ObjectInputStream(tcpSocket.getInputStream());
            }
            catch (IOException ex)
            {
                logger.error("Input stream error fool", ex);
            }
            while (true)
            {
                if (tcpSocket.isClosed() || !tcpSocket.isConnected())
                {
                    logger.trace("TCP Socket is closed or not Connected");
                }
                else
                {
                    //todo supply an interface to swap in different read implementations
                    Object readObject = null;
                    try
                    {
                        //blocks here
                        readObject = inputstream.readObject();
                        logger.trace("Read Object from Stream: " + readObject);

                        if (readObject != null)
                        {
                            enqueueMessage((Message) readObject);
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
                            logger.error("TCP Receive: ");
                            ServerExceptionHandler.handleException(ex);
                        }
                    }
                    catch (ClassNotFoundException ex)
                    {
                        logger.error("TCP Receive Class NotFound: ", ex);
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

    public DefaultIoServerConnection(ConnectionConfiguration config, Socket tcpSocket, DatagramSocket udpSocket)
    {
        this.config = config;
        this.tcpSocket = tcpSocket;
        this.udpSocket = udpSocket;
    }

    public void enqueueMessage(Message message)
    {
        boolean added = this.queue.add(message);
        synchronized (this.lock)
        {
            this.lock.notifyAll();
        }
        logger.trace("Message enqueued {}", added ? "Successfully." : "Failed.");
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
                logger.error("TCP input stream failed to close", ex);
            }
        }
    }

    @Override
    public boolean isConnected()
    {
        return connected;
    }

    @Override
    public int getConnectionID()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Server<Message> getServer()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void run()
    {
        this.connected = true;

        //start read and write threads
        this.executorService.execute(tcpReceive);


        while (connected)
        {
            while (!this.queue.isEmpty())
            {
                logger.info("Processing Queue");
                //todo fix this to actually process the messages to the listeners
                Message poll = this.queue.poll();
                logger.info(poll + "");
            }

            synchronized (this.lock)
            {
                try
                {
                    this.lock.wait();
                }
                catch (InterruptedException ex)
                {
                    logger.error("Lock failed: " + ex);
                }
            }
        }
    }

    @Override
    public void sendMessage(Envelope message)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
