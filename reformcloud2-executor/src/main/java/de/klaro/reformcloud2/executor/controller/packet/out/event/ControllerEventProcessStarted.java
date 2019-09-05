package de.klaro.reformcloud2.executor.controller.packet.out.event;

import de.klaro.reformcloud2.executor.api.common.configuration.JsonConfiguration;
import de.klaro.reformcloud2.executor.api.common.network.NetworkUtil;
import de.klaro.reformcloud2.executor.api.common.network.packet.DefaultPacket;
import de.klaro.reformcloud2.executor.api.common.process.ProcessInformation;

public final class ControllerEventProcessStarted extends DefaultPacket {

    public ControllerEventProcessStarted(ProcessInformation processInformation) {
        super(NetworkUtil.EVENT_BUS + 2, new JsonConfiguration().add("info", processInformation));
    }
}
