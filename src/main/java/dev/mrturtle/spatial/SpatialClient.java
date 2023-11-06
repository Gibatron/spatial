package dev.mrturtle.spatial;

import dev.mrturtle.spatial.inventory.InventoryShape;
import dev.mrturtle.spatial.networking.SpatialNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashMap;

public class SpatialClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(SpatialNetworking.SYNC_SHAPES_PACKET_ID, (client, handler, buf, responseSender) -> {
            HashMap<Identifier, InventoryShape> shapes = (HashMap<Identifier, InventoryShape>) buf.readMap(PacketByteBuf::readIdentifier, InventoryShape::readFrom);
            Spatial.setShapes(shapes);
            Spatial.LOGGER.info("Received shape sync packet from server, loaded {} shapes", shapes.size());
        });
    }
}
