package ping.pong.net.client.io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import ping.pong.net.connection.MessageProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IoTcpReadRunnable is a thread for reading messages from an input stream.
 * The thread blocks when attempting to read data. When data is read it is passed
 * to the MessageProcessor
 * @author mfullen
 */
public final class IoTcpReadRunnable<MessageType> implements Runnable
{
    /**
     * Logger for IoTcpReadRunnable
     */
    public static final Logger logger = LoggerFactory.getLogger(IoTcpReadRunnable.class);
    /**
     * MessageProcessor to process the messages read
     */
    protected MessageProcessor<MessageType> messageProcessor = null;
    /**
     * The socket the thread is reading from
     */
    protected Socket tcpSocket = null;
    /**
     * The inputstream in which the thread is reading from
     */
    protected ObjectInputStream inputStream = null;
    /**
     * Flag for whether this thread is running
     */
    protected boolean running = false;

    /**
     * Constructor for the Read Thread
     * @param messageProcessor The processor which handles the incoming message
     * @param tcpSocket The socket in which the message is being read from
     */
    public IoTcpReadRunnable(MessageProcessor<MessageType> messageProcessor, Socket tcpSocket)
    {
        this.messageProcessor = messageProcessor;
        this.tcpSocket = tcpSocket;
        this.init();
    }

    /**
     * Initialize the InputStream from the socket
     */
    protected void init()
    {
        try
        {
            this.inputStream = new ObjectInputStream(tcpSocket.getInputStream());
        }
        catch (IOException ex)
        {
            logger.error("Tcp Socket Init Error Error ", ex);
        }
    }

    /**
     * Is this thread still running/running?
     * @return
     */
    public boolean isRunning()
    {
        return this.running;
    }

    /**
     * Block while attempting to read Object from Stream
     * @return return the object from the stream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    protected synchronized Object readObject() throws IOException,
                                                      ClassNotFoundException
    {
        Object readObject = null;
        //blocks here
        logger.trace("{} About to block for read Object");

        readObject = this.inputStream.readObject();
        logger.trace("{} Read Object from Stream: {} ", "", readObject);

        return readObject;
    }

    /**
     * Closes the thread but properly shuts down the socket by closing the inputstream
     * and the tcpsocket. Calls Socket.close
     */
    public void close()
    {
        try
        {
            this.running = false;
            this.inputStream.close();
            synchronized (tcpSocket)
            {
                this.tcpSocket.close();
            }
        }
        catch (IOException ex)
        {
            logger.error("Error closing Socket", ex);
        }
    }

    @Override
    public void run()
    {
        this.running = true;

        while (this.running)
        {
            try
            {
                Object readObject = readObject();
                if (readObject != null)
                {
                    this.messageProcessor.enqueueReceivedMessage((MessageType) readObject);
                }
                else
                {
                    logger.error("Read Object is null");
                }
            }
            catch (IOException ex)
            {
                logger.error("TcpRecieveThread IOException", ex);
            }
            catch (ClassNotFoundException ex)
            {
                logger.error("TcpRecieveThread ClassnotFound", ex);
            }
        }
        this.close();
    }
}
