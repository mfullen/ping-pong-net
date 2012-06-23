/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ping.pong.net.connection.config;

import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Adrian
 */
public class ConnectionConfigFactoryTest
{

    /**
     * Test of createPPNConfig method, of class ConnectionConfigFactory.
     */
    @Test
    @Ignore // TODO: this test fails because the code looks for a keystore file in the classpath
    public void testCreatePPNConfig()
    {
        ConnectionConfiguration result = ConnectionConfigFactory.createPPNConfig("myip", 1, 2, true);
        assertEquals("myip", result.getIpAddress());
        assertEquals(1, result.getPort());
        assertEquals(2, result.getUdpPort());
        assertEquals(true, result.isSsl());
    }

    /**
     * Test of createPPNServerConfig method, of class ConnectionConfigFactory.
     */
    @Test
    @Ignore // TODO: this test fails because the code looks for a keystore file in the classpath
    public void testCreatePPNServerConfig_3args()
    {
        ConnectionConfiguration result = ConnectionConfigFactory.createPPNServerConfig(1, 2, true);
        assertEquals(1, result.getPort());
        assertEquals(2, result.getUdpPort());
        assertEquals(true, result.isSsl());
    }

    /**
     * Test of createPPNServerConfig method, of class ConnectionConfigFactory.
     */
    @Test
    @Ignore // TODO: this test fails because the code looks for a keystore file in the classpath
    public void testCreatePPNServerConfig_int_boolean()
    {
        ConnectionConfiguration result = ConnectionConfigFactory.createPPNServerConfig(1, true);
        assertEquals(ConnectionConfiguration.LOCAL_HOST, result.getIpAddress());
        assertEquals(1, result.getPort());
        assertEquals(ConnectionConfiguration.UDP_DISABLED, result.getUdpPort());
        assertEquals(true, result.isSsl());
    }

    /**
     * Test of createPPNServerConfig method, of class ConnectionConfigFactory.
     */
    @Test
    public void testCreatePPNServerConfig_int()
    {
        ConnectionConfiguration result = ConnectionConfigFactory.createPPNServerConfig(1);
        assertEquals(ConnectionConfiguration.LOCAL_HOST, result.getIpAddress());
        assertEquals(1, result.getPort());
        assertEquals(ConnectionConfiguration.UDP_DISABLED, result.getUdpPort());
        assertEquals(false, result.isSsl());
    }

    /**
     * Test of createPPNClientConfig method, of class ConnectionConfigFactory.
     */
    @Test
    @Ignore // TODO: this test fails because the code looks for a keystore file in the classpath
    public void testCreatePPNClientConfig_4args()
    {
        ConnectionConfiguration result = ConnectionConfigFactory.createPPNClientConfig("myip", 1, 2, true);
        assertEquals("myip", result.getIpAddress());
        assertEquals(1, result.getPort());
        assertEquals(2, result.getUdpPort());
        assertEquals(true, result.isSsl());
    }

    /**
     * Test of createPPNClientConfig method, of class ConnectionConfigFactory.
     */
    @Test
    @Ignore // TODO: this test fails because the code looks for a keystore file in the classpath
    public void testCreatePPNClientConfig_3args()
    {
        ConnectionConfiguration result = ConnectionConfigFactory.createPPNClientConfig("myip", 1, true);
        assertEquals("myip", result.getIpAddress());
        assertEquals(1, result.getPort());
        assertEquals(ConnectionConfiguration.UDP_DISABLED, result.getUdpPort());
        assertEquals(true, result.isSsl());
    }

    /**
     * Test of createPPNClientConfig method, of class ConnectionConfigFactory.
     */
    @Test
    public void testCreatePPNClientConfig_String_int()
    {
        ConnectionConfiguration result = ConnectionConfigFactory.createPPNClientConfig("myip", 1);
        assertEquals("myip", result.getIpAddress());
        assertEquals(1, result.getPort());
        assertEquals(ConnectionConfiguration.UDP_DISABLED, result.getUdpPort());
        assertEquals(false, result.isSsl());
    }

    /**
     * Test of createConnectionConfiguration method, of class ConnectionConfigFactory.
     */
    @Test
    public void testCreateConnectionConfiguration()
    {
        ConnectionConfiguration result = ConnectionConfigFactory.createConnectionConfiguration();
        assertEquals(ConnectionConfiguration.LOCAL_HOST, result.getIpAddress());
        assertEquals(ConnectionConfiguration.DEFAULT_TCP_PORT, result.getPort());
        assertEquals(ConnectionConfiguration.DEFAULT_UDP_PORT, result.getUdpPort());
        assertEquals(false, result.isSsl());
    }
}
