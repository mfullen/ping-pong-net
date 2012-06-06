package ping.pong.net.connection;

/**
 *
 */
public interface ConnectionEvent
{
    void onSocketClosed();

    void onSocketCreated();
}
