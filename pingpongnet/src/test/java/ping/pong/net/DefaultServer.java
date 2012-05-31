/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ping.pong.net;

import java.util.Collection;
import ping.pong.net.connection.Connection;
import ping.pong.net.connection.MessageListener;
import ping.pong.net.server.Server;
import ping.pong.net.server.ServerConnectionListener;

/**
 *
 * @author Adrian
 */
class DefaultServer implements Server
{
    private boolean running;

    public DefaultServer()
    {
    }

    @Override
    public void broadcast(Object message)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void start()
    {
        this.running = true;
    }

    @Override
    public void close()
    {
        this.running = false;
    }

    @Override
    public Connection getConnection(int id)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection getConnections()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasConnections()
    {
        return false;
    }

    @Override
    public boolean isRunning()
    {
        return this.running;
    }

    @Override
    public void addMessageListener(MessageListener listener)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeMessageListener(MessageListener listener)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addConnectionListener(ServerConnectionListener connectionListener)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeConnectionListener(ServerConnectionListener connectionListener)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
