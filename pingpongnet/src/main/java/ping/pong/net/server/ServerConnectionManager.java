package ping.pong.net.server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ping.pong.net.connection.Connection;
import ping.pong.net.connection.ConnectionConfiguration;

/**
 *
 * @author mfullen
 */
class ServerConnectionManager<MessageType> implements Runnable
{
    public static final Logger logger = LoggerFactory.getLogger(ServerConnectionManager.class);
    private boolean listening = true;
    protected ConnectionConfiguration configuration;
    protected ServerSocket tcpServerSocket = null;
    protected DatagramSocket udpServerSocket = null;
    protected IoServerImpl<MessageType> server = null;
    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    public ServerConnectionManager(ConnectionConfiguration configuration, IoServerImpl<MessageType> server)
    {
        this.configuration = configuration;
        this.server = server;
    }

    /**
     * Shutdown the connection manager and all connection threads.
     */
    public void shutdown()
    {
        if (!listening)
        {
            //logger.trace("The Connection Manager has already been shut down. This has no effect");
            return;
        }
        if (this.executorService != null && !this.executorService.isShutdown())
        {
            List<Runnable> shutdownNow = this.executorService.shutdownNow();
            boolean shutdown = this.executorService.isShutdown() && this.executorService.isTerminated();
            logger.info("Shutdown of Executor serivce has {}", shutdown ? "completed successfully." : "failed.");
        }

        this.listening = false;
        this.configuration = null;

        try
        {
            tcpServerSocket.close();
        }
        catch (IOException ex)
        {
            logger.error("Error Closing TCP socket");
        }
        udpServerSocket.close();
        this.tcpServerSocket = null;
        this.udpServerSocket = null;
        this.executorService = null;
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
                logger.trace("ServerConnectionManager about to block until connection accepted.");
                Socket acceptingSocket = null;
                try
                {

                    acceptingSocket = tcpServerSocket.accept();
                }
                catch (IOException ex)
                {
                    ServerExceptionHandler.handleException(ex);
                }
                if (acceptingSocket != null)
                {
                    Connection<MessageType> createDefaultIOServerConnection = new DefaultIoServerConnection<MessageType>(this.server, configuration, acceptingSocket, udpServerSocket);
                    this.server.addConnection(createDefaultIOServerConnection);
                    executorService.execute(createDefaultIOServerConnection);
                    logger.info("Connection {} started...", createDefaultIOServerConnection.getConnectionId());
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
