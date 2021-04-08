package com.spinthechoice.grpc;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import static java.util.Arrays.asList;

public class GardensClient implements AutoCloseable {
    private final ManagedChannel channel;
    private final GardensGrpc.GardensBlockingStub stub;
    private final GardensGrpc.GardensStub asyncStub;

    public GardensClient(final String hostname, final int port) {
        channel = ManagedChannelBuilder
                .forAddress(hostname, port)
                .usePlaintext()
                .build();

        // TODO can I reuse the stub?
        stub = GardensGrpc.newBlockingStub(channel);
        asyncStub = GardensGrpc.newStub(channel);
    }

    public String addPlant(final GardensOuterClass.Plant request) {
        final StringValue response = stub.addPlant(request);
        return response.getValue();
    }

    public GardensOuterClass.Plant getPlant(final String id) {
        final StringValue request = StringValue.newBuilder().setValue(id).build();
        return stub.getPlant(request);
    }

    public List<GardensOuterClass.Plant> getPlantInGarden(final String garden) {
        final StringValue request = StringValue.newBuilder().setValue(garden).build();
        final Iterator<GardensOuterClass.Plant> iter = stub.getPlantsInGarden(request);
        return toList(iter);
    }

    private static <T> List<T> toList(final Iterator<T> iter) {
        final List<T> list = new LinkedList<>();
        while (iter.hasNext()) {
            list.add(iter.next());
        }
        return list;
    }

    public String water(final String... plantIds) {
        return water(asList(plantIds));
    }

    public String water(final Collection<String> plantIds) {
        // setup response stuff
        final AtomicReference<String> responseHolder = new AtomicReference<>();
        final CountDownLatch complete = new CountDownLatch(1);
        final StreamObserver<StringValue> responseObserver  = new StreamObserver<>() {
            @Override
            public void onNext(final StringValue value) {
                responseHolder.set(value.getValue());
            }

            @Override
            public void onError(final Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                complete.countDown();
            }
        };

        // send requests
        final StreamObserver<StringValue> requestObserver = asyncStub.water(responseObserver);
        plantIds.forEach(id -> requestObserver.onNext(StringValue.newBuilder().setValue(id).build()));
        requestObserver.onCompleted();

        // check for responses
        await(complete);
        return responseHolder.get();
    }

    private static void await(final CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() {
        channel.shutdown();
    }
}
