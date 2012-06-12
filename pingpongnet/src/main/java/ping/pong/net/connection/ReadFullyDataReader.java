package ping.pong.net.connection;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mfullen
 */
public class ReadFullyDataReader implements DataReader<byte[]>
{
    private DataInputStream inputStream = null;
    private static Logger logger = LoggerFactory.getLogger(ReadFullyDataReader.class);

    public ReadFullyDataReader()
    {
    }

    @Override
    public InputStream init(Socket socket)
    {
        try
        {
            this.inputStream = new DataInputStream(socket.getInputStream());
        }
        catch (IOException ex)
        {
            logger.error("Tcp Socket Init Error Error ", ex);
        }
        return this.inputStream;
    }

    @Override
    public synchronized byte[] readData()
    {
        int size = 0;
        try
        {
            size = inputStream.readInt();
        }
        catch (IOException ex)
        {
            logger.error("Error reading size", ex);
        }
        logger.debug("Size: {}", size);
        byte[] buffer = new byte[size];
        try
        {
            this.inputStream.readFully(buffer);
        }
        catch (IOException ex)
        {
            logger.error("Error reading Rest of data", ex);
        }
        return buffer;
    }
}
