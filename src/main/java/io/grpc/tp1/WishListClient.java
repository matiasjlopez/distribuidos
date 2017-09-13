package io.grpc.tp1;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Mati on 9/7/2017.
 */

public class WishListClient {
    private static final Logger logger = Logger.getLogger(WishListClient.class.getName());

    private final ManagedChannel channel;
    private final WishListServiceGrpc.WishListServiceBlockingStub blockingStub;
    private final WishListServiceGrpc.WishListServiceStub asyncStub;

    /** Construct client for accessing RouteGuide server at {@code host:port}. */
    public WishListClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(true));
    }

    /** Construct client for accessing RouteGuide server using the existing channel. */
    public WishListClient(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();
        blockingStub = WishListServiceGrpc.newBlockingStub(channel);
        asyncStub = WishListServiceGrpc.newStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void addElement(String user, String element) {
        UserElement userElement = UserElement.newBuilder()
                                    .setUser(User.newBuilder().setUser(user))
                                    .setElement(Element.newBuilder().setName(element))
                                    .build();

        WishList wishList;

        try {
            wishList = blockingStub.addElement(userElement);
//            logger.info("Wishlist from user: " + user);
            System.out.println("Wishlist from user: " + user);
//            logger.info(" ");
//            logger.info(String.valueOf(wishList));
            System.out.println(String.valueOf(wishList));
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        }
    }

    public static void main(String[] args) throws Exception {
        WishListClient client = new WishListClient("localhost", 50051);
        try {
      /* Access a service running on the local machine on port 50051 */
            switch (args[0]) {
                case "addElement":
                    client.addElement(args[1], args[2]);
                    break;
            }
        } finally {
            client.shutdown();
        }
    }
}
