package carcassonne.server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import carcassonne.client.ConnectionTypeRequest;
import carcassonne.client.Request;
import carcassonne.settings.GameSettings;

public class Server {
    private int port;
    private final ThreadPoolExecutor executor;
    private Thread listenThread;
    private List<Thread> connectionThreads;
    private ServerSocket socket;
    private boolean running;
    private List<SubscribedClient> subscribers;

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    public Server() {
        this(GameSettings.SERVER_PORT);
    }

    public Server(final int port) {
        this.connectionThreads = new ArrayList<>(GameSettings.MAXIMAL_PLAYERS);
        this.subscribers = new ArrayList<>(GameSettings.MAXIMAL_PLAYERS);
        this.port = port;
        this.executor = new ThreadPoolExecutor(GameSettings.SERVER_CORE_POOL_SIZE, GameSettings.SERVER_MAX_POOL_SIZE,
                Long.MAX_VALUE, TimeUnit.NANOSECONDS, new ArrayBlockingQueue<Runnable>(GameSettings.SERVER_QUEUE_SIZE));
        try {
            socket = new ServerSocket();
        } catch (IOException e) {
            System.err.println("Error creating Server socket:");
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            socket.bind(new InetSocketAddress(this.port));
            running = true;
            listenThread = new Thread(this::listen);
            listenThread.start();
        } catch (IOException e) {
            System.err.println("Error starting Server:");
            e.printStackTrace();
        }
    }

    private void listen() {
        while (running) {
            try {
                final Socket connection = socket.accept();
                final Thread connectionThread = new Thread(() -> this.listenForRequest(connection));
                connectionThread.start();
                this.connectionThreads.add(connectionThread);
            } catch (IOException e) {
                System.err.println("Error accepting connection:");
                e.printStackTrace();
            }
        }
    }

    private void listenForRequest(final Socket channel) {
        try {
            final ObjectInputStream inputStream = new ObjectInputStream(channel.getInputStream());
            final ObjectOutputStream outputStream = new ObjectOutputStream(channel.getOutputStream());
            final ConnectionType connectionType = determineConnectionType(inputStream);
            if (connectionType == ConnectionType.REQUEST_RESPONSE) {
                while (running && channelReady(channel)) {
                    try {
                        final Request request = (Request) inputStream.readObject();
                        RequestProcessor processor = new RequestProcessor(request, outputStream, subscribers);
                        this.executor.execute(processor);
                    } catch (ClassNotFoundException e) {
                        handleError("Error while parsing request", e);
                    } catch (IOException e) {
                        if (e instanceof EOFException) {
                            System.err.println("Connection closed unexpectedly.");
                            break;
                        } else {
                            handleError("Error while receiving request", e);
                        }
                    }
                }
            } else if (connectionType == ConnectionType.PUB_SUB) {
                this.subscribers.add(new SubscribedClient(outputStream));
            }
        } catch (IOException e) {
            System.err.println("Error while reading from connection:");
            e.printStackTrace();
        }
    }

    private ConnectionType determineConnectionType(final ObjectInputStream inputStream) {
        try {
            final ConnectionTypeRequest request = (ConnectionTypeRequest) inputStream.readObject();
            return request.getType();
        } catch (Exception e) {
            System.err.println("Error determining connection type:");
            e.printStackTrace();
            return null;
        }
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

    private boolean channelReady(final Socket channel) {
        return channel.isConnected() && !channel.isClosed();
    }

}
