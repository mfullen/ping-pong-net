package ping.pong.net.connection;

/**
 *
 * @author mfullen
 */
public final class DataFactory
{
    private DataFactory()
    {
    }

    public static DataReader createDataReader(boolean usePPNSerialization)
    {
        return usePPNSerialization ? new ReadObjectDataReader() : new ReadFullyDataReader();
    }

    public static DataWriter createDataWriter(boolean usePPNSerialization)
    {
        return usePPNSerialization ? new WriteObjectDataWriter() : new WriteByteArrayDataWriter();
    }
}
