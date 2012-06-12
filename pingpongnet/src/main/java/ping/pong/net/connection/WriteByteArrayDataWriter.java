package ping.pong.net.connection;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mfullen
 */
public class WriteByteArrayDataWriter implements DataWriter<byte[]>
{
    private DataOutputStream outputStream = null;
    private static Logger logger = LoggerFactory.getLogger(WriteByteArrayDataWriter.class);

    @Override
    public OutputStream init(Socket socket)
    {
        try
        {
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            this.outputStream.flush();
        }
        catch (IOException ex)
        {
            logger.error("Tcp Socket Init Error Error ", ex);
        }
        return this.outputStream;
    }

    @Override
    public void writeData(byte[] data)
    {
        try
        {
            logger.trace("About to write Object to Stream {}", data);
            this.outputStream.writeInt(data.length);
            this.outputStream.flush();
            this.outputStream.write(data);
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
