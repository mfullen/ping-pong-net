package ping.pong.net.client;

import ping.pong.net.connection.Envelope;
import ping.pong.net.connection.MessageListener;
import ping.pong.net.connection.MessageSender;

/**
 *
 * @author mfullen
 */
public interface Client<Message> extends
        MessageSender<Envelope<Message>>
{
    void start();

    void close();

    boolean isConnected();

    int getId();

    void addMessageListener(MessageListener<? super Client, Message> listener);

    void removeMessageListener(MessageListener<? super Client, Message> listener);

    void addConnectionListener(ClientConnectionListener listener);

    void removeConnectionListener(ClientConnectionListener listener);
}
