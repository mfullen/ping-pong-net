package ping.pong.net.connection;

/**
 *
 * @author mfullen
 */
public class DefaultEnvelope<Message> implements Envelope<Message>
{
    private boolean reliable = true;
    private Message message;

    public void setReliable(boolean reliable)
    {
        this.reliable = reliable;
    }

    public void setMessage(Message message)
    {
        this.message = message;
    }

    @Override
    public boolean isReliable()
    {
        return reliable;
    }

    @Override
    public Message getMessage()
    {
        return message;
    }
}