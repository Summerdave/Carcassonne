package carcassonne.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Optional;
import java.util.function.Consumer;

import carcassonne.model.Player;
import carcassonne.model.grid.GridDirection;
import carcassonne.model.grid.GridSpot;
import carcassonne.server.BroadcastMessage;
import carcassonne.server.ConnectionType;
import carcassonne.server.Response;
import carcassonne.server.ResponseCode;
import carcassonne.settings.GameSettings;

public class Client {
    private final Socket requestSocket;
    private final Socket subscribeSocket;
    private String serverName;

    public String getServerName() {
        return serverName;
    }

    private int serverPort;
    private ObjectOutputStream requestStream;
    private ObjectInputStream responseStream;
    private ObjectInputStream subscribeStream;
    private final Consumer<BroadcastMessage> subscriber;

    public Client(final String server, final Consumer<BroadcastMessage> subscriber) {
        this(server, GameSettings.SERVER_PORT, subscriber);
    }

    public Client(String server, int serverPort, Consumer<BroadcastMessage> subscriber) {
        this.requestSocket = new Socket();
        this.subscribeSocket = new Socket();
        this.serverName = server;
        this.serverPort = serverPort;
        this.subscriber = subscriber;
    }

    public void connect() throws IOException {
        if (!requestReady()) {
            requestStream = prepareConnection(requestSocket, ConnectionType.REQUEST_RESPONSE);
        } else {
            requestStream = new ObjectOutputStream(requestSocket.getOutputStream());
        }
        responseStream = new ObjectInputStream(requestSocket.getInputStream());
        if (!subscribeReady()) {
            prepareConnection(subscribeSocket, ConnectionType.PUB_SUB);
        }
        subscribeStream = new ObjectInputStream(subscribeSocket.getInputStream());
        subscribe();
    }

    private boolean subscribeReady() {
        return subscribeSocket.isConnected() && !subscribeSocket.isClosed();
    }

    private boolean requestReady() {
        return requestSocket.isConnected() && !requestSocket.isClosed();
    }

    private ObjectOutputStream prepareConnection(final Socket socket, final ConnectionType type) throws IOException {
        socket.connect(new InetSocketAddress(serverName, serverPort));
        final ObjectOutputStream initialWrite = new ObjectOutputStream(socket.getOutputStream());
        initialWrite.writeObject(new ConnectionTypeRequest(type));
        return initialWrite;
    }

    public boolean sendTilePlaced(final GridSpot spot, final Player activePlayer) {
        final TilePlacedRequest request = new TilePlacedRequest(spot, activePlayer);
        sendRequest(request);
        final Optional<Response> response = receiveResponse();
        return response.isPresent() && response.get().getCode() == ResponseCode.OK;
    }

    public boolean sendMeeplePlaced(final GridSpot spot, final Player activePlayer, final GridDirection direction) {
        final MeeplePlacedRequest request = new MeeplePlacedRequest(activePlayer, direction, spot);
        sendRequest(request);
        final Optional<Response> response = receiveResponse();
        return response.isPresent() && response.get().getCode() == ResponseCode.OK;
    }

    public boolean sendPlacingSkipped(final Player activePlayer) {
        final PlacingSkippedRequest request = new PlacingSkippedRequest(activePlayer);
        sendRequest(request);
        final Optional<Response> response = receiveResponse();
        return response.isPresent() && response.get().getCode() == ResponseCode.OK;
    }

    public void subscribe() {
        new Thread(() -> {
            while (true) {
                final Optional<BroadcastMessage> response = receiveBroadcast();
                if (response.isPresent()) {
                    consumeMessage(response.get());
                }
            }
        }).start();
    }

    private void sendRequest(final Request request) {
        try {
            requestStream.writeObject(request);
        } catch (final IOException ex) {
            handleError("Error while sending request:", ex);
        }
    }

    private Optional<Response> receiveResponse() {
        try {
            final Response response = (Response) responseStream.readObject();
            return Optional.of(response);
        } catch (final Exception ex) {
            handleError("Error while receiving response: ", ex);
        }
        return Optional.empty();
    }

    private Optional<BroadcastMessage> receiveBroadcast() {
        try {
            final BroadcastMessage message = (BroadcastMessage) subscribeStream.readObject();
            return Optional.of(message);
        } catch (final Exception ex) {
            handleError("Error while receiving broadcast message", ex);
        }
        return Optional.empty();
    }

    private void consumeMessage(BroadcastMessage broadcastMessage) {
        this.subscriber.accept(broadcastMessage);
    }

    private void handleError(final String message, final Exception e) {
        System.err.println(message);
        e.printStackTrace();
        try {
            Thread.sleep(GameSettings.RETRY_TIME);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    public Response requestGameStart() {
        final GameStartRequest request = new GameStartRequest();
        sendRequest(request);
        final Optional<Response> response = receiveResponse();
        return response.get();
    }

    public void setServerName(final String serverName) {
        this.serverName = serverName;
    }

}
