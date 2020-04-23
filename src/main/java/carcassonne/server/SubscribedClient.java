package carcassonne.server;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class SubscribedClient {
    private ObjectOutputStream outputStream;

    public SubscribedClient(final ObjectOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void publish(final BroadcastMessage message) throws IOException {
        outputStream.writeObject(message);
    }

}
