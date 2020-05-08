/*
 * MIT License
 *
 * Copyright (c) ReformCloud-Team
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package systems.reformcloud.reformcloud2.runner.commands;

import org.jetbrains.annotations.NotNull;
import systems.reformcloud.reformcloud2.runner.Runner;
import systems.reformcloud.reformcloud2.runner.reformscript.InterpretedReformScript;
import systems.reformcloud.reformcloud2.runner.reformscript.utils.InterpreterCommand;

import java.util.Collection;

public final class CheckForUpdatesCommand extends InterpreterCommand {

    public CheckForUpdatesCommand(@NotNull Runner runner) {
        super("check_for_updates");
        this.runner = runner;
    }

    private final Runner runner;

    @Override
    public void execute(@NotNull String cursorLine, @NotNull InterpretedReformScript script, @NotNull Collection<String> allLines) {
        if (Integer.getInteger("reformcloud.executor.type", 0) == 3
                || !Boolean.getBoolean("reformcloud.auto.update")
                || Boolean.getBoolean("reformcloud.indev.builds")
                || Boolean.getBoolean("reformcloud.dev.mode")) {
            System.out.println("Automatic apply of updates is disabled!");
            return;
        }

        System.out.println("Collecting information about updates...");
        runner.getApplicationsUpdater().collectInformation();
        runner.getCloudVersionUpdater().collectInformation();
        System.out.println("Collected all needed information");

        if (runner.getCloudVersionUpdater().hasNewVersion()) {
            System.out.println("The " + runner.getCloudVersionUpdater().getName() + " updater has a new version available");
            runner.getCloudVersionUpdater().applyUpdates();
        }

        if (runner.getApplicationsUpdater().hasNewVersion()) {
            System.out.println("The " + runner.getApplicationsUpdater().getName() + " updater has a new version available");
            runner.getApplicationsUpdater().applyUpdates();
        }
    }
}
