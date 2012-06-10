package ping.pong.net.client.io;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ping.pong.net.client.Client;
import ping.pong.net.client.ClientConnectionListener;
import ping.pong.net.connection.Connection;
import ping.pong.net.connection.ConnectionConfiguration;
import ping.pong.net.connection.ConnectionFactory;
import ping.pong.net.connection.DisconnectInfo;
import ping.pong.net.connection.Envelope;
import ping.pong.net.connection.MessageListener;

/**
 * The Io Client Implementation of the Client interface.
 * @author mfullen
 */
public final class IoClientImpl<Message> implements Client<Message>
{
    /**
     * The logger being user for this class
     */
    public static final Logger logger = LoggerFactory.getLogger(IoClientImpl.class);
    /**
     * The Connection for the Client
     */
    protected Connection<Message> connection = null;
    /**
     * The connection Configuration used when attempting to create a connection
     */
    protected ConnectionConfiguration config = null;
    /**
     * The list of Message listeners for this client
     */
    protected List<MessageListener> messageListeners = new ArrayList<MessageListener>();
    /**
     * The list of ConnectionListeners for this client
     */
    protected List<ClientConnectionListener> connectionListeners = new ArrayList<ClientConnectionListener>();

    /**
     * Constructor for a default IoClient Implementation. Creates it based of
     * defaults for a Connection Configuration
     */
    public IoClientImpl()
    {
        this(ConnectionFactory.createConnectionConfiguration());
    }

    /**
     * Creates a Client Implementation based off the given ConnectionConfiguration
     * @param config the configuration to use
     */
    public IoClientImpl(ConnectionConfiguration config)
    {
        this.config = config;
        this.connection = new IoClientConnectionImpl<Message>(this, config);
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
            Thread connectionThread = new Thread(this.connection, "IoClientConnection");
            connectionThread.start();
            logger.info("Client connected to server {} on TCP port {}", this.config.getIpAddress(), this.config.getPort());
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

    /**
     * Package Private method which handles message received from internal classes
     * and passes the message through the message listeners
     * @param message the message to pass on to the message listeners
     */
    synchronized void handleMessageReceived(Message message)
    {
        for (MessageListener<? super Client<Message>, Message> messageListener : messageListeners)
        {
            messageListener.messageReceived(this, message);
        }
    }

    /**
     * Package Private method which handles client connected from internal classes
     * and passes the message through the connection listeners
     */
    synchronized void onClientConnected()
    {
        for (ClientConnectionListener clientConnectionListener : connectionListeners)
        {
            clientConnectionListener.clientConnected(this);
        }
    }

    /**
     * Package Private method which handles client disconnected from internal classes
     * @param disconnectInfo The disconnect information associated with the disconnect
     */
    synchronized void onClientDisconnected(DisconnectInfo disconnectInfo)
    {
        for (ClientConnectionListener clientConnectionListener : connectionListeners)
        {
            clientConnectionListener.clientDisconnected(this, disconnectInfo);
        }
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
        if (this.connection == null)
        {
            logger.error("Connection is null");
        }
        else if (this.connection.isConnected())
        {
            this.connection.sendMessage(message);
        }
    }
}
