package ping.pong.net.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import ping.pong.net.connection.Envelope;

/**
 *
 * @author mfullen
 */
public class ClientExample
{
    public static void main(String[] args) throws IOException,
                                                  InterruptedException
    {
        {
            Socket client = new Socket("localhost", 5011);
            ObjectInputStream inputStream = new ObjectInputStream(client.getInputStream());
            int readInt = inputStream.readInt();
            System.out.println("I was assigned Client id: " + readInt);
            ObjectOutputStream outputStream = new ObjectOutputStream(client.getOutputStream());
            outputStream.writeObject(new byte[]
                    {
                        1, 3, 2, 3
                    });
            outputStream.flush();
            outputStream.close();
        }
        {
            Socket client = new Socket("localhost", 5011);
            ObjectInputStream inputStream = new ObjectInputStream(client.getInputStream());
            int readInt = inputStream.readInt();
            System.out.println("I was assigned Client id: " + readInt);
            ObjectOutputStream outputStream = new ObjectOutputStream(client.getOutputStream());
            for (int i = 0; i < 5; i++)
            {
                outputStream.writeObject("hello world:" + i);
                Thread.sleep(500);
            }
            outputStream.flush();
            outputStream.close();
        }

        while (true)
        {
        }
//
//        Envelope<String> udpEnvelope = new Envelope<String>()
//        {
//            @Override
//            public boolean isReliable()
//            {
//                return false;
//            }
//
//            @Override
//            public String getMessage()
//            {
//                return "This is a UDP message";
//            }
//        };
//        Envelope<String> tcpEnvelope = new Envelope<String>()
//        {
//            @Override
//            public boolean isReliable()
//            {
//                return false;
//            }
//
//            @Override
//            public String getMessage()
//            {
//                return "This is a TCP message";
//            }
//        };
    }
}
