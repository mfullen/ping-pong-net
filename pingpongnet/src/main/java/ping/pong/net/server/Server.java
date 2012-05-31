package ping.pong.net.server;

import java.util.Collection;
import ping.pong.net.connection.Connection;
import ping.pong.net.connection.MessageListener;

/**
 *
 * @author mfullen
 */
public interface Server<Message>
{
    void broadcast(Message message);

    void start();

    void close();

    Connection getConnection(int id);

    Collection<Connection> getConnections();

    boolean hasConnections();

    boolean isRunning();

    void addMessageListener(MessageListener<? super Connection, Message> listener);

    void removeMessageListener(MessageListener<? super Connection, Message> listener);

    void addConnectionListener(ServerConnectionListener connectionListener);

    void removeConnectionListener(ServerConnectionListener connectionListener);
}
