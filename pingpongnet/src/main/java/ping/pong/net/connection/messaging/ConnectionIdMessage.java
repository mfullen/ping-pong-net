package ping.pong.net.connection.messaging;

public final class ConnectionIdMessage implements AbstractMessage
{
    private static final long serialVersionUID = -5530776801634240701L;

    public static class RequestMessage implements AbstractMessage
    {
        private static final long serialVersionUID = -8137880902144409573L;
    }

    public static class ResponseMessage implements AbstractMessage
    {
        private static final long serialVersionUID = -6001062890618431909L;
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
