package ping.pong.net.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ping.pong.net.connection.Connection;
import ping.pong.net.connection.ConnectionConfiguration;
import ping.pong.net.connection.ConnectionFactory;
import ping.pong.net.connection.Envelope;
import ping.pong.net.connection.MessageListener;

/**
 *
 * @author mfullen
 */
public final class IoServerImpl<Message> implements Server<Envelope<Message>>
{
    public static final Logger logger = LoggerFactory.getLogger(IoServerImpl.class);
    protected Map<Integer, Connection> connectionsMap = new ConcurrentHashMap<Integer, Connection>();
    protected List<MessageListener> messageListeners = new ArrayList<MessageListener>();
    protected List<ServerConnectionListener> connectionListeners = new ArrayList<ServerConnectionListener>();
    private ServerConnectionManager serverConnectionManager = null;
    protected ConnectionConfiguration config = null;

    public IoServerImpl()
    {
        this(ConnectionFactory.createConnectionConfiguration());
    }

    public IoServerImpl(ConnectionConfiguration config)
    {
        this.config = config;
    }

    @Override
    public void broadcast(Envelope<Message> message)
    {
        logger.info("Broadcasting Message: {}", message);
        for (Connection connection : this.connectionsMap.values())
        {
            connection.sendMessage(message);
        }
    }

    @Override
    public void start()
    {
        if (serverConnectionManager != null)
        {
            logger.error("Cannot start server. It is already running");
            return;
        }
        this.serverConnectionManager = new ServerConnectionManager(config);
        this.serverConnectionManager.start();
        logger.info("Server started {} on port {}", config.getIpAddress(), config.getPort());
    }

    @Override
    public void shutdown()
    {
        if (serverConnectionManager == null)
        {
            logger.error("Server Connection Manager is null.");
            return;
        }

        this.serverConnectionManager.shutdown();
    }

    @Override
    public Connection getConnection(int id)
    {
        Connection connection = this.connectionsMap.get(id);
        if (connection == null)
        {
            logger.error("Connection Id {} not found.", id);
        }
        return connection;
    }

    @Override
    public Collection<Connection> getConnections()
    {
        return Collections.unmodifiableCollection(this.connectionsMap.values());
    }

    @Override
    public boolean hasConnections()
    {
        return !this.connectionsMap.isEmpty();
    }

    @Override
    public boolean isListening()
    {
        return serverConnectionManager != null && serverConnectionManager.isListening() && serverConnectionManager.isAlive();
    }

    @Override
    public void addMessageListener(MessageListener<? super Connection, Envelope<Message>> listener)
    {
        boolean added = this.messageListeners.add(listener);
        logger.trace("Add Message Listener: {}", added ? "Successful" : "Failure");
    }

    @Override
    public void removeMessageListener(MessageListener<? super Connection, Envelope<Message>> listener)
    {
        boolean removed = this.messageListeners.remove(listener);
        logger.trace("Remove Message Listener: {}", removed ? "Successful" : "Failure");
    }

    @Override
    public void addConnectionListener(ServerConnectionListener connectionListener)
    {
        boolean added = this.connectionListeners.add(connectionListener);
        logger.trace("Add Connection Listener: {}", added ? "Successful" : "Failure");
    }

    @Override
    public void removeConnectionListener(ServerConnectionListener connectionListener)
    {
        boolean removed = this.connectionListeners.remove(connectionListener);
        logger.trace("Remove Connection Listener: {}", removed ? "Successful" : "Failure");
    }
}
