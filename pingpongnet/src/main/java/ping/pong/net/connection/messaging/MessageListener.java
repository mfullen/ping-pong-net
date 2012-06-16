package ping.pong.net.connection.messaging;

/**
 *
 * @author mfullen
 */
public interface MessageListener<S, Message>
{
    void messageReceived(S source, Message message);
}
