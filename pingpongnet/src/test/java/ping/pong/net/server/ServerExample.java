package ping.pong.net.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import ping.pong.net.connection.Envelope;

/**
 *
 * @author mfullen
 */
public class ServerExample
{
    public static void main(String[] args) throws UnknownHostException,
                                                  IOException,
                                                  InterruptedException
    {
        //Server<String> server = new DefaultIoServerConnection<String>(ConnectionFactory.createConnectionConfiguration(), new Socket("localhost", 5011), null);
        Server<Envelope<String>> server = new IoServerImpl<String>();
        server.start();
        {
            Socket client = new Socket("localhost", 5011);
            ObjectOutputStream outputStream = new ObjectOutputStream(client.getOutputStream());
            outputStream.writeObject("hello world");
            outputStream.close();
        }
        {
            Socket client = new Socket("localhost", 5011);
            ObjectOutputStream outputStream = new ObjectOutputStream(client.getOutputStream());
            for (int i = 0; i < 5; i++)
            {
                outputStream.writeObject("hello world:" + i);
                Thread.sleep(500);
            }

        }
    }
}
