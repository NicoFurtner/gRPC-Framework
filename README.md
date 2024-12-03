# DEZSYS_GK72_ELECTION_GRPC
This lesson introduces the Remote Procedure Technology (RPC).

## Introduction
This exercise is intended to demonstrate the functionality and implementation of Remote Procedure Call (RPC) technology using the Open Source High Performance gRPC Framework gRPC Frameworks (https://grpc.io). It shows that this framework can be used to develop a middleware system for connecting several services developed with different programming languages.

Document all individual implementation steps and any problems that arise in a log (Markdown). Create a GITHUB repository for this project and add the link to it in the comments.

## Assessment
#### Requirements überwiegend erfüllt
Answer the questions below about the gRPC framework
Use one of the tutorials listed under "Links & Further Resources" to create a simple HelloWorld application using the gRPC framework
Create first the Proto-file where you define your gRPC Service and its data structures
Implement a gRPC server and gRPC client in a programming language of your choice
Document each single development step in your protocol and describe the most important code snippets in few sentences. Furthermore, the output of the application and any problems that occur should be documented in submission document.
#### Requirements zur Gänze erfüllt
Customize the service so that a simple ElectionData record can be transferred
Document which parts of the program need to be adapted


## Documentation
Create IntelliJ Project with Gradle and import the gRCP zip on Project-Structure → Librarys

Then edit the build.gradle.kts File with the dependencies:

implementation("io.grpc:grpc-netty:1.68.1")
implementation("io.grpc:grpc-protobuf:1.68.1")
implementation("io.grpc:grpc-stub:1.68.1")
implementation("io.grpc:grpc-api:1.68.1")
compileOnly("javax.annotation:javax.annotation-api:1.3.2")

with the plugin:

id("com.google.protobuf") version "0.9.4" 

and with sourceSets

```java
import com.google.protobuf.gradle.id

plugins {
    id("java")
    id("com.google.protobuf") version "0.9.4" 
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral() 
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("io.grpc:grpc-netty:1.68.1")
    implementation("io.grpc:grpc-protobuf:1.68.1")
    implementation("io.grpc:grpc-stub:1.68.1")
    implementation("io.grpc:grpc-api:1.68.1")
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")

}
sourceSets {
    main {
        java {
            srcDir("build/generated/source/proto/main/java")
            srcDir("build/generated/source/proto/main/grpc")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
```

and then “Load Gradle Changes” so after that we can add the protoBuf  to the Gradle File 

```jsx
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.5"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.68.1"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("grpc")
            }
        }
    }
}
```

for me it first didn’t work, so I had to import the protobuf.gradle.id library:

```jsx
import com.google.protobuf.gradle.id
```

next create a new directory “proto” in src and add file hello.proto, where we define the HelloWorldService and the Messeging Services “HelloRequest” and “HelloResponse”

![image.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/d396820d-0690-452d-9cc7-0534b54f3ef5/420803ac-1481-4290-ae83-34f8a1b7e628/image.png)

```java
syntax = "proto3";

option java_package = "org.example";
option java_outer_classname = "Hello";

service HelloWorldService {
  rpc hello (HelloRequest) returns (HelloResponse);
}

message HelloRequest {
  string firstname = 1;
  string lastname = 2;
}

message HelloResponse {
  string text = 1;
}
```

and now load gradle changes again so we can start 

now we created the 3 classes in directory main/java/org.example

### HelloWorldClient.java

connects to the server using ManagedChannel, calls the hello method, and sends a HelloRequest with the first and last name.

It then receives a HelloResponse containing the greeting text and prints it to the console.

The connection is closed after the response is received.

```java
package org.example;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class HelloWorldClient {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        HelloWorldServiceGrpc.HelloWorldServiceBlockingStub stub = HelloWorldServiceGrpc.newBlockingStub(channel);

        Hello.HelloResponse helloResponse = stub.hello(Hello.HelloRequest.newBuilder()
                .setFirstname("Nico")
                .setLastname("Furtner")
                .build());
        System.out.println( helloResponse.getText() );
        channel.shutdown();

    }

}
```

### ServerClass.java

implements the HelloWorldService by creating the class, which defines the hello method, there the server processes the request and generates a  message using the first and last name provided in the HelloRequest.

```java
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
        System.out.println("Server is running with both HelloWorld and Election services!");
        server.start();
        server.blockUntilShutdown();
    }

}
```

### HelloWorldService.java

contains Hello Method

```java
package org.example;

import io.grpc.stub.StreamObserver;

public class HelloWorldService extends HelloWorldServiceGrpc.HelloWorldServiceImplBase {

    @Override
    public void hello( Hello.HelloRequest request, StreamObserver<Hello.HelloResponse> responseObserver) {
        System.out.println("Handling hello endpoint: " + request.toString());

        String text = "Hello World, " + request.getFirstname() + " " + request.getLastname();
        Hello.HelloResponse response = Hello.HelloResponse.newBuilder().setText(text).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
```

before starting the server its important to build the gradle 

And now we start the server and the client so the name will be shown

We use Port 50051, because it is said so in the documentation and it’s the only supported for gRPC

![image.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/d396820d-0690-452d-9cc7-0534b54f3ef5/eaa9e557-69eb-4e89-a165-266dc0404674/image.png)

It is important to create the files and directorys in the right directory, that was my problem at first, because I created the proto directory in main, that didn’t work for me, so I created it in src, which worked afterwards. 

![image.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/d396820d-0690-452d-9cc7-0534b54f3ef5/5d2fbf64-cca3-4c47-8464-a575bff99bc4/image.png)

helpful source: https://intuting.medium.com/implement-grpc-service-using-java-gradle-7a54258b60b8

# Questions

### What is gRPC and Why Does It Work Across Languages and Platforms?

**gRPC** is a framework for making remote procedure calls (RPCs) between systems. It works across languages and platforms because it uses **Protocol Buffers (protobuf)** for a standard data format and provides libraries for many programming languages. It also uses HTTP/2, which is platform-agnostic and supports features like streaming.

---

### Describe the RPC Life Cycle Starting with the RPC Client

1. **Client Calls Stub**: The client app calls a method on the gRPC stub (proxy object).
2. **Request Sent**: The request is serialized and sent over HTTP/2 to the server.
3. **Server Processes**: The server deserializes the request, runs the service logic, and prepares a response.
4. **Response Sent**: The server serializes the response and sends it back to the client.
5. **Client Receives**: The client deserializes the response and processes the result.

---

### Describe the Workflow of Protocol Buffers

**Define Schema**: Write a `.proto` file with message structures and services.

**Generate Code**: Use `protoc` to create serialization and service code for your language.

**Use the Code**: Implement logic (server) and call methods (client).

**Serialize/Deserialize**: Data is serialized for transport and deserialized when received.

---

### What Are the Benefits of Using Protocol Buffers?

**s**mall binary format

many languages using the same `.proto` file.

allows schema updates without breaking existing systems.

---

### When Is the Use of Protocol Buffers Not Recommended?

When you need **human-readable data** (use JSON or XML instead).

For **simple projects** where plain text formats are easier.

In systems with **rapidly changing schemas**, which can complicate Protobuf management.

---

### List 3 Data Types for Protocol Buffers

Simple types: `int32`, `float`, `string`.

Custom types: Your own defined messages.

Repeated types: Lists of items.
