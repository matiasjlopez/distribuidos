package io.grpc.tp1;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Mati on 9/2/2017.
 */

public class WishListServer {
    private static final Logger logger = Logger.getLogger(WishListServer.class.getName());

    private final int port;
    private Server server;

    public WishListServer(int port) {
        this.port = port;
        server = ServerBuilder.forPort(port).addService(new WishListService())
                .build();
    }

    private void start() throws IOException {
        server.start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may has been reset by its JVM shutdown hook.
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            WishListServer.this.stop();
            System.err.println("*** server shut down");
        }));
    }

    /** Stop serving requests and shutdown resources. */
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    private static class WishListService extends WishListServiceGrpc.WishListServiceImplBase {

        private Map<User, WishList> wishLists;

        WishListService() {
            this.wishLists = new HashMap<>();
            User user = User.newBuilder().setUser("mlopez").build();
            this.wishLists.put(user, WishList.newBuilder().addElement(Element.newBuilder().setName("PELOTA")).build());
        }

        @Override
        public void addElement(UserElement request, StreamObserver<WishList> responseObserver) {
            User user = request.getUser();
            WishList wishList;
            if(this.wishLists.containsKey(user)) {
                wishList = this.wishLists.get(user);
                List<Element> elementList = new ArrayList<>(wishList.getElementList());
                elementList.add(request.getElement());
                wishList = WishList.newBuilder().addAllElement(elementList).build();
                this.wishLists.put(user, wishList);
                responseObserver.onNext(wishList);
            } else {
                responseObserver.onNext(WishList.newBuilder()
                        .addElement(Element.newBuilder().setName(request.getElement().getName()))
                        .build());
            }

            responseObserver.onCompleted();
        }

        @Override
        public void getWishList(User request, StreamObserver<WishList> responseObserver) {

        }

        @Override
        public void deleteElement(UserElement request, StreamObserver<WishList> responseObserver) {

        }

    }

    public static void main(String[] args) throws IOException, InterruptedException {

        final WishListServer server = new WishListServer(50051);
        server.start();
        server.blockUntilShutdown();
    }
}
