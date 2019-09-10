package de.klaro.reformcloud2.executor.controller.commands;

import de.klaro.reformcloud2.executor.api.common.CommonHelper;
import de.klaro.reformcloud2.executor.api.common.ExecutorAPI;
import de.klaro.reformcloud2.executor.api.common.commands.basic.GlobalCommand;
import de.klaro.reformcloud2.executor.api.common.commands.source.CommandSource;
import de.klaro.reformcloud2.executor.api.common.groups.MainGroup;
import de.klaro.reformcloud2.executor.api.common.groups.ProcessGroup;
import de.klaro.reformcloud2.executor.api.common.groups.basic.DefaultProcessGroup;
import de.klaro.reformcloud2.executor.api.common.groups.utils.*;
import de.klaro.reformcloud2.executor.api.common.language.LanguageManager;
import de.klaro.reformcloud2.executor.api.common.process.ProcessInformation;
import de.klaro.reformcloud2.executor.api.common.utility.thread.AbsoluteThread;
import de.klaro.reformcloud2.executor.controller.ControllerExecutor;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class CommandProcess extends GlobalCommand {

    public CommandProcess() {
        super("rc", "reformcloud.command.rc", "The main management command for reformcloud", Arrays.asList("reformcloud", "servers", "process", "proxies"));
    }

    @Override
    public boolean handleCommand(CommandSource commandSource, String[] strings) {
        if (strings.length == 1 && strings[0].equalsIgnoreCase("versions")) {
            {
                System.out.println(LanguageManager.get("command-rc-available-versions", "Java-Proxy"));
                StringBuilder stringBuilder = new StringBuilder();
                Version.getAvailableJavaProxyVersions().forEach(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        stringBuilder.append(s).append(", ");
                    }
                });
                System.out.println(stringBuilder.substring(0, stringBuilder.length() - 1));
            }

            {
                System.out.println(LanguageManager.get("command-rc-available-versions", "Pocket-Edition-Proxy"));
                StringBuilder stringBuilder = new StringBuilder().append("\n");
                Version.getAvailablePocketProxyVersions().forEach(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        stringBuilder.append(s).append(", ");
                    }
                });
                System.out.println(stringBuilder.substring(0, stringBuilder.length() - 1));
            }

            {
                System.out.println(LanguageManager.get("command-rc-available-versions", "Java-Server"));
                StringBuilder stringBuilder = new StringBuilder();
                Version.getAvailableJavaServerVersions().forEach(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        stringBuilder.append(s).append(", ");
                    }
                });
                System.out.println(stringBuilder.substring(0, stringBuilder.length() - 1));
            }

            {
                System.out.println(LanguageManager.get("command-rc-available-versions", "Pocket-Edition-Server"));
                StringBuilder stringBuilder = new StringBuilder();
                Version.getAvailablePocketServerVersions().forEach(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        stringBuilder.append(s).append(", ");
                    }
                });
                System.out.println(stringBuilder.substring(0, stringBuilder.length() - 1));
            }
            return true;
        } else if (strings.length == 1 && strings[0].equalsIgnoreCase("list")) {
            ExecutorAPI.getInstance().getAllProcesses().forEach(new Consumer<ProcessInformation>() {
                @Override
                public void accept(ProcessInformation processInformation) {
                    System.out.println(
                            "  => "
                                    + processInformation.getName()
                                    + "/" + processInformation.getProcessUniqueID()
                                    + " " + processInformation.getOnlineCount() + "/"
                                    + processInformation.getMaxPlayers() + " "
                                    + processInformation.getTemplate().getVersion()
                    );
                }
            });
            return true;
        }


        if (strings.length <= 1) {
            sendHelp(commandSource);
            return true;
        }

        switch (strings[0].toLowerCase()) {
            case "start": {
                if (strings.length == 2) {
                    ProcessGroup processGroup = ExecutorAPI.getInstance().getProcessGroup(strings[1]);
                    if (processGroup == null) {
                        System.out.println(LanguageManager.get("command-rc-group-unknown", strings[1]));
                        return true;
                    }

                    ExecutorAPI.getInstance().startProcess(processGroup.getName());
                    System.out.println(LanguageManager.get("command-rc-execute-success"));
                    return true;
                }

                if (strings.length == 3) {
                    ProcessGroup processGroup = ExecutorAPI.getInstance().getProcessGroup(strings[1]);
                    if (processGroup == null) {
                        System.out.println(LanguageManager.get("command-rc-group-unknown", strings[1]));
                        return true;
                    }

                    Integer i = CommonHelper.fromString(strings[2]);
                    if (i == null || i < 1) {
                        System.out.println(LanguageManager.get("command-rc-integer-failed", strings[2]));
                        return true;
                    }

                    for (int started = 0; started <= i; started++) {
                        ExecutorAPI.getInstance().startProcess(processGroup.getName());
                        AbsoluteThread.sleep(TimeUnit.MILLISECONDS, 20);
                    }

                    System.out.println(LanguageManager.get("command-rc-execute-success"));
                    return true;
                }

                if (strings.length == 4) {
                    ProcessGroup processGroup = ExecutorAPI.getInstance().getProcessGroup(strings[1]);
                    if (processGroup == null) {
                        System.out.println(LanguageManager.get("command-rc-group-unknown", strings[1]));
                        return true;
                    }

                    Integer i = CommonHelper.fromString(strings[2]);
                    if (i == null || i < 1) {
                        System.out.println(LanguageManager.get("command-rc-integer-failed", strings[2]));
                        return true;
                    }

                    for (int started = 0; started <= i; started++) {
                        ExecutorAPI.getInstance().startProcess(processGroup.getName(), strings[3]);
                        AbsoluteThread.sleep(TimeUnit.MILLISECONDS, 20);
                    }

                    System.out.println(LanguageManager.get("command-rc-execute-success"));
                    return true;
                }
                break;
            }

            case "stop": {
                if (strings.length == 2) {
                    ProcessInformation processInformation;
                    UUID uuid = CommonHelper.tryParse(strings[1]);
                    if (uuid == null) {
                        processInformation = ExecutorAPI.getInstance().getProcess(strings[1]);
                    } else {
                        processInformation = ExecutorAPI.getInstance().getProcess(uuid);
                    }

                    if (processInformation == null) {
                        System.out.println(LanguageManager.get("command-rc-process-unknown", strings[1]));
                        return true;
                    }

                    ExecutorAPI.getInstance().stopProcess(processInformation.getProcessUniqueID());
                    System.out.println(LanguageManager.get("command-rc-execute-success"));
                    return true;
                }
                break;
            }

            case "stopall": {
                if (strings.length == 2) {
                    ProcessGroup processGroup = ExecutorAPI.getInstance().getProcessGroup(strings[1]);
                    if (processGroup == null) {
                        System.out.println(LanguageManager.get("command-rc-group-unknown", strings[1]));
                        return true;
                    }

                    ExecutorAPI.getInstance().getProcesses(processGroup.getName()).forEach(new Consumer<ProcessInformation>() {
                        @Override
                        public void accept(ProcessInformation processInformation) {
                            ExecutorAPI.getInstance().stopProcess(processInformation.getProcessUniqueID());
                            AbsoluteThread.sleep(TimeUnit.MILLISECONDS, 10);
                        }
                    });

                    System.out.println(LanguageManager.get("command-rc-execute-success"));
                    return true;
                }
                break;
            }

            case "ofall": {
                if (strings.length == 3) {
                    if (strings[2].equalsIgnoreCase("list")) {
                        MainGroup mainGroup = ExecutorAPI.getInstance().getMainGroup(strings[1]);
                        if (mainGroup == null) {
                            System.out.println(LanguageManager.get("command-rc-main-group-unknown", strings[1]));
                            return true;
                        }

                        mainGroup.getSubGroups().forEach(new Consumer<String>() {
                            @Override
                            public void accept(String s) {
                                System.out.println(LanguageManager.get("command-rc-main-sub-group", s));
                            }
                        });
                        return true;
                    } else if (strings[2].equalsIgnoreCase("stop")) {
                        MainGroup mainGroup = ExecutorAPI.getInstance().getMainGroup(strings[1]);
                        if (mainGroup == null) {
                            System.out.println(LanguageManager.get("command-rc-main-group-unknown", strings[1]));
                            return true;
                        }

                        mainGroup.getSubGroups().forEach(new Consumer<String>() {
                            @Override
                            public void accept(String s) {
                                ProcessGroup processGroup = ExecutorAPI.getInstance().getProcessGroup(s);
                                if (processGroup == null) {
                                    return;
                                }

                                ExecutorAPI.getInstance().getProcesses(processGroup.getName()).forEach(new Consumer<ProcessInformation>() {
                                    @Override
                                    public void accept(ProcessInformation processInformation) {
                                        ExecutorAPI.getInstance().stopProcess(processInformation.getProcessUniqueID());
                                        AbsoluteThread.sleep(TimeUnit.MILLISECONDS, 10);
                                    }
                                });
                            }
                        });

                        System.out.println(LanguageManager.get("command-rc-execute-success"));
                        return true;
                    }
                }
                break;
            }

            case "execute": {
                ProcessInformation processInformation;
                UUID uuid = CommonHelper.tryParse(strings[1]);
                if (uuid == null) {
                    processInformation = ExecutorAPI.getInstance().getProcess(strings[1]);
                } else {
                    processInformation = ExecutorAPI.getInstance().getProcess(uuid);
                }

                if (processInformation == null) {
                    System.out.println(LanguageManager.get("command-rc-process-unknown", strings[1]));
                    return true;
                }

                StringBuilder stringBuilder = new StringBuilder();
                for (String s : Arrays.copyOfRange(strings, 2, strings.length)) {
                    stringBuilder.append(s).append(" ");
                }

                ExecutorAPI.getInstance().executeProcessCommand(processInformation.getName(), stringBuilder.toString());
                System.out.println(LanguageManager.get("command-rc-execute-success"));
                return true;
            }

            case "create": {
                if (strings[1].equalsIgnoreCase("main") && strings.length == 3) {
                    MainGroup mainGroup = ExecutorAPI.getInstance().getMainGroup(strings[2]);
                    if (mainGroup == null) {
                        ExecutorAPI.getInstance().createMainGroup(strings[2]);
                        System.out.println(LanguageManager.get("command-rc-execute-success"));
                    } else {
                        System.out.println(LanguageManager.get("command-rc-create-main-group-already-exists", strings[2]));
                    }
                    return true;
                }

                if (strings[1].equalsIgnoreCase("sub")) {
                    if (strings.length == 3) {
                        ProcessGroup processGroup = ExecutorAPI.getInstance().getProcessGroup(strings[2]);
                        if (processGroup == null) {
                            ExecutorAPI.getInstance().createProcessGroup(strings[2]);
                            System.out.println(LanguageManager.get("command-rc-execute-success"));
                        } else {
                            System.out.println(LanguageManager.get("command-rc-create-sub-group-already-exists", strings[2]));
                        }
                        return true;
                    }

                    if (strings.length == 4) {
                        ProcessGroup processGroup = ExecutorAPI.getInstance().getProcessGroup(strings[2]);
                        Version version = CommonHelper.findEnumField(Version.class, strings[3]).orNothing();
                        if (version == null) {
                            System.out.println(LanguageManager.get("command-rc-version-not-found", strings[3]));
                            return true;
                        }

                        if (processGroup == null) {
                            ExecutorAPI.getInstance().createProcessGroup(
                                    strings[2],
                                    null,
                                    Collections.singletonList(new Template(
                                            0,
                                            "default",
                                            "#",
                                            null,
                                            "A ReformCloud2 default process",
                                            new RuntimeConfiguration(
                                                    512,
                                                    new ArrayList<>(),
                                                    new HashMap<>()
                                            ), version
                                    ))
                            );
                            System.out.println(LanguageManager.get("command-rc-execute-success"));
                        } else {
                            System.out.println(LanguageManager.get("command-rc-create-sub-group-already-exists", strings[2]));
                        }
                        return true;
                    }

                    if (strings.length == 5) {
                        ProcessGroup processGroup = ExecutorAPI.getInstance().getProcessGroup(strings[2]);
                        Version version = CommonHelper.findEnumField(Version.class, strings[3]).orNothing();
                        if (version == null) {
                            System.out.println(LanguageManager.get("command-rc-version-not-found", strings[3]));
                            return true;
                        }

                        if (processGroup == null) {
                            ExecutorAPI.getInstance().createProcessGroup(
                                    strings[2],
                                    strings[4],
                                    Collections.singletonList(new Template(
                                            0,
                                            "default",
                                            "#",
                                            null,
                                            "A ReformCloud2 default process",
                                            new RuntimeConfiguration(
                                                    512,
                                                    new ArrayList<>(),
                                                    new HashMap<>()
                                            ), version
                                    ))
                            );
                            System.out.println(LanguageManager.get("command-rc-execute-success"));
                        } else {
                            System.out.println(LanguageManager.get("command-rc-create-sub-group-already-exists", strings[2]));
                        }
                        return true;
                    }

                    if (strings.length == 6) {
                        ProcessGroup processGroup = ExecutorAPI.getInstance().getProcessGroup(strings[2]);
                        Version version = CommonHelper.findEnumField(Version.class, strings[3]).orNothing();
                        Boolean staticProcess = CommonHelper.booleanFromString(strings[5]);
                        if (version == null) {
                            System.out.println(LanguageManager.get("command-rc-version-not-found", strings[3]));
                            return true;
                        }

                        if (staticProcess == null) {
                            System.out.println(LanguageManager.get("command-rc-required-boolean", strings[5]));
                            return true;
                        }

                        if (processGroup == null) {
                            ExecutorAPI.getInstance().createProcessGroup(
                                    strings[2],
                                    strings[4],
                                    Collections.singletonList(new Template(
                                            0,
                                            "default",
                                            "#",
                                            null,
                                            "A ReformCloud2 default process",
                                            new RuntimeConfiguration(
                                                    512,
                                                    new ArrayList<>(),
                                                    new HashMap<>()
                                            ), version
                                    )), new StartupConfiguration(
                                            -1, 1, 1, 41000, StartupEnvironment.JAVA_RUNTIME, true, new ArrayList<>()
                                    ), new PlayerAccessConfiguration(
                                            false, "reformcloud.join.maintenance", false,
                                            null, true, true, true, 50
                                    ), staticProcess
                            );
                            System.out.println(LanguageManager.get("command-rc-execute-success"));
                        } else {
                            System.out.println(LanguageManager.get("command-rc-create-sub-group-already-exists", strings[2]));
                        }
                        return true;
                    }

                    if (strings.length == 7) {
                        ProcessGroup processGroup = ExecutorAPI.getInstance().getProcessGroup(strings[2]);
                        Version version = CommonHelper.findEnumField(Version.class, strings[3]).orNothing();
                        Boolean staticProcess = CommonHelper.booleanFromString(strings[5]);
                        Boolean lobby = CommonHelper.booleanFromString(strings[6]);

                        if (version == null) {
                            System.out.println(LanguageManager.get("command-rc-version-not-found", strings[3]));
                            return true;
                        }

                        if (lobby == null) {
                            System.out.println(LanguageManager.get("command-rc-required-boolean", strings[6]));
                            return true;
                        }

                        if (staticProcess == null) {
                            System.out.println(LanguageManager.get("command-rc-required-boolean", strings[5]));
                            return true;
                        }

                        if (processGroup == null) {
                            ProcessGroup processGroup1 = new DefaultProcessGroup(
                                    strings[2],
                                    strings[4],
                                    41000,
                                    version,
                                    512,
                                    false,
                                    50,
                                    staticProcess,
                                    lobby
                            );

                            ExecutorAPI.getInstance().createProcessGroup(processGroup1);
                            System.out.println(LanguageManager.get("command-rc-execute-success"));
                        } else {
                            System.out.println(LanguageManager.get("command-rc-create-sub-group-already-exists", strings[2]));
                        }
                        return true;
                    }

                    if (strings.length == 8) {
                        ProcessGroup processGroup = ExecutorAPI.getInstance().getProcessGroup(strings[2]);
                        Version version = CommonHelper.findEnumField(Version.class, strings[3]).orNothing();
                        Boolean staticProcess = CommonHelper.booleanFromString(strings[5]);
                        Integer min = CommonHelper.fromString(strings[6]);
                        Integer max = CommonHelper.fromString(strings[7]);

                        if (version == null) {
                            System.out.println(LanguageManager.get("command-rc-version-not-found", strings[3]));
                            return true;
                        }

                        if (max == null || max < -1) {
                            System.out.println(LanguageManager.get("command-rc-integer-failed", strings[7]));
                            return true;
                        }

                        if (min == null || min < 0) {
                            System.out.println(LanguageManager.get("command-rc-integer-failed", strings[6]));
                            return true;
                        }

                        if (staticProcess == null) {
                            System.out.println(LanguageManager.get("command-rc-required-boolean", strings[5]));
                            return true;
                        }

                        if (processGroup == null) {
                            ExecutorAPI.getInstance().createProcessGroup(
                                    strings[2],
                                    strings[4],
                                    Collections.singletonList(new Template(
                                            0,
                                            "default",
                                            "#",
                                            null,
                                            "A ReformCloud2 default process",
                                            new RuntimeConfiguration(
                                                    512,
                                                    new ArrayList<>(),
                                                    new HashMap<>()
                                            ), version
                                    )), new StartupConfiguration(
                                            max, min, 1, 41000, StartupEnvironment.JAVA_RUNTIME, true, new ArrayList<>()
                                    ), new PlayerAccessConfiguration(
                                            false, "reformcloud.join.maintenance", false,
                                            null, true, true, true, 50
                                    ), staticProcess
                            );
                            System.out.println(LanguageManager.get("command-rc-execute-success"));
                        } else {
                            System.out.println(LanguageManager.get("command-rc-create-sub-group-already-exists", strings[2]));
                        }
                        return true;
                    }
                }
                break;
            }

            case "list": {
                if (strings.length == 2) {
                    ExecutorAPI.getInstance().getProcesses(strings[1]).forEach(new Consumer<ProcessInformation>() {
                        @Override
                        public void accept(ProcessInformation processInformation) {
                            System.out.println(
                                    "  => "
                                            + processInformation.getName()
                                            + "/" + processInformation.getProcessUniqueID()
                                            + " " + processInformation.getOnlineCount() + "/"
                                            + processInformation.getMaxPlayers() + " "
                                            + processInformation.getTemplate().getVersion()
                            );
                        }
                    });
                }

                System.out.println(LanguageManager.get("command-rc-execute-success"));
                return true;
            }

            case "listgroups": {
                if (strings.length == 2 && strings[1].equalsIgnoreCase("sub")) {
                    ExecutorAPI.getInstance().getProcessGroups().forEach(new Consumer<ProcessGroup>() {
                        @Override
                        public void accept(ProcessGroup processGroup) {
                            System.out.println("  => " +
                                    processGroup.getName() +
                                    " parent: " + processGroup.getParentGroup() +
                                    " maintenance: " + processGroup.getPlayerAccessConfiguration().isMaintenance() +
                                    " static: " + processGroup.isStaticProcess() +
                                    " lobby: " + processGroup.isCanBeUsedAsLobby()
                            );
                        }
                    });
                    return true;
                }

                if (strings.length == 2 && strings[1].equalsIgnoreCase("main")) {
                    ExecutorAPI.getInstance().getMainGroups().forEach(new Consumer<MainGroup>() {
                        @Override
                        public void accept(MainGroup mainGroup) {
                            System.out.println("  => " + mainGroup.getName() + "/" + mainGroup.getSubGroups());
                        }
                    });
                    return true;
                }
                break;
            }

            case "delete": {
                if (strings.length == 3) {
                    if (strings[1].equalsIgnoreCase("sub")) {
                        ProcessGroup processGroup = ExecutorAPI.getInstance().getProcessGroup(strings[2]);
                        if (processGroup == null) {
                            System.out.println(LanguageManager.get("command-rc-group-unknown", strings[1]));
                            return true;
                        }

                        ControllerExecutor.getInstance().getControllerExecutorConfig().deleteProcessGroup(processGroup);
                        System.out.println(LanguageManager.get("command-rc-execute-success"));
                        return true;
                    }

                    if (strings[1].equalsIgnoreCase("main")) {
                        MainGroup mainGroup = ExecutorAPI.getInstance().getMainGroup(strings[2]);
                        if (mainGroup == null) {
                            System.out.println(LanguageManager.get("command-rc-main-group-unknown", strings[1]));
                        }

                        ControllerExecutor.getInstance().getControllerExecutorConfig().deleteMainGroup(mainGroup);
                        System.out.println(LanguageManager.get("command-rc-execute-success"));
                        return true;
                    }
                }

                break;
            }

            default: {
                sendHelp(commandSource);
                return true;
            }
        }

        sendHelp(commandSource);
        return true;
    }

    private void sendHelp(CommandSource commandSource) {
        commandSource.sendMessage(
                "\n" +
                "rc list\n" +
                "rc list <group>\n" +
                "rc listgroups <main | sub>\n" +
                "rc versions\n" +
                "rc start <group>\n" +
                "rc start <group> <amount>\n" +
                "rc start <group> <amount> <template>\n" +
                "rc stop <name>\n" +
                "rc stop <uuid>\n" +
                "rc stopall <subGroup>\n" +
                "rc ofAll <mainGroup> <list | stop>\n" +
                "rc execute <name | uuid> <command>\n" +
                "rc create main <name>\n" +
                "rc create sub <name>\n" +
                "rc create sub <name> <version>\n" +
                "rc create sub <name> <version> <parent>\n" +
                "rc create sub <name> <version> <parent> <static>\n" +
                "rc create sub <name> <version> <parent> <static> <lobby>\n" +
                "rc create sub <name> <version> <parent> <static> <minonline> <maxonline>\n" +
                "rc delete <sub | main> <name>"
        );
    }
}
