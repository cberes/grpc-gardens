package com.spinthechoice.grpc;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GardensClient implements AutoCloseable {
    private final ManagedChannel channel;
    private final GardensGrpc.GardensBlockingStub stub;

    public GardensClient(final String hostname, final int port) {
        channel = ManagedChannelBuilder
                .forAddress(hostname, port)
                .usePlaintext()
                .build();

        // TODO can I reuse the stub?
        stub = GardensGrpc.newBlockingStub(channel);
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

    @Override
    public void close() {
        channel.shutdown();
    }
}
