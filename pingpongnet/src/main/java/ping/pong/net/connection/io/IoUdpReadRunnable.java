package ping.pong.net.connection.io;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
            synchronized (udpSocket)
            {
                this.udpSocket.close();
            }
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

                byte[] trimmedBuffer = Arrays.copyOf(receivedData, packet.getLength());

                this.messageProcessor.enqueueReceivedMessage((MessageType) trimmedBuffer);
            }
            catch (IOException ex)
            {
                LOGGER.error("Error receiving UDP packet", ex);
                hasErrors = true;
            }
        }
        this.close();
    }
}
