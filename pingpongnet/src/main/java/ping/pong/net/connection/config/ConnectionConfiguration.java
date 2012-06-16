package ping.pong.net.connection.config;

/**
 *
 * @author mfullen
 */
public interface ConnectionConfiguration
{
    /**
     * Default Keystore name for a key on the classpath
     */
    String DEFAULT_KEY_STORE = "key.ks";
    /**
     * Default password for the keystore
     */
    String DEFAULT_KEY_STORE_PASSWORD = "pingpong123";

    /**
     * Get the port the TCP connection uses
     * @return
     */
    int getPort();

    /**
     * Set the port the TCP connection will use
     * @param port
     */
    void setPort(int port);

    /**
     * Get the port the UDP connection uses
     * @return
     */
    int getUdpPort();

    /**
     * Set the port the UDP connection will use
     * @param port
     */
    void setUdpPort(int udpport);

    /**
     * Get the Ip Address
     * @return
     */
    String getIpAddress();

    /**
     * Set the Ip Address
     * @param ipAddress
     */
    void setIpAddress(String ipAddress);

    /**
     * Returns true if ssl is enabled false if not
     * @return
     */
    boolean isSsl();

    /**
     * Sets SSL security
     * @param sslEnabled
     */
    void setSsl(boolean sslEnabled);

    /**
     * Gets the path of the currently set Keystore. If a custom keystore isn't set
     * a default keystore is used. It is highly recommended you provide a keystore
     * @return
     */
    String getKeystorePath();

    /**
     * Sets the path of the Keystore. If a custom keystore isn't set
     * a default keystore is used. It is highly recommended you provide a keystore
     * @param keystore
     */
    void setKeystorePath(String keystore);

    /**
     * Gets the password for the keystore
     * @return
     */
    String getKeystorePassword();

    /**
     * Sets the password to use on the keystore
     * @param password
     */
    void setKeystorePassword(String password);
}
