/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ping.pong.net.connection.messaging;

import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Adrian
 */
public class EnvelopeFactoryTest
{

    /**
     * Test of createTcpEnvelope method, of class EnvelopeFactory.
     */
    @Test
    public void testCreateTcpEnvelope()
    {
        assertEquals(true, EnvelopeFactory.createTcpEnvelope("testMsg").isReliable());
    }

    /**
     * Test of createUdpEnvelope method, of class EnvelopeFactory.
     */
    @Test
    public void testCreateUdpEnvelope()
    {
        assertEquals(false, EnvelopeFactory.createUdpEnvelope("testMsg").isReliable());
    }
}
