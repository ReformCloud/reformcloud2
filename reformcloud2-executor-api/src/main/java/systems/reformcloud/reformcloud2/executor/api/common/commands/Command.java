package systems.reformcloud.reformcloud2.executor.api.common.commands;

import com.google.gson.reflect.TypeToken;
import systems.reformcloud.reformcloud2.executor.api.common.commands.basic.GlobalCommand;
import systems.reformcloud.reformcloud2.executor.api.common.commands.complete.TabCompleter;
import systems.reformcloud.reformcloud2.executor.api.common.commands.permission.Permission;
import systems.reformcloud.reformcloud2.executor.api.common.commands.source.CommandSource;

import java.util.List;

public interface Command extends TabCompleter {

    TypeToken<GlobalCommand> TYPE = new TypeToken<GlobalCommand>() {};

    /**
     * @return The main command name
     */
    String mainCommand();

    /**
     * @return The permission of the command
     */
    Permission permission();

    /**
     * @return The command aliases
     */
    List<String> aliases();

    /**
     * @return The command description
     */
    String description();

    /**
     * @return The allowed command sources
     */
    AllowedCommandSources sources();

    /**
     * Handles the command
     *
     * @param commandSource The command source of the command
     * @param strings The parameters given in the command
     * @return If the command execute was successful
     */
    boolean handleCommand(CommandSource commandSource, String[] strings);
}
