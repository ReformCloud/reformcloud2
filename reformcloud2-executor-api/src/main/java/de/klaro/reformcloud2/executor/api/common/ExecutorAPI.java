package de.klaro.reformcloud2.executor.api.common;

import de.klaro.reformcloud2.executor.api.ExecutorType;
import de.klaro.reformcloud2.executor.api.common.api.applications.ApplicationAsyncAPI;
import de.klaro.reformcloud2.executor.api.common.api.console.ConsoleAsyncAPI;
import de.klaro.reformcloud2.executor.api.common.api.group.GroupAsyncAPI;
import de.klaro.reformcloud2.executor.api.common.api.player.PlayerAsyncAPI;
import de.klaro.reformcloud2.executor.api.common.api.plugins.PluginAsyncAPI;
import de.klaro.reformcloud2.executor.api.common.api.process.ProcessAsyncAPI;
import de.klaro.reformcloud2.executor.api.common.base.Conditions;

import java.util.Objects;

public abstract class ExecutorAPI implements
        ProcessAsyncAPI,
        GroupAsyncAPI,
        ApplicationAsyncAPI,
        ConsoleAsyncAPI,
        PlayerAsyncAPI,
        PluginAsyncAPI {

    protected ExecutorType type;

    /* ========================== */

    private static ExecutorAPI instance;

    public static void setInstance(ExecutorAPI instance) {
        Conditions.isTrue(ExecutorAPI.instance == null, "Executor api instance is already defined");
        ExecutorAPI.instance = Objects.requireNonNull(instance);
    }

    public static ExecutorAPI getInstance() {
        return instance;
    }

    /* ========================== */

    public ExecutorType getType() {
        return type;
    }
}
