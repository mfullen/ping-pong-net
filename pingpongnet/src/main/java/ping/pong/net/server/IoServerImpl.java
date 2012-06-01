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
public final class IoServerImpl<MessageType> implements
        Server<Envelope<MessageType>>
{
    public static final Logger logger = LoggerFactory.getLogger(IoServerImpl.class);
    private static final String CANT_ADD_LISTENER = "You must add Listeners before the server is started";
    protected Map<Integer, Connection> connectionsMap = new ConcurrentHashMap<Integer, Connection>();
    private ServerConnectionManager<MessageType> serverConnectionManager = null;
    protected ConnectionConfiguration config = null;
    protected List<MessageListener> messageListeners = new ArrayList<MessageListener>();
    protected List<ServerConnectionListener> connectionListeners = new ArrayList<ServerConnectionListener>();

    public IoServerImpl()
    {
        this(ConnectionFactory.createConnectionConfiguration());
    }

    public IoServerImpl(ConnectionConfiguration config)
    {
        this.config = config;
    }

    @Override
    public void broadcast(Envelope<MessageType> message)
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
        this.serverConnectionManager = new ServerConnectionManager(config, this);
        new Thread(this.serverConnectionManager).start();
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
        this.serverConnectionManager = null;
        logger.info("Server shutdown");
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
        return serverConnectionManager != null && serverConnectionManager.isListening();
    }

    @Override
    public void addMessageListener(MessageListener<? super Connection, Envelope<MessageType>> listener)
    {
        boolean added = this.messageListeners.add(listener);
        logger.trace("Add Message Listener: {}", added ? "Successful" : "Failure");
    }

    @Override
    public void removeMessageListener(MessageListener<? super Connection, Envelope<MessageType>> listener)
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

    private boolean hasConnectionManager()
    {
        return this.serverConnectionManager == null ? false : true;
    }

    synchronized void addConnection(Connection<MessageType> connection)
    {
        int id = this.getNextAvailableId();
        connection.setConnectionId(id);
        
        this.connectionsMap.put(id, connection);

        for (ServerConnectionListener serverConnectionListener : this.connectionListeners)
        {
            serverConnectionListener.connectionAdded(this, connection);
        }
    }

    @Override
    public synchronized int getNextAvailableId()
    {
        int id = 1;

        boolean needsId = true;
        while (needsId)
        {
            if (!this.connectionsMap.containsKey(id))
            {
                needsId = false;
                return id;
            }
            id++;
        }
        return id;
    }
}
