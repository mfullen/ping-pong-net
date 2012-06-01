package ping.pong.net.server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ping.pong.net.connection.Connection;
import ping.pong.net.connection.ConnectionConfiguration;
import ping.pong.net.connection.ConnectionFactory;

/**
 *
 * @author mfullen
 */
class ServerConnectionManager extends Thread
{
    public static final Logger logger = LoggerFactory.getLogger(ServerConnectionManager.class);
    private boolean listening = true;
    protected ConnectionConfiguration configuration;
    protected ServerSocket tcpServerSocket = null;
    protected DatagramSocket udpServerSocket = null;
    protected List<Connection> acceptedConnections = new ArrayList<Connection>();
    private ExecutorService executorService = Executors.newFixedThreadPool(25);

    public ServerConnectionManager(ConnectionConfiguration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * Shutdown the connection manager and all connection threads.
     */
    public void shutdown()
    {
        if (this.executorService.isShutdown())
        {
            return;
        }
        List<Runnable> shutdownNow = this.executorService.shutdownNow();
        boolean shutdown = this.executorService.isShutdown() && this.executorService.isTerminated();
        logger.info("Shutdown of Executor serivce has {}", shutdown ? "completed successfully." : "failed.");
        this.listening = false;
    }

    /**
     *
     * @return
     */
    public boolean isListening()
    {
        return listening;
    }

    @Override
    public void run()
    {
        try
        {
            ServerSocketFactory socketFactory = configuration.isSsl() ? SSLServerSocketFactory.getDefault() : ServerSocketFactory.getDefault();
            try
            {
                tcpServerSocket = socketFactory.createServerSocket();
                tcpServerSocket.setReuseAddress(true);
                tcpServerSocket.bind(new InetSocketAddress(configuration.getPort()));
            }
            catch (IOException ex)
            {
                logger.error("Error creating TCP server socket. " + ex);
            }
            try
            {
                udpServerSocket = new DatagramSocket(configuration.getUdpPort());
            }
            catch (Exception e)
            {
                logger.error("Error creating UDP server socket. " + e);
            }

            while (listening)
            {
                Socket acceptingSocket = null;
                try
                {
                    acceptingSocket = tcpServerSocket.accept();
                }
                catch (IOException ex)
                {
                    logger.error("Error accepting TCP socket.", ex);
                }
                if (acceptingSocket != null)
                {
                    Connection createDefaultIOServerConnection = ConnectionFactory.createDefaultIOServerConnection(configuration, acceptingSocket, udpServerSocket);
                    //this.acceptedConnections.add(createDefaultIOServerConnection);
                    executorService.execute(createDefaultIOServerConnection);
                }
            }
        }
        catch (Exception exception)
        {
            ServerExceptionHandler.handleException(exception);
        }
        finally
        {
            this.shutdown();
        }
    }
}
