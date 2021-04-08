package com.spinthechoice.grpc;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GardensTest {
    @Test void everything() throws Exception {
        final int port = 8080;
        try (GardensServer server = new GardensServer(port);
             GardensClient client = new GardensClient("localhost", port)) {
            server.start();

            final String coneflowerId = client.addPlant(GardensOuterClass.Plant.newBuilder()
                    .setSpecies("Purple Coneflower")
                    .setGarden("Front")
                    .setLight(GardensOuterClass.LightPreference.FULL)
                    .setMoisture(GardensOuterClass.MoisturePreference.MEDIUM)
                    .build());

            final GardensOuterClass.Plant coneflower = client.getPlant(coneflowerId);
            assertNotNull(coneflower);
            assertEquals(coneflowerId, coneflower.getId());
            assertEquals("Purple Coneflower", coneflower.getSpecies());
            assertEquals("Front", coneflower.getGarden());
            assertEquals(GardensOuterClass.LightPreference.FULL, coneflower.getLight());
            assertEquals(GardensOuterClass.MoisturePreference.MEDIUM, coneflower.getMoisture());
            assertNotEquals(0L, coneflower.getPlanted());
            assertEquals(0, coneflower.getWaterLevel());

            client.addPlant(GardensOuterClass.Plant.newBuilder()
                    .setSpecies("Early Sunflower")
                    .setGarden("Side")
                    .setLight(GardensOuterClass.LightPreference.FULL)
                    .setMoisture(GardensOuterClass.MoisturePreference.MEDIUM)
                    .build());

            client.addPlant(GardensOuterClass.Plant.newBuilder()
                    .setSpecies("Wild Bergamot")
                    .setGarden("Front")
                    .setLight(GardensOuterClass.LightPreference.FULL)
                    .setMoisture(GardensOuterClass.MoisturePreference.MEDIUM)
                    .build());

            final List<GardensOuterClass.Plant> frontPlants = client.getPlantInGarden("Front");
            assertEquals(2, frontPlants.size());

            final Set<String> names = frontPlants.stream()
                    .map(GardensOuterClass.Plant::getSpecies)
                    .collect(toSet());
            assertTrue(names.contains("Purple Coneflower"));
            assertTrue(names.contains("Wild Bergamot"));

            final String waterResponse = client.water(frontPlants.stream()
                    .map(GardensOuterClass.Plant::getId)
                    .collect(toList()));
            assertEquals("Watered 2 plants", waterResponse);

            final GardensOuterClass.Plant watered = client.getPlant(coneflowerId);
            assertNotNull(watered);
            assertEquals(1, watered.getWaterLevel());
        }
    }
}
