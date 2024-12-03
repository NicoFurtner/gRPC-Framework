package org.example;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class ServerClass {

    private static final int PORT = 50051;
    private Server server;

    public void start() throws IOException {
        server = ServerBuilder.forPort(PORT)
                .addService(new HelloWorldService())
                .build()
                .start();

        System.out.println("Server started, listening on port " + PORT);
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server == null) {
            return;
        }
        server.awaitTermination();
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        ServerClass server = new ServerClass();
        System.out.println("Server is running HelloWorld");
        server.start();
        server.blockUntilShutdown();
    }

}