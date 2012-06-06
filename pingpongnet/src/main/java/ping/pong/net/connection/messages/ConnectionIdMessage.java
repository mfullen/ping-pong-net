package ping.pong.net.connection.messages;

public final class ConnectionIdMessage implements AbstractMessage
{
    public static class RequestMessage implements AbstractMessage
    {
    }

    public static class ResponseMessage implements AbstractMessage
    {
        private int id = -1;

        public ResponseMessage()
        {
        }

        public ResponseMessage(int id)
        {
            this.id = id;
        }

        public int getId()
        {
            return id;
        }
    }
}
