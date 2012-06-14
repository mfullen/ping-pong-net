package ping.pong.net.connection.io;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ping.pong.net.connection.RunnableEventListener;

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
    protected OutputStream outputStream = null;
    /**
     * Flag for whether this thread is running
     */
    protected boolean running = false;
    /**
     * The queue of messages to write from
     */
    protected BlockingQueue<MessageType> writeQueue = new LinkedBlockingQueue<MessageType>();
    /**
     * Notifies the listener when this runnable is closed
     */
    protected RunnableEventListener runnableEventListener = null;
    private DataWriter dataWriter = null;

    public IoTcpWriteRunnable(RunnableEventListener runnableEventListener, DataWriter dataWriter, Socket tcpSocket)
    {
        this.tcpSocket = tcpSocket;
        this.runnableEventListener = runnableEventListener;
        this.dataWriter = dataWriter;
    }

    /**
     * Initialize the Output Stream from the socket
     */
    protected void init()
    {
        this.outputStream = dataWriter.init(this.tcpSocket);
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
        finally
        {
            if (this.runnableEventListener != null)
            {
                this.runnableEventListener.onRunnableClosed();
                this.runnableEventListener = null;
            }
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
                this.dataWriter.writeData(message);
            }
            catch (InterruptedException ex)
            {
                logger.error("Error with write thread", ex);
            }
        }
    }
}
