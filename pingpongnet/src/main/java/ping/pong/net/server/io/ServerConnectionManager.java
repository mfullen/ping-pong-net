package ping.pong.net.server.io;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ping.pong.net.connection.Connection;
import ping.pong.net.connection.ConnectionConfiguration;
import ping.pong.net.connection.ConnectionEvent;
import ping.pong.net.connection.Envelope;
import ping.pong.net.connection.messages.ConnectionIdMessage;
import ping.pong.net.connection.messages.ConnectionIdMessage.ResponseMessage;
import ping.pong.net.server.ServerExceptionHandler;

/**
 *
 * @author mfullen
 */
final class ServerConnectionManager<MessageType> implements Runnable
{
    public static final Logger logger = LoggerFactory.getLogger(ServerConnectionManager.class);
    protected boolean listening = true;
    protected ConnectionConfiguration configuration;
    protected ServerSocket tcpServerSocket = null;
    protected DatagramSocket udpServerSocket = null;
    protected IoServerImpl<MessageType> server = null;

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

        for (Connection connection : this.server.connectionsMap.values())
        {
            connection.close();
        }

        this.listening = false;
        this.configuration = null;

        try
        {
            if (tcpServerSocket != null)
            {
                tcpServerSocket.close();
            }
            else
            {
                logger.warn("TCP Socket is null");
            }
        }
        catch (IOException ex)
        {
            logger.error("Error Closing TCP socket");
        }

        if (udpServerSocket != null)
        {
            udpServerSocket.close();
        }
        else
        {
            logger.warn("UDP Socket is null");
        }

        this.tcpServerSocket = null;
        this.udpServerSocket = null;
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
            if (configuration.isSsl())
            {
                System.setProperty("javax.net.ssl.keyStore", configuration.getKeystorePath());
                System.setProperty("javax.net.ssl.keyStorePassword", configuration.getKeystorePassword());
            }

            ServerSocketFactory socketFactory = configuration.isSsl() ? SSLServerSocketFactory.getDefault() : ServerSocketFactory.getDefault();

            try
            {
                tcpServerSocket = socketFactory.createServerSocket(configuration.getPort());
                tcpServerSocket.setReuseAddress(true);
//                if (configuration.isSsl())
//                {
//                    ((SSLServerSocket) tcpServerSocket).setNeedClientAuth(true);
//                }
                // tcpServerSocket.bind(new InetSocketAddress(configuration.getPort()));
            }
            catch (IOException ex)
            {
                logger.error("Error creating TCP server socket. " + ex);
                listening = false;
            }
            try
            {
                udpServerSocket = new DatagramSocket(configuration.getUdpPort());
            }
            catch (Exception e)
            {
                logger.error("Error creating UDP server socket. " + e);
                listening = false;
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
                    ServerExceptionHandler.handleException(ex, logger);
                    this.shutdown();
                }
                if (acceptingSocket != null)
                {
                    final Connection ioServerConnection = new IoServerConnectionImpl<MessageType>(configuration, acceptingSocket, udpServerSocket);
                    ioServerConnection.setConnectionId(this.server.getNextAvailableId());
                    ioServerConnection.addConnectionEventListener(new ConnectionEvent()
                    {
                        @Override
                        public void onSocketClosed()
                        {
                            //remove the connection from the server
                            if (server.hasConnections())
                            {
                                Connection connection = server.getConnection(ioServerConnection.getConnectionId());
                                if (connection != null)
                                {
                                    server.removeConnection(ioServerConnection);
                                }
                            }
                        }

                        @Override
                        public void onSocketCreated()
                        {
                            ioServerConnection.sendMessage(new Envelope<ConnectionIdMessage.ResponseMessage>()
                            {
                                @Override
                                public boolean isReliable()
                                {
                                    return true;
                                }

                                @Override
                                public ResponseMessage getMessage()
                                {
                                    ResponseMessage responseMessage = new ConnectionIdMessage.ResponseMessage(ioServerConnection.getConnectionId());
                                    return responseMessage;
                                }
                            });
                            logger.debug("OnSocketCreated");
                        }
                    });
                    ((IoServerConnectionImpl) ioServerConnection).fireOnSocketCreated();
                    Thread cThread = new Thread(ioServerConnection, "Connection: " + ioServerConnection.getConnectionId());
                    cThread.setDaemon(true);
                    cThread.start();
                    this.server.addConnection(ioServerConnection);
                    logger.info("Connection {} started...", ioServerConnection.getConnectionId());
                }
            }
        }
        catch (Exception exception)
        {
            ServerExceptionHandler.handleException(exception, logger);
        }
        finally
        {
            this.shutdown();
        }
    }
}
