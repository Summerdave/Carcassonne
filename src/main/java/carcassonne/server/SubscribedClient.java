package carcassonne.server;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class SubscribedClient {
    private ObjectOutputStream outputStream;

    public SubscribedClient(final ObjectOutputStream outputStream, final int subscribedPlayers) {
        this.outputStream = outputStream;
        try {
            publish(new WelcomeMessage(subscribedPlayers + 1));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void publish(final BroadcastMessage message) throws IOException {
        outputStream.writeObject(message);
    }

}
