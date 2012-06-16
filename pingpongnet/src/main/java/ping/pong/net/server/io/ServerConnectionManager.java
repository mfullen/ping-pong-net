package ping.pong.net.server.io;

import ping.pong.net.connection.io.DataWriter;
import ping.pong.net.connection.io.DataReader;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ping.pong.net.connection.*;
import ping.pong.net.connection.config.ConnectionConfiguration;
import ping.pong.net.connection.messaging.EnvelopeFactory;
import ping.pong.net.connection.messaging.MessageListener;
import ping.pong.net.connection.messaging.ConnectionIdMessage;

/**
 *
 * @author mfullen
 */
class ServerConnectionManager<MessageType> implements Runnable
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConnectionManager.class);
    protected boolean listening = true;
    protected ConnectionConfiguration configuration;
    protected ServerSocket tcpServerSocket = null;
    protected DatagramSocket udpServerSocket = null;
    protected IoServer<MessageType> server = null;
    protected DataReader customDataReader = null;
    protected DataWriter customDataWriter = null;

    public ServerConnectionManager(ConnectionConfiguration configuration, IoServer<MessageType> server)
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
                LOGGER.warn("TCP Socket is null");
            }
        }
        catch (IOException ex)
        {
            LOGGER.error("Error Closing TCP socket");
        }

        if (udpServerSocket != null)
        {
            udpServerSocket.close();
        }
        else
        {
            LOGGER.warn("UDP Socket is null");
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
            }
            catch (IOException ex)
            {
                LOGGER.error("Error creating TCP server socket. " + ex);
                listening = false;
            }
            try
            {
                udpServerSocket = new DatagramSocket(configuration.getUdpPort());
            }
            catch (Exception e)
            {
                LOGGER.error("Error creating UDP server socket. " + e);
                listening = false;
            }

            while (listening)
            {
                LOGGER.trace("ServerConnectionManager about to block until connection accepted.");
                Socket acceptingSocket = null;
                try
                {
                    acceptingSocket = tcpServerSocket.accept();
                }
                catch (IOException ex)
                {
                    ConnectionExceptionHandler.handleException(ex, LOGGER);
                    this.shutdown();
                }
                if (acceptingSocket != null)
                {
                    final Connection ioServerConnection = new ServerIoConnection<MessageType>(configuration, customDataReader, customDataWriter, acceptingSocket, udpServerSocket);
                    ioServerConnection.setConnectionId(this.server.getNextAvailableId());

                    ioServerConnection.addConnectionEventListener(new ConnectionEventImpl(ioServerConnection));
                    ((ServerIoConnection) ioServerConnection).fireOnSocketCreated();
                    Thread cThread = new Thread(ioServerConnection, "Connection: " + ioServerConnection.getConnectionId());
                    cThread.setDaemon(true);
                    cThread.start();
                    this.server.addConnection(ioServerConnection);
                    LOGGER.info("Connection {} started...", ioServerConnection.getConnectionId());
                }
            }
        }
        catch (Exception exception)
        {
            ConnectionExceptionHandler.handleException(exception, LOGGER);
        }
        finally
        {
            this.shutdown();
        }
    }

    public void setCustomDataReader(DataReader customDataReader)
    {
        this.customDataReader = customDataReader;
    }

    public void setCustomDataWriter(DataWriter customDataWriter)
    {
        this.customDataWriter = customDataWriter;
    }

    final class ConnectionEventImpl implements ConnectionEvent<MessageType>
    {
        private final Connection ioServerConnection;

        public ConnectionEventImpl(Connection ioServerConnection)
        {
            this.ioServerConnection = ioServerConnection;
        }

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
            if (!ioServerConnection.isUsingCustomSerialization())
            {
                ioServerConnection.sendMessage(EnvelopeFactory.createTcpEnvelope(new ConnectionIdMessage.ResponseMessage(ioServerConnection.getConnectionId())));
                LOGGER.debug("Using PPN Serialization, sending Id Response");
            }

            LOGGER.debug("OnSocketCreated");
        }

        @Override
        public void onSocketReceivedMessage(MessageType message)
        {
            for (MessageListener<Connection<MessageType>, MessageType> messageListener : server.messageListeners)
            {
                messageListener.messageReceived(ioServerConnection, message);
            }
        }
    }
}
