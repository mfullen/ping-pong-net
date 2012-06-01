package ping.pong.net.server;

import java.net.DatagramSocket;
import java.net.Socket;
import ping.pong.net.connection.Connection;
import ping.pong.net.connection.ConnectionConfiguration;

/**
 *
 * @author mfullen
 */
public class DefaultIoServerConnection<Message> implements Connection<Message>
{
    protected DatagramSocket udpSocket = null;
    protected Socket tcpSocket = null;
    protected ConnectionConfiguration config = null;
  

    private DefaultIoServerConnection()
    {
    }

    public DefaultIoServerConnection(ConnectionConfiguration config, Socket tcpSocket, DatagramSocket udpSocket)
    {
        this.config = config;
        this.tcpSocket = tcpSocket;
        this.udpSocket = udpSocket;
    }

    @Override
    public void close()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isConnected()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getConnectionID()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Server<Message> getServer()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sendMessage(Message message)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void run()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
