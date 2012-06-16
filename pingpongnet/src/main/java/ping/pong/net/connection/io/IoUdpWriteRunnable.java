package ping.pong.net.connection.io;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ping.pong.net.connection.ConnectionUtils;
import ping.pong.net.connection.RunnableEventListener;
import ping.pong.net.connection.config.ConnectionConfiguration;

/**
 *
 * @author mfullen
 */
public class IoUdpWriteRunnable<MessageType> extends AbstractIoUdpRunnable
{
    /**
     * Logger for IoUdpReadRunnable
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(IoUdpWriteRunnable.class);
    /**
     * The queue of messages to write from
     */
    protected BlockingQueue<MessageType> writeQueue = new LinkedBlockingQueue<MessageType>();
    /**
     * Configuration for the UDP write
     */
    protected ConnectionConfiguration config = null;

    public IoUdpWriteRunnable(ConnectionConfiguration config, RunnableEventListener runnableEventListener, DatagramSocket udpSocket)
    {
        super(runnableEventListener, udpSocket);
        this.config = config;
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
        this.running = true;

        try
        {
            while (this.running)
            {
                MessageType message = this.writeQueue.take();
                byte[] messageBytes = null;
                if (message instanceof byte[])
                {
                    messageBytes = (byte[]) message;
                    LOGGER.trace("Message to send is byte[]");
                }
                else
                {
                    messageBytes = ConnectionUtils.getbytes(message);
                    LOGGER.trace("Message to send is {}", message);
                }
                DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, InetAddress.getByName(this.config.getIpAddress()), this.config.getUdpPort());
                this.udpSocket.send(packet);
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Error Writing UDP message", e);
        }
        finally
        {
            this.close();
        }
    }
}
