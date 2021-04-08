package com.spinthechoice.grpc;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class GardensServer implements AutoCloseable {
    private final Server server;

    public GardensServer(final int port) {
        server = ServerBuilder.forPort(port)
                .addService(new GardensImpl())
                .build();
    }

    public void start() throws IOException {
        server.start();
    }

    @Override
    public void close() {
        server.shutdown();
    }

    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        return server.awaitTermination(timeout, unit);
    }

    public void awaitTermination() throws InterruptedException {
        server.awaitTermination();
    }

    public static void main(final String[] args) throws Exception {
        final int port = Integer.parseInt(args[0]);
        final GardensServer server = new GardensServer(port);
        server.start();

        System.out.println("Server started, listening on " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.close();
            System.out.println("Server shut down");
        }));

        server.awaitTermination();;
    }
}
