package ping.pong.net.client.io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ping.pong.net.connection.Connection;
import ping.pong.net.connection.ConnectionConfiguration;
import ping.pong.net.connection.MessageProcessor;

/**
 *
 * @author mfullen
 */
public final class IoTcpReadRunnable<MessageType> implements Runnable
{
    public static final Logger logger = LoggerFactory.getLogger(IoTcpReadRunnable.class);
    private ConnectionConfiguration config = null;
    private Connection connection = null;
    protected MessageProcessor<MessageType> messageProcessor = null;
    protected Socket tcpSocket = null;
    protected ObjectOutputStream outputStream = null;
    protected ObjectInputStream inputStream = null;
    protected boolean connected = false;

    public IoTcpReadRunnable(MessageProcessor<MessageType> messageProcessor, Connection connection)
    {
        this.connection = connection;
        this.config = connection.getConnectionConfiguration();
        this.messageProcessor = messageProcessor;
        this.init();
    }

    protected void init()
    {
        SocketFactory factory = config.isSsl() ? SSLSocketFactory.getDefault() : SocketFactory.getDefault();
        try
        {
            this.tcpSocket = factory.createSocket(config.getIpAddress(), config.getPort());
            this.outputStream = new ObjectOutputStream(tcpSocket.getOutputStream());
            this.outputStream.flush();

            this.inputStream = new ObjectInputStream(tcpSocket.getInputStream());
        }
        catch (IOException ex)
        {
            logger.error("Tcp Socket Read Error ", ex);
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
        //todo
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

    }
}
