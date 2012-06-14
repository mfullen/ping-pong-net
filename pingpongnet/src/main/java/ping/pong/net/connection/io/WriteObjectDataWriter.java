package ping.pong.net.connection.io;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ping.pong.net.connection.DataWriter;

/**
 *
 * @author mfullen
 */
public class WriteObjectDataWriter implements DataWriter<Object>
{
    private ObjectOutputStream outputStream = null;
    private static Logger logger = LoggerFactory.getLogger(WriteObjectDataWriter.class);

    @Override
    public OutputStream init(Socket socket)
    {
        try
        {
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
            this.outputStream.flush();
        }
        catch (IOException ex)
        {
            logger.error("Tcp Socket Init Error Error ", ex);
        }
        return this.outputStream;
    }

    @Override
    public void writeData(Object data)
    {
        try
        {
            logger.trace("About to write Object to Stream {}", data);
            this.outputStream.writeObject(data);
            this.outputStream.flush();
            logger.trace("Wrote {} to stream", data);
            logger.trace("Flushed Outputstream");
        }
        catch (IOException ex)
        {
            logger.error("Error Writing Object", ex);
        }
    }
}
