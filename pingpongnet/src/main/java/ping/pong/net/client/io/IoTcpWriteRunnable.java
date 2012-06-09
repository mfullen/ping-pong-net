package ping.pong.net.client.io;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IoTcpWriteRunnable Write a message to the output stream of a TCP socket
 * @author mfullen
 */
public final class IoTcpWriteRunnable<MessageType> implements Runnable
{
    /**
     * Logger for IoTcpReadRunnable
     */
    public static final Logger logger = LoggerFactory.getLogger(IoTcpWriteRunnable.class);
    /**
     * The socket the thread is reading from
     */
    protected Socket tcpSocket = null;
    /**
     * The inputstream in which the thread is reading from
     */
    protected ObjectOutputStream outputStream = null;
    /**
     * Flag for whether this thread is running
     */
    protected boolean running = false;
    /**
     * The queue of messages to write from
     */
    protected BlockingQueue<MessageType> writeQueue = new LinkedBlockingQueue<MessageType>();

    public IoTcpWriteRunnable(Socket tcpSocket)
    {
        this.tcpSocket = tcpSocket;
    }

    /**
     * Initialize the Output Stream from the socket
     */
    protected void init()
    {
        try
        {
            this.outputStream = new ObjectOutputStream(tcpSocket.getOutputStream());
            this.outputStream.flush();
        }
        catch (IOException ex)
        {
            logger.error("Tcp Socket Init Error Error ", ex);
        }
    }

    /**
     * Is this thread still running?
     * @return
     */
    public boolean isRunning()
    {
        return this.running;
    }

    /**
     * Closes the thread but properly shuts down the socket by closing the outputstream.
     * This method flushes the output stream before closing
     * and the tcpsocket. Calls Socket.close
     */
    public void close()
    {
        try
        {
            this.running = false;
            this.outputStream.flush();
            this.outputStream.close();
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

    /**
     * Enqueue a message to the write Queue
     * @param message the message to queue
     * @return true if successful, false if not
     */
    public boolean enqueueMessage(MessageType message)
    {
        return this.writeQueue.add(message);
    }

    /**
     * Write and Object to the output stream
     * @param message the message to write
     * @throws IOException
     */
    protected void writeOject(MessageType message) throws IOException
    {
        logger.trace("About to write Object to Stream {}", message);
        this.outputStream.writeObject(message);
        this.outputStream.flush();
        logger.trace("Wrote {} to and flushed Outputstream", message);
    }

    @Override
    public void run()
    {
        this.init();

        this.running = true;

        while (this.running)
        {
            try
            {
                MessageType message = this.writeQueue.take();
                this.writeOject(message);
            }
            catch (IOException ex)
            {
                logger.error("Error Writing Object", ex);
            }
            catch (InterruptedException ex)
            {
                logger.error("Error with write thread", ex);
            }
        }
    }
}
