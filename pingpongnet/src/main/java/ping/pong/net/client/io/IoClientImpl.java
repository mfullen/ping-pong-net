package ping.pong.net.client.io;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ping.pong.net.client.Client;
import ping.pong.net.client.ClientConnectionListener;
import ping.pong.net.connection.Connection;
import ping.pong.net.connection.ConnectionConfiguration;
import ping.pong.net.connection.ConnectionFactory;
import ping.pong.net.connection.Envelope;
import ping.pong.net.connection.MessageListener;

/**
 *
 * @author mfullen
 */
public final class IoClientImpl<Message> implements Client<Message>
{
    public static final Logger logger = LoggerFactory.getLogger(IoClientImpl.class);
    protected Connection<Message> connection = null;
    protected ConnectionConfiguration config = null;
    protected DatagramSocket udpSocket = null;

    protected List<MessageListener> messageListeners = new ArrayList<MessageListener>();
    protected List<ClientConnectionListener> connectionListeners = new ArrayList<ClientConnectionListener>();

    public IoClientImpl()
    {
        this(ConnectionFactory.createConnectionConfiguration());
    }

    public IoClientImpl(ConnectionConfiguration config)
    {
        this.config = config;
    }

    @Override
    public void start()
    {
        if (this.connection == null)
        {
            logger.error("Connection is null");
        }
        else if (this.connection.isConnected())
        {
            logger.warn("Can't start connection it is already running");
        }
        else
        {
            Thread connectionThread = new Thread(this.connection);
            connectionThread.start();
            logger.info("Client connected to server {} on port {}", -1, -1);
        }
    }

    @Override
    public void close()
    {
        if (this.connection == null)
        {
            logger.error("Connection is null");
            return;
        }
        this.connection.close();
        logger.info("Client Closed");
    }

    @Override
    public boolean isConnected()
    {
        return this.connection != null && this.connection.isConnected();
    }

    @Override
    public int getId()
    {
        if (isConnected())
        {
            return this.connection.getConnectionId();
        }
        return -1;
    }

    @Override
    public void addMessageListener(MessageListener<? super Client, Message> listener)
    {
        boolean added = false;
        if (listener != null)
        {
            added = this.messageListeners.add(listener);
        }
        logger.trace("Add Message Listener: {}", added ? "Successful" : "Failure");
    }

    @Override
    public void removeMessageListener(MessageListener<? super Client, Message> listener)
    {
        boolean removed = false;
        if (listener != null)
        {
            removed = this.messageListeners.remove(listener);
        }
        logger.trace("Remove Message Listener: {}", removed ? "Successful" : "Failure");
    }

    @Override
    public void addConnectionListener(ClientConnectionListener listener)
    {
        boolean added = false;
        if (listener != null)
        {
            added = this.connectionListeners.add(listener);
        }
        logger.trace("Add Connection Listener: {}", added ? "Successful" : "Failure");
    }

    @Override
    public void removeConnectionListener(ClientConnectionListener listener)
    {
        boolean removed = false;
        if (listener != null)
        {
            removed = this.connectionListeners.remove(listener);
        }
        logger.trace("Remove Connection Listener: {}", removed ? "Successful" : "Failure");
    }

    @Override
    public void sendMessage(Envelope<Message> message)
    {
        this.connection.sendMessage(message);
    }
}
