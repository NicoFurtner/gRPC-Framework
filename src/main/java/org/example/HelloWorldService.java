package org.example;

import io.grpc.stub.StreamObserver;

public class HelloWorldService extends HelloWorldServiceGrpc.HelloWorldServiceImplBase {

    @Override
    public void hello(Hello.HelloRequest request, StreamObserver<Hello.HelloResponse> responseObserver) {
        System.out.println("Handling hello endpoint: " + request.toString());

        String text = "Hello World, " + request.getFirstname() + " " + request.getLastname();
        Hello.HelloResponse response = Hello.HelloResponse.newBuilder().setText(text).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}