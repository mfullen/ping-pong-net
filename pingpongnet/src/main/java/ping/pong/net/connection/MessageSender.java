package ping.pong.net.connection;

/**
 *
 * @author mfullen
 */
public interface MessageSender<Message extends Envelope<Message>>
{
    /**
     *  Sends a message to the other end of the connection.
     * @param message the message to send
     */
    void sendMessage(Message message);
}
