package ping.pong.net.connection;

/**
 *
 * @author mfullen
 */
public interface MessageSender<Message extends Object>
{
    /**
     *  Sends a message to the other end of the connection.
     * @param message the message to send
     */
    void sendMessage(Message message);
}
