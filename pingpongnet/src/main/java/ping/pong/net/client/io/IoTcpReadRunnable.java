package ping.pong.net.client.io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ping.pong.net.connection.Connection;
import ping.pong.net.connection.MessageProcessor;

/**
 *
 * @author mfullen
 */
public final class IoTcpReadRunnable<MessageType> implements Runnable
{
    public static final Logger logger = LoggerFactory.getLogger(IoTcpReadRunnable.class);
    private Connection connection = null;
    protected MessageProcessor<MessageType> messageProcessor = null;
    protected Socket tcpSocket = null;
    protected ObjectOutputStream outputStream = null;
    protected ObjectInputStream inputStream = null;
    protected boolean connected = false;

    public IoTcpReadRunnable(MessageProcessor<MessageType> messageProcessor, Connection connection, Socket tcpSocket)
    {
        this.connection = connection;
        this.messageProcessor = messageProcessor;
        this.tcpSocket = tcpSocket;
        this.init();
    }

    protected void init()
    {
        try
        {
            this.outputStream = new ObjectOutputStream(tcpSocket.getOutputStream());
            this.outputStream.flush();

            this.inputStream = new ObjectInputStream(tcpSocket.getInputStream());
        }
        catch (IOException ex)
        {
            logger.error("Tcp Socket Init Error Error ", ex);
        }

    }

    protected Object readObject() throws IOException, ClassNotFoundException
    {
        Object readObject = null;
        //blocks here
        logger.trace("{} About to block for read Object");

        readObject = this.inputStream.readObject();
        logger.trace("{} Read Object from Stream: {} ", "", readObject);

        return readObject;
    }

    public void close()
    {
        try
        {
            //todo
            this.connected = false;
            this.outputStream.flush();
            this.outputStream.close();
            this.inputStream.close();
            synchronized (tcpSocket)
            {
                this.tcpSocket.close();
            }
        }
        catch (IOException ex)
        {
            logger.error("Error closing Socket");
        }
    }

    @Override
    public void run()
    {
        try
        {
            //find better way to receive id
            int id = this.inputStream.readInt();
            synchronized (this)
            {
                this.connection.setConnectionId(id);
            }
            this.connected = true;
            logger.info("TcpRecieveThread Id set {}", id);
        }
        catch (IOException ex)
        {
            logger.error("TcpRecieveThread Error reading Id", ex);
        }

        while (this.connected)
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
