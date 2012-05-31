package ping.pong.net.connection;

/**
 *
 * @author mfullen
 */
public interface MessageListener<S, Message>
{
    public void messageReceived(S source, Message message);
}
