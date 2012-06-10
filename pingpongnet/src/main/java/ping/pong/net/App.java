package ping.pong.net;

import ping.pong.net.connection.Connection;
import ping.pong.net.connection.messaging.Envelope;
import ping.pong.net.server.Server;
import ping.pong.net.server.ServerConnectionListener;
import ping.pong.net.server.io.IoServerImpl;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main(String[] args)
    {
        IoServerImpl<String> server = new IoServerImpl<String>();
        server.addConnectionListener(new ServerConnectionListener()
        {
            @Override
            public void connectionAdded(Server server, Connection conn)
            {
                System.out.println("Connection Added");
                server.broadcast(new Envelope<String>()
                {
                    @Override
                    public boolean isReliable()
                    {
                        return true;
                    }

                    @Override
                    public String getMessage()
                    {
                        return "Test";
                    }
                });
                printStats();
            }

            @Override
            public void connectionRemoved(Server server, Connection conn)
            {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        server.start();
        printStats();
    }

    public static void printStats()
    {
        Runtime runtime = Runtime.getRuntime();
        double conversion = 1.0 / 1000000.0;
        double maxMemory = runtime.maxMemory() * conversion;
        double totalMemory = runtime.totalMemory() * conversion;
        double freeMemory = runtime.freeMemory() * conversion;

        System.out.println("MAX: " + maxMemory);
        System.out.println("Total: " + totalMemory);
        System.out.println("Free: " + freeMemory);
    }
}
