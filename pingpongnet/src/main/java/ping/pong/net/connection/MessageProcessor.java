package ping.pong.net.connection;

/**
 *
 * @author mfullen
 */
public interface MessageProcessor<Message>
{
    void enqueueReceivedMessage(Message message);

    void enqueueMessageToWrite(Envelope<Message> message);
}
