package ping.pong.net.connection;

import java.io.File;
import java.net.URISyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mfullen
 */
class DefaultConnectionConfiguration implements ConnectionConfiguration
{
    public static final Logger logger = LoggerFactory.getLogger(DefaultConnectionConfiguration.class);

    public static String findKeyStorePath(String filepath)
    {
        if (filepath == null)
        {
            return getKeystoreFromClassPath();
        }

        File file = new File(filepath);
        if (file.exists())
        {
            return file.getAbsolutePath();
        }

        return getKeystoreFromClassPath();
    }

    private static String getKeystoreFromClassPath()
    {
        String path = null;
        try
        {
            path = new File(Thread.currentThread().getContextClassLoader().getResource(DEFAULT_KEY_STORE).toURI()).getAbsolutePath();
            logger.trace("Couldn't find specified keystore, reverting to default {}", path);
        }
        catch (URISyntaxException ex)
        {
            logger.error("URI error.", ex);
        }
        return path;
    }

    DefaultConnectionConfiguration()
    {
        this(5011, 5012, "localhost", false);
    }

    DefaultConnectionConfiguration(int port, int udpPort, String ipAddress, boolean ssl)
    {
        this(port, udpPort, ipAddress, ssl, ssl ? findKeyStorePath(null) : null, DEFAULT_KEY_STORE_PASSWORD);
    }

    DefaultConnectionConfiguration(int port, int udpPort, String ipAddress, boolean ssl, String keystorePath, String keystorePassword)
    {
        this.port = port;
        this.udpPort = udpPort;
        this.ipAddress = ipAddress;
        this.ssl = ssl;
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
    }
    private int port;
    private int udpPort;
    private String ipAddress;
    private boolean ssl;
    private String keystorePath;
    private String keystorePassword;

    @Override
    public String getKeystorePassword()
    {
        return keystorePassword;
    }

    @Override
    public void setKeystorePassword(String keystorePassword)
    {
        this.keystorePassword = keystorePassword;
    }

    @Override
    public void setKeystorePath(String keystorePath)
    {
        this.keystorePath = keystorePath;
    }

    @Override
    public String getKeystorePath()
    {
        return keystorePath;
    }

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
