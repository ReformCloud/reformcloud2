package de.klaro.reformcloud2.executor.api.common.api.basic.events;

import de.klaro.reformcloud2.executor.api.common.event.Event;
import de.klaro.reformcloud2.executor.api.common.process.ProcessInformation;

public final class ProcessStoppedEvent extends Event {

    public ProcessStoppedEvent(ProcessInformation processInformation) {
        this.processInformation = processInformation;
    }

    private final ProcessInformation processInformation;

    public ProcessInformation getProcessInformation() {
        return processInformation;
    }
}
