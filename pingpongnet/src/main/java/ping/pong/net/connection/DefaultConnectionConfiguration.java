package ping.pong.net.connection;

/**
 *
 * @author mfullen
 */
class DefaultConnectionConfiguration implements ConnectionConfiguration
{
    DefaultConnectionConfiguration()
    {
        this(5011, 5012, "localhost", false);
    }

    DefaultConnectionConfiguration(int port, int udpPort, String ipAddress, boolean ssl)
    {
        this.port = port;
        this.udpPort = udpPort;
        this.ipAddress = ipAddress;
        this.ssl = ssl;
    }
    private int port;
    private int udpPort;
    private String ipAddress;
    private boolean ssl;

    @Override
    public String getIpAddress()
    {
        return ipAddress;
    }

    @Override
    public void setIpAddress(String ipAddress)
    {
        this.ipAddress = ipAddress;
    }

    @Override
    public int getPort()
    {
        return port;
    }

    @Override
    public void setPort(int port)
    {
        this.port = port;
    }

    @Override
    public int getUdpPort()
    {
        return udpPort;
    }

    @Override
    public void setUdpPort(int udpPort)
    {
        this.udpPort = udpPort;
    }

    @Override
    public boolean isSsl()
    {
        return ssl;
    }

    @Override
    public void setSsl(boolean ssl)
    {
        this.ssl = ssl;
    }
}
