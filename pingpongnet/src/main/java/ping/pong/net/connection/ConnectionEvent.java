package ping.pong.net.connection;

/**
 *
 */
public interface ConnectionEvent<MessageType>
{
    void onSocketClosed();

    void onSocketCreated();

    void onSocketReceivedMessage(MessageType message);
}
