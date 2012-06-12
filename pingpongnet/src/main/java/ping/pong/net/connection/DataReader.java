package ping.pong.net.connection;

import java.io.InputStream;
import java.net.Socket;

/**
 *
 * @author mfullen
 */
public interface DataReader<Type>
{
    Type readData();

    InputStream init(Socket socket);
}
