package de.klaro.reformcloud2.executor.api.common.commands.basic.command.sources;

import de.klaro.reformcloud2.executor.api.common.commands.AllowedCommandSources;
import de.klaro.reformcloud2.executor.api.common.commands.basic.GlobalCommand;

import java.util.List;

public abstract class ApiCommand extends GlobalCommand {

    public ApiCommand(String command, String permission, String description, List<String> aliases) {
        super(command, permission, description, aliases);
    }

    public ApiCommand(String command, String description, List<String> aliases) {
        super(command, description, aliases);
    }

    public ApiCommand(String command, String description) {
        super(command, description);
    }

    public ApiCommand(String command) {
        super(command);
    }

    @Override
    public AllowedCommandSources sources() {
        return AllowedCommandSources.API;
    }
}