package ping.pong.net.connection;

import java.io.OutputStream;
import java.net.Socket;

/**
 *
 * @author mfullen
 */
public interface DataWriter<Type>
{
    OutputStream init(Socket socket);

    void writeData(Type data);
}
