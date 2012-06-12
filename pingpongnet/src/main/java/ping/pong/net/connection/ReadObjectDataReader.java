package ping.pong.net.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mfullen
 */
public class ReadObjectDataReader implements
        DataReader<Object>
{
    private ObjectInputStream inputStream = null;
    private static Logger logger = LoggerFactory.getLogger(ReadObjectDataReader.class);

    public ReadObjectDataReader()
    {
    }

    @Override
    public InputStream init(Socket socket)
    {
        try
        {
            this.inputStream = new ObjectInputStream(socket.getInputStream());
        }
        catch (IOException ex)
        {
            logger.error("Tcp Socket Init Error Error ", ex);
        }
        return this.inputStream;
    }

    @Override
    public synchronized Object readData()
    {
        Object readObject = null;
        logger.trace("About to block for read Object");
        try
        {
            readObject = this.inputStream.readObject();
        }
        catch (IOException ex)
        {
            ConnectionExceptionHandler.handleException(ex, logger);
        }
        catch (ClassNotFoundException ex)
        {
            ConnectionExceptionHandler.handleException(ex, logger);
        }
        logger.trace("{Read Object from Stream: {} ", readObject);

        return readObject;
    }
}
