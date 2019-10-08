package systems.reformcloud.reformcloud2.executor.controller.packet.in.query;

import systems.reformcloud.reformcloud2.executor.api.common.ExecutorAPI;
import systems.reformcloud.reformcloud2.executor.api.common.api.basic.ExternalAPIImplementation;
import systems.reformcloud.reformcloud2.executor.api.common.configuration.JsonConfiguration;
import systems.reformcloud.reformcloud2.executor.api.common.network.channel.PacketSender;
import systems.reformcloud.reformcloud2.executor.api.common.network.channel.handler.NetworkHandler;
import systems.reformcloud.reformcloud2.executor.api.common.network.packet.DefaultPacket;
import systems.reformcloud.reformcloud2.executor.api.common.network.packet.Packet;
import systems.reformcloud.reformcloud2.executor.api.common.process.ProcessInformation;

import java.util.UUID;
import java.util.function.Consumer;

public final class ControllerQueryGetProcess implements NetworkHandler {

    @Override
    public int getHandlingPacketID() {
        return ExternalAPIImplementation.EXTERNAL_PACKET_ID + 33;
    }

    @Override
    public void handlePacket(PacketSender packetSender, Packet packet, Consumer<Packet> responses) {
        ProcessInformation result;

        if (packet.content().has("name")) {
            String name = packet.content().getString("name");
            result = ExecutorAPI.getInstance().getProcess(name);
        } else {
            UUID uniqueID = packet.content().get("uniqueID", UUID.class);
            result = ExecutorAPI.getInstance().getProcess(uniqueID);
        }

        responses.accept(new DefaultPacket(-1, new JsonConfiguration().add("result", result)));
    }
}