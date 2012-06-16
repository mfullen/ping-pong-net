package ping.pong.net.connection.io;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ping.pong.net.connection.ConnectionUtils;
import ping.pong.net.connection.RunnableEventListener;
import ping.pong.net.connection.messaging.MessageProcessor;

/**
 *
 * @author mfullen
 */
public class IoUdpReadRunnable<MessageType> implements Runnable
{
    /**
     * Logger for IoUdpReadRunnable
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(IoUdpReadRunnable.class);
    private static final int RECEIVE_BUFFER_SIZE = 1024;
    /**
     * MessageProcessor to process the messages read
     */
    protected MessageProcessor<MessageType> messageProcessor = null;
    /**
     * Flag for whether this thread is running
     */
    protected boolean running = false;
    protected DatagramSocket udpSocket = null;
    /**
     * Notifies the listener when this runnable is closed
     */
    protected RunnableEventListener runnableEventListener = null;

    public IoUdpReadRunnable(MessageProcessor<MessageType> messageProcessor, RunnableEventListener runnableEventListener, DatagramSocket udpSocket)
    {
        this.messageProcessor = messageProcessor;
        this.udpSocket = udpSocket;
        this.runnableEventListener = runnableEventListener;
    }

    public void close()
    {
        this.running = false;
        if (this.udpSocket != null)
        {
            LOGGER.trace("attempting to close udp socket");
            this.udpSocket.close();
        }
        else
        {
            LOGGER.error("UDP SOCKET IS NULL");
        }


        if (this.runnableEventListener != null)
        {
            this.runnableEventListener.onRunnableClosed();
            this.runnableEventListener = null;
            LOGGER.debug("Udp Read Socket Closed");
        }
    }

    /**
     * Is this thread still running/running?
     * @return
     */
    public synchronized boolean isRunning()
    {
        return this.running;
    }

    @Override
    public void run()
    {
        this.running = true;
        boolean hasErrors = false;
        byte[] data = new byte[RECEIVE_BUFFER_SIZE];
        while (this.running && !hasErrors)
        {
            MessageType messageType = null;
            byte[] trimmedBuffer = null;
            try
            {
                DatagramPacket packet = new DatagramPacket(data, data.length);
                udpSocket.receive(packet);
                byte[] receivedData = packet.getData();

                LOGGER.trace("Received {} from {} on port {}", new Object[]
                        {
                            receivedData, packet.getAddress(),
                            packet.getPort()
                        });

                trimmedBuffer = Arrays.copyOf(receivedData, packet.getLength());

                //If the byte order is LittleEndian convert to BigEndian
                ByteBuffer byteBuffer = ByteBuffer.allocate(trimmedBuffer.length);
                LOGGER.trace("Byte order is {}", byteBuffer.order());
                if (byteBuffer.order().equals(ByteOrder.LITTLE_ENDIAN))
                {
                    trimmedBuffer = byteBuffer.order(ByteOrder.BIG_ENDIAN).array();
                    LOGGER.trace("Byte order converted to {}", ByteOrder.BIG_ENDIAN);
                }

                messageType = ConnectionUtils.<MessageType>getObject(trimmedBuffer);
                LOGGER.trace("Message deserialized into: " + messageType);

            }
            catch (StreamCorruptedException streamCorruptedException)
            {
                //The received message isn't an Object so process it as normal byte[]
                messageType = (MessageType) trimmedBuffer;
            }
            catch (IOException ex)
            {
                LOGGER.error("Error receiving UDP packet", ex);
                hasErrors = true;
            }
            catch (ClassNotFoundException ex)
            {
                LOGGER.error("Error converting to object");
            }
            finally
            {
                if (messageType != null)
                {
                    this.messageProcessor.enqueueReceivedMessage(messageType);
                }
            }
        }
        this.close();
    }
}
