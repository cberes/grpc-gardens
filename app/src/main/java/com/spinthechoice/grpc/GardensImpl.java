package com.spinthechoice.grpc;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Override
    public StreamObserver<StringValue> water(final StreamObserver<StringValue> responseObserver) {
        final AtomicInteger count = new AtomicInteger(0);
        return new StreamObserver<>() {
            @Override
            public void onNext(final StringValue value) {
                final String id = value.getValue();
                if (plants.containsKey(id)) {
                    final GardensOuterClass.Plant plant = plants.get(id);
                    plants.put(plant.getId(), plant.toBuilder()
                            .setWaterLevel(plant.getWaterLevel() + 1) // TODO periodically decrease water level
                            .build());
                    count.incrementAndGet();
                }
            }

            @Override
            public void onError(final Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                final StringValue response = StringValue.newBuilder().setValue("Watered " + count.get() + " plants").build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }
}
