package com.spinthechoice.grpc;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.protobuf.StringValue;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;

public class GardensImpl extends GardensGrpc.GardensImplBase {
    private final Map<String, GardensOuterClass.Plant> plants = new HashMap<>();

    @Override
    public void addPlant(final GardensOuterClass.Plant request, final StreamObserver<StringValue> responseObserver) {
        final String id = newId();
        plants.put(id, request.toBuilder()
                .setId(id)
                .setPlanted(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .build());

        final StringValue response = StringValue.newBuilder().setValue(id).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    protected String newId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public void getPlant(final StringValue request, final StreamObserver<GardensOuterClass.Plant> responseObserver) {
        final String id = request.getValue();
        if (plants.containsKey(id)) {
            responseObserver.onNext(plants.get(id));
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(new StatusException(Status.NOT_FOUND));
        }
    }

    @Override
    public void getPlantsInGarden(final StringValue request, final StreamObserver<GardensOuterClass.Plant> responseObserver) {
        final String garden = request.getValue();
        plants.values().stream()
                .filter(plant -> garden.equals(plant.getGarden()))
                .forEach(responseObserver::onNext);
        responseObserver.onCompleted();
    }
}
