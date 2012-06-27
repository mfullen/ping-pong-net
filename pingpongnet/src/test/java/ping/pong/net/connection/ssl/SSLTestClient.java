package ping.pong.net.connection.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.*;
import ping.pong.net.connection.config.ConnectionConfiguration;

/**
 *
 * @author mfullen
 */
public class SSLTestClient
{
    public static void main(String[] args)
    {
        clientWithKeyStores();
        clientWithoutKeyStore();
    }

    public static void clientWithoutKeyStore()
    {
        try
        {
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket clientSocket = (SSLSocket) factory.createSocket("localhost", 5011);
            clientSocket.setSoTimeout(5000);
            clientSocket.addHandshakeCompletedListener(new HandshakeCompletedListener()
            {
                @Override
                public void handshakeCompleted(HandshakeCompletedEvent hce)
                {
                    System.out.println("Handshake complete");
                }
            });
            clientSocket.startHandshake();




        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void clientWithKeyStores()
    {
        try
        {
            SSLContext ctx = SSLContext.getInstance("SSLv3");
            ctx.getClientSessionContext().setSessionTimeout(5);
            TrustManager[] trustManagers = getTrustManagers("jks", new FileInputStream(getPath(ConnectionConfiguration.DEFAULT_KEY_STORE)), ConnectionConfiguration.DEFAULT_KEY_STORE_PASSWORD);
            KeyManager[] keyManagers = getKeyManagers("jks", new FileInputStream(getPath(ConnectionConfiguration.DEFAULT_KEY_STORE)), ConnectionConfiguration.DEFAULT_KEY_STORE_PASSWORD);
            ctx.init(keyManagers, trustManagers, new SecureRandom());
            SSLSocketFactory factory = ctx.getSocketFactory();
            SSLSocket clientSocket = (SSLSocket) factory.createSocket("localhost", 5011);
            clientSocket.setSoTimeout(5000);
            clientSocket.addHandshakeCompletedListener(new HandshakeCompletedListener()
            {
                @Override
                public void handshakeCompleted(HandshakeCompletedEvent hce)
                {
                    System.out.println("Handshake complete");
                }
            });
            clientSocket.startHandshake();




        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static String getPath(String filename)
    {
        String path = null;
        try
        {
            path = new File(Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).getAbsolutePath();
        }
        catch (URISyntaxException ex)
        {
            Logger.getLogger(SSLTestServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            return path;
        }
    }

    protected static KeyManager[] getKeyManagers(String keyStoreType, InputStream keyStoreFile, String keyStorePassword)
            throws Exception
    {
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(keyStoreFile, keyStorePassword.toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyStorePassword.toCharArray());
        return kmf.getKeyManagers();
    }

    protected static TrustManager[] getTrustManagers(String trustStoreType, InputStream trustStoreFile, String trustStorePassword)
            throws Exception
    {
        KeyStore trustStore = KeyStore.getInstance(trustStoreType);
        trustStore.load(trustStoreFile, trustStorePassword.toCharArray());
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        return tmf.getTrustManagers();
    }
}
