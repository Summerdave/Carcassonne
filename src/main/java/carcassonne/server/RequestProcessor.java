package carcassonne.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import carcassonne.client.MeeplePlacedRequest;
import carcassonne.client.PlacingSkippedRequest;
import carcassonne.client.Request;
import carcassonne.client.TilePlacedRequest;
import carcassonne.model.Round;
import carcassonne.model.grid.Grid;
import carcassonne.settings.GameSettings;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class RequestProcessor implements Runnable {

    private final Request request;
    private final ObjectOutputStream outputStream;
    private List<SubscribedClient> subscribers;

    public RequestProcessor(final Request request, final ObjectOutputStream outputStream,
            final List<SubscribedClient> subscribers) {
        this.request = request;
        this.outputStream = outputStream;
        this.subscribers = subscribers;
    }

    @Override
    public void run() {
        this.processRequest();
    }

    private void processRequest() {
        final ResponseCode code;
        switch (request.getType()) {
        case PLACE_TILE:
            code = processTilePlacedRequest((TilePlacedRequest) request);
            break;
        case GAME_START:
            code = processGameStartRequest();
            break;
        case PLACE_MEEPLE:
            code = processMeeplePlacedRequest((MeeplePlacedRequest) request);
            break;
        case PLACING_SKIPPED:
            code = processPlacingSkipped((PlacingSkippedRequest) request);
            break;
        default:
            throw new NotImplementedException();
        }
        final Response response = new Response(code);
        sendResponse(response);
    }

    private ResponseCode processGameStartRequest() {
        final int boardWidth = GameSettings.BOARD_WIDTH;
        final int boardHeight = GameSettings.BOARD_HEIGHT;
        final int numPlayers = subscribers.size();
        final Grid grid = new Grid(boardWidth, boardHeight);
        final Round round = new Round(numPlayers, grid, new GameSettings());
        final List<BroadcastMessage> messages = new ArrayList<>(subscribers.size());
        for (int i = 0; i < numPlayers; i++) {
            final BroadcastMessage message = new GameStartMessage(-1, boardWidth, boardHeight, round, i);
            messages.add(message);
        }
        informSubscribers(messages);
        return ResponseCode.OK;
    }

    private ResponseCode processTilePlacedRequest(final TilePlacedRequest request) {
        final BroadcastMessage broadcast = new TilePlacedMessage(request.getPlayer(), request.getTilePlaced());
        informSubscribers(broadcast);
        return ResponseCode.OK;
    }

    private ResponseCode processMeeplePlacedRequest(MeeplePlacedRequest request) {
        final BroadcastMessage broadcast = new MeeplePlacedMessage(request.getPlayer(), request.getSpot(),
                request.getDirection());
        informSubscribers(broadcast);
        return ResponseCode.OK;
    }

    private ResponseCode processPlacingSkipped(final PlacingSkippedRequest request) {
        final BroadcastMessage broadcast = new PlacingSkippedMessage(request.getPlayer());
        informSubscribers(broadcast);
        return ResponseCode.OK;
    }

    private void informSubscribers(final List<BroadcastMessage> messages) {
        final Iterator<SubscribedClient> subscriberIterator = subscribers.iterator();
        final Iterator<BroadcastMessage> messageIterator = messages.iterator();
        while (subscriberIterator.hasNext()) {
            final SubscribedClient client = subscriberIterator.next();
            final BroadcastMessage message = messageIterator.next();
            try {
                client.publish(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void informSubscribers(final BroadcastMessage message) {
        for (final SubscribedClient client : subscribers) {
            try {
                client.publish(message);
            } catch (final IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendResponse(final Response response) {
        try {
            outputStream.writeObject(response);
        } catch (IOException e) {
            System.err.println("Error while sending response:");
            e.printStackTrace();
        }
    }

}
