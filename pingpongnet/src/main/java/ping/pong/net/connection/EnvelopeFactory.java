package ping.pong.net.connection;

/**
 *
 * @author mfullen
 */
public final class EnvelopeFactory
{
    private EnvelopeFactory()
    {
    }

    public static <MessageType> Envelope createTcpEnvelope(MessageType message)
    {
        DefaultEnvelope<MessageType> envelope = new DefaultEnvelope<MessageType>();
        envelope.setMessage(message);
        envelope.setReliable(true);
        return envelope;
    }

    public static <MessageType> Envelope createUdpEnvelope(MessageType message)
    {
        DefaultEnvelope<MessageType> envelope = new DefaultEnvelope<MessageType>();
        envelope.setMessage(message);
        envelope.setReliable(false);
        return envelope;
    }
}
