package ping.pong.net.connection.messaging;

/**
 *
 * @author mfullen
 */
public interface MessageListener<S, Message>
{
    public void messageReceived(S source, Message message);
}
