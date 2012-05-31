package ping.pong.net.connection;

/**
 *
 * @author mfullen
 */
public interface DisconnectInfo
{
    String getReason();

    DisconnectState getDisconnectState();
}
