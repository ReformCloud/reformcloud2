package systems.reformcloud.reformcloud2.executor.node;

import io.netty.channel.ChannelHandlerContext;
import org.reflections.Reflections;
import systems.reformcloud.reformcloud2.executor.api.ExecutorType;
import systems.reformcloud.reformcloud2.executor.api.common.CommonHelper;
import systems.reformcloud.reformcloud2.executor.api.common.ExecutorAPI;
import systems.reformcloud.reformcloud2.executor.api.common.application.ApplicationLoader;
import systems.reformcloud.reformcloud2.executor.api.common.application.InstallableApplication;
import systems.reformcloud.reformcloud2.executor.api.common.application.LoadedApplication;
import systems.reformcloud.reformcloud2.executor.api.common.application.basic.DefaultApplicationLoader;
import systems.reformcloud.reformcloud2.executor.api.common.client.ClientRuntimeInformation;
import systems.reformcloud.reformcloud2.executor.api.common.commands.AllowedCommandSources;
import systems.reformcloud.reformcloud2.executor.api.common.commands.Command;
import systems.reformcloud.reformcloud2.executor.api.common.commands.basic.ConsoleCommandSource;
import systems.reformcloud.reformcloud2.executor.api.common.commands.basic.commands.CommandClear;
import systems.reformcloud.reformcloud2.executor.api.common.commands.basic.commands.CommandHelp;
import systems.reformcloud.reformcloud2.executor.api.common.commands.basic.commands.CommandReload;
import systems.reformcloud.reformcloud2.executor.api.common.commands.basic.commands.CommandStop;
import systems.reformcloud.reformcloud2.executor.api.common.commands.basic.manager.DefaultCommandManager;
import systems.reformcloud.reformcloud2.executor.api.common.commands.manager.CommandManager;
import systems.reformcloud.reformcloud2.executor.api.common.commands.source.CommandSource;
import systems.reformcloud.reformcloud2.executor.api.common.configuration.JsonConfiguration;
import systems.reformcloud.reformcloud2.executor.api.common.database.Database;
import systems.reformcloud.reformcloud2.executor.api.common.database.basic.drivers.file.FileDatabase;
import systems.reformcloud.reformcloud2.executor.api.common.database.basic.drivers.h2.H2Database;
import systems.reformcloud.reformcloud2.executor.api.common.database.basic.drivers.mongo.MongoDatabase;
import systems.reformcloud.reformcloud2.executor.api.common.database.basic.drivers.mysql.MySQLDatabase;
import systems.reformcloud.reformcloud2.executor.api.common.database.config.DatabaseConfig;
import systems.reformcloud.reformcloud2.executor.api.common.groups.MainGroup;
import systems.reformcloud.reformcloud2.executor.api.common.groups.ProcessGroup;
import systems.reformcloud.reformcloud2.executor.api.common.groups.template.RuntimeConfiguration;
import systems.reformcloud.reformcloud2.executor.api.common.groups.template.Template;
import systems.reformcloud.reformcloud2.executor.api.common.groups.template.Version;
import systems.reformcloud.reformcloud2.executor.api.common.groups.template.backend.TemplateBackendManager;
import systems.reformcloud.reformcloud2.executor.api.common.groups.template.backend.basic.FileBackend;
import systems.reformcloud.reformcloud2.executor.api.common.groups.utils.PlayerAccessConfiguration;
import systems.reformcloud.reformcloud2.executor.api.common.groups.utils.StartupConfiguration;
import systems.reformcloud.reformcloud2.executor.api.common.groups.utils.StartupEnvironment;
import systems.reformcloud.reformcloud2.executor.api.common.language.LanguageManager;
import systems.reformcloud.reformcloud2.executor.api.common.logger.LoggerBase;
import systems.reformcloud.reformcloud2.executor.api.common.logger.coloured.ColouredLoggerHandler;
import systems.reformcloud.reformcloud2.executor.api.common.logger.other.DefaultLoggerHandler;
import systems.reformcloud.reformcloud2.executor.api.common.network.auth.Auth;
import systems.reformcloud.reformcloud2.executor.api.common.network.auth.NetworkType;
import systems.reformcloud.reformcloud2.executor.api.common.network.auth.defaults.DefaultAuth;
import systems.reformcloud.reformcloud2.executor.api.common.network.auth.defaults.DefaultServerAuthHandler;
import systems.reformcloud.reformcloud2.executor.api.common.network.channel.PacketSender;
import systems.reformcloud.reformcloud2.executor.api.common.network.channel.defaults.DefaultPacketSender;
import systems.reformcloud.reformcloud2.executor.api.common.network.channel.handler.NetworkHandler;
import systems.reformcloud.reformcloud2.executor.api.common.network.channel.manager.DefaultChannelManager;
import systems.reformcloud.reformcloud2.executor.api.common.network.client.NetworkClient;
import systems.reformcloud.reformcloud2.executor.api.common.network.packet.DefaultPacket;
import systems.reformcloud.reformcloud2.executor.api.common.network.packet.defaults.DefaultPacketHandler;
import systems.reformcloud.reformcloud2.executor.api.common.network.packet.handler.PacketHandler;
import systems.reformcloud.reformcloud2.executor.api.common.network.server.DefaultNetworkServer;
import systems.reformcloud.reformcloud2.executor.api.common.network.server.NetworkServer;
import systems.reformcloud.reformcloud2.executor.api.common.node.NodeInformation;
import systems.reformcloud.reformcloud2.executor.api.common.plugins.InstallablePlugin;
import systems.reformcloud.reformcloud2.executor.api.common.plugins.Plugin;
import systems.reformcloud.reformcloud2.executor.api.common.plugins.basic.DefaultPlugin;
import systems.reformcloud.reformcloud2.executor.api.common.process.ProcessInformation;
import systems.reformcloud.reformcloud2.executor.api.common.process.ProcessState;
import systems.reformcloud.reformcloud2.executor.api.common.restapi.auth.basic.DefaultWebServerAuth;
import systems.reformcloud.reformcloud2.executor.api.common.restapi.http.server.DefaultWebServer;
import systems.reformcloud.reformcloud2.executor.api.common.restapi.http.server.WebServer;
import systems.reformcloud.reformcloud2.executor.api.common.restapi.request.RequestListenerHandler;
import systems.reformcloud.reformcloud2.executor.api.common.restapi.request.defaults.DefaultRequestListenerHandler;
import systems.reformcloud.reformcloud2.executor.api.common.restapi.user.WebUser;
import systems.reformcloud.reformcloud2.executor.api.common.utility.StringUtil;
import systems.reformcloud.reformcloud2.executor.api.common.utility.function.Double;
import systems.reformcloud.reformcloud2.executor.api.common.utility.list.Links;
import systems.reformcloud.reformcloud2.executor.api.common.utility.task.Task;
import systems.reformcloud.reformcloud2.executor.api.common.utility.task.defaults.DefaultTask;
import systems.reformcloud.reformcloud2.executor.api.common.utility.thread.AbsoluteThread;
import systems.reformcloud.reformcloud2.executor.api.node.Node;
import systems.reformcloud.reformcloud2.executor.api.node.cluster.ClusterSyncManager;
import systems.reformcloud.reformcloud2.executor.api.node.cluster.SyncAction;
import systems.reformcloud.reformcloud2.executor.api.node.network.NodeNetworkManager;
import systems.reformcloud.reformcloud2.executor.api.node.process.NodeProcessManager;
import systems.reformcloud.reformcloud2.executor.node.cluster.DefaultClusterManager;
import systems.reformcloud.reformcloud2.executor.node.cluster.DefaultNodeInternalCluster;
import systems.reformcloud.reformcloud2.executor.node.cluster.sync.DefaultClusterSyncManager;
import systems.reformcloud.reformcloud2.executor.node.config.NodeConfig;
import systems.reformcloud.reformcloud2.executor.node.config.NodeExecutorConfig;
import systems.reformcloud.reformcloud2.executor.node.network.DefaultNodeNetworkManager;
import systems.reformcloud.reformcloud2.executor.node.network.client.NodeNetworkClient;
import systems.reformcloud.reformcloud2.executor.node.network.packet.out.api.NodeAPIAction;
import systems.reformcloud.reformcloud2.executor.node.process.LocalAutoStartupHandler;
import systems.reformcloud.reformcloud2.executor.node.process.LocalNodeProcessManager;
import systems.reformcloud.reformcloud2.executor.node.process.manager.LocalProcessManager;
import systems.reformcloud.reformcloud2.executor.node.process.startup.LocalProcessQueue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class NodeExecutor extends Node {

    private static NodeExecutor instance;

    private static volatile boolean running = false;

    private final CommandManager commandManager = new DefaultCommandManager();

    private final CommandSource console = new ConsoleCommandSource(commandManager);

    private final ApplicationLoader applicationLoader = new DefaultApplicationLoader();

    private final NetworkServer networkServer = new DefaultNetworkServer();

    private final WebServer webServer = new DefaultWebServer();

    private final PacketHandler packetHandler = new DefaultPacketHandler();

    private final NodeProcessManager nodeProcessManager = new LocalNodeProcessManager();

    private final NodeExecutorConfig nodeExecutorConfig = new NodeExecutorConfig();

    private final DatabaseConfig databaseConfig = new DatabaseConfig();

    private final LocalAutoStartupHandler localAutoStartupHandler = new LocalAutoStartupHandler();

    private LocalProcessQueue localProcessQueue;

    private NetworkClient networkClient;

    private NodeConfig nodeConfig;

    private Database database;

    private NodeNetworkManager nodeNetworkManager;

    private ClusterSyncManager clusterSyncManager;

    private LoggerBase loggerBase;

    private RequestListenerHandler requestListenerHandler;

    NodeExecutor() {
        Node.setInstance(this);
        ExecutorAPI.setInstance(this);
        super.type = ExecutorType.NODE;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                shutdown();
            } catch (final Throwable throwable) {
                throwable.printStackTrace();
            }
        }, "Shutdown-Hook"));

        bootstrap();
    }

    @Override
    protected void bootstrap() {
        final long current = System.currentTimeMillis();
        instance = this;

        try {
            if (Boolean.getBoolean("reformcloud2.disable.colours")) {
                this.loggerBase = new DefaultLoggerHandler();
            } else {
                this.loggerBase = new ColouredLoggerHandler();
            }
        } catch (final IOException ex) {
            ex.printStackTrace();
        }

        this.nodeExecutorConfig.init();
        this.nodeConfig = this.nodeExecutorConfig.getNodeConfig();

        this.nodeNetworkManager = new DefaultNodeNetworkManager(
                nodeProcessManager,
                new DefaultNodeInternalCluster(new DefaultClusterManager(), nodeExecutorConfig.getSelf(), packetHandler)
        );
        this.nodeNetworkManager.getCluster().getClusterManager().init();

        this.requestListenerHandler = new DefaultRequestListenerHandler(new DefaultWebServerAuth(this));

        final NodeNetworkClient nodeNetworkClient = new NodeNetworkClient();
        this.networkClient = nodeNetworkClient;
        this.clusterSyncManager = new DefaultClusterSyncManager(nodeNetworkClient);

        this.applicationLoader.detectApplications();
        this.applicationLoader.installApplications();

        TemplateBackendManager.registerDefaults();

        this.loadPacketHandlers();

        this.nodeConfig.getNetworkListener().forEach(e -> e.forEach((ip, port) -> {
            this.networkServer.bind(ip, port, new DefaultServerAuthHandler(
                    packetHandler,
                    packetSender -> this.nodeNetworkManager.getCluster().getClusterManager().handleNodeDisconnect(
                                this.nodeNetworkManager.getCluster(),
                                packetSender.getName()
                    ),
                    packet -> {
                        DefaultAuth auth = packet.content().get("auth", Auth.TYPE);
                        if (this.nodeExecutorConfig.getConnectionKey() == null || auth == null) {
                            System.out.println(LanguageManager.get("network-channel-auth-failed", auth == null ? "unknown" : auth.getName()));
                            return new Double<>(auth == null ? "unknown" : auth.getName(), false);
                        }

                        if (auth.type().equals(NetworkType.CLIENT)) {
                            return new Double<>(auth.getName(), false);
                        }

                        AtomicReference<Double<String, Boolean>> result = new AtomicReference<>();
                        if (auth.type().equals(NetworkType.NODE)) {
                            NodeInformation nodeInformation = packet.content().get("info", NodeInformation.TYPE);
                            if (nodeInformation == null) {
                                return new Double<>(auth.getName(), false);
                            }

                            this.nodeNetworkManager.getCluster().getClusterManager().handleConnect(
                                    this.nodeNetworkManager.getCluster(),
                                    nodeInformation,
                                    (aBoolean, s) -> result.set(new Double<>(nodeInformation.getName(), aBoolean))
                            );
                        } else if (auth.type().equals(NetworkType.PROCESS)) {
                            ProcessInformation information = nodeNetworkManager.getNodeProcessHelper()
                                    .getLocalCloudProcess(auth.getName());
                            if (information == null) {
                                return new Double<>(auth.getName(), false);
                            }

                            information.getNetworkInfo().setConnected(true);
                            information.setProcessState(ProcessState.READY);
                            nodeNetworkManager.getNodeProcessHelper().update(information);
                            System.out.println(LanguageManager.get("process-connected", auth.getName(), auth.parent()));
                        }

                        System.out.println(LanguageManager.get("network-channel-auth-success", auth.getName(), auth.parent()));
                        return result.get();
                    }
            ) {
                @Override
                public BiFunction<String, ChannelHandlerContext, PacketSender> onSuccess() {
                    return (s, context) -> {
                        context.channel().writeAndFlush(new DefaultPacket(-511, new JsonConfiguration().add("access", true)));
                        PacketSender sender = new DefaultPacketSender(context.channel());
                        sender.setName(s);
                        clusterSyncManager.getWaitingConnections().remove(sender.getAddress());
                        return sender;
                    };
                }

                @Override
                public Consumer<ChannelHandlerContext> onAuthFailure() {
                    return context -> {
                        String host = ((InetSocketAddress) context.channel().remoteAddress()).getAddress().getHostAddress();
                        clusterSyncManager.getWaitingConnections().remove(host);
                        context.channel().writeAndFlush(new DefaultPacket(-511, new JsonConfiguration().add("access", false))).syncUninterruptibly().channel().close();
                    };
                }
            });
        }));

        this.nodeConfig.getHttpNetworkListener().forEach(map -> map.forEach((host, port) -> {
                    this.webServer.add(host, port, this.requestListenerHandler);
                })
        );

        this.applicationLoader.loadApplications();

        databaseConfig.load();
        switch (databaseConfig.getType()) {
            case FILE: {
                this.database = new FileDatabase();
                this.databaseConfig.connect(this.database);
                break;
            }

            case H2: {
                this.database = new H2Database();
                this.databaseConfig.connect(this.database);
                break;
            }

            case MONGO: {
                this.database = new MongoDatabase();
                this.databaseConfig.connect(this.database);
                break;
            }

            case MYSQL: {
                this.database = new MySQLDatabase();
                this.databaseConfig.connect(this.database);
                break;
            }
        }

        createDatabase("internal_users");
        if (this.nodeExecutorConfig.isFirstStartup()) {
            final String token = StringUtil.generateString(2);
            WebUser webUser = new WebUser("admin", token, Collections.singletonList("*"));
            insert("internal_users", webUser.getName(), "", new JsonConfiguration().add("user", webUser));

            System.out.println(LanguageManager.get("setup-created-default-user", webUser.getName(), token));
        }

        if (this.nodeConfig.getOtherNodes().isEmpty()) {
            System.out.println(LanguageManager.get("network-node-no-other-nodes-defined"));
        } else {
            if (this.nodeExecutorConfig.getConnectionKey() == null) {
                System.out.println(LanguageManager.get("network-node-try-connect-with-no-key"));
            } else {
                this.nodeConfig.getOtherNodes().forEach(e -> e.forEach((ip, port) -> {
                    if (this.networkClient.connect(ip, port, new DefaultAuth(
                            this.nodeExecutorConfig.getConnectionKey(),
                            null,
                            NetworkType.NODE,
                            this.nodeNetworkManager.getCluster().getSelfNode().getName(),
                            new JsonConfiguration().add("info", this.nodeNetworkManager.getCluster().getSelfNode())
                    ), createReader(name -> {
                        NodeInformation information = this.nodeNetworkManager.getCluster().getNode(name);
                        if (information == null) {
                            this.nodeNetworkManager.getNodeProcessHelper().handleProcessDisconnect(name);
                        } else {
                            this.nodeNetworkManager.getCluster().getClusterManager().handleNodeDisconnect(
                                    this.nodeNetworkManager.getCluster(),
                                    name
                            );
                        }
                    }))) {
                        System.out.println(LanguageManager.get(
                                "network-node-connection-to-other-node-success", ip, Integer.toString(port)
                        ));
                        this.clusterSyncManager.getWaitingConnections().add(ip);
                    } else {
                        System.out.println(LanguageManager.get(
                                "network-node-connection-to-other-node-not-successful", ip, Integer.toString(port)
                        ));
                    }
                }));
            }
        }

        this.localProcessQueue = new LocalProcessQueue();
        this.localAutoStartupHandler.doStart();
        this.applicationLoader.enableApplications();

        this.loadCommands();
        this.sendGroups();

        running = true;
        System.out.println(LanguageManager.get("startup-done", Long.toString(System.currentTimeMillis() - current)));

        this.awaitConnectionsAndUpdate();
        this.runConsole();
    }

    public static NodeExecutor getInstance() {
        return instance;
    }

    public LoggerBase getLoggerBase() {
        return loggerBase;
    }

    public NodeNetworkManager getNodeNetworkManager() {
        return nodeNetworkManager;
    }

    public ClusterSyncManager getClusterSyncManager() {
        return clusterSyncManager;
    }

    public NodeConfig getNodeConfig() {
        return nodeConfig;
    }

    public NodeExecutorConfig getNodeExecutorConfig() {
        return nodeExecutorConfig;
    }

    public Double<String, Integer> getConnectHost() {
        if (this.nodeConfig.getNetworkListener().size() == 1) {
            Map.Entry<String, Integer> result = nodeConfig.getNetworkListener().get(0).entrySet().iterator().next();
            return new Double<>(result.getKey(), result.getValue());
        }

        Map.Entry<String, Integer> result = nodeConfig.getNetworkListener().get(new Random().nextInt(nodeConfig.getNetworkListener().size())).entrySet().iterator().next();
        return new Double<>(result.getKey(), result.getValue());
    }

    @Override
    public void shutdown() throws Exception {
        if (running) {
            running = false;
        } else {
            return;
        }

        System.out.println(LanguageManager.get("runtime-try-shutdown"));

        this.networkServer.closeAll();
        this.networkClient.disconnect();
        this.webServer.close();

        this.localProcessQueue.interrupt();
        this.localAutoStartupHandler.interrupt();

        LocalProcessManager.close();

        this.database.disconnect();

        this.nodeNetworkManager.getNodeProcessHelper().getClusterProcesses();
        this.applicationLoader.disableApplications();

        this.loggerBase.close();
    }

    private void runConsole() {
        String line;

        while (!Thread.currentThread().isInterrupted()) {
            try {
                loggerBase.getConsoleReader().setPrompt("");
                loggerBase.getConsoleReader().resetPromptLine("", "", 0);

                while ((line = loggerBase.readLine()) != null && !line.trim().isEmpty() && running) {
                    loggerBase.getConsoleReader().setPrompt("");
                    commandManager.dispatchCommand(console, AllowedCommandSources.ALL, line, System.out::println);
                }
            } catch (final Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    @Override
    public NetworkServer getNetworkServer() {
        return networkServer;
    }

    @Override
    public CommandManager getCommandManager() {
        return commandManager;
    }

    @Override
    public PacketHandler getPacketHandler() {
        return packetHandler;
    }

    @Override
    public Task<Boolean> loadApplicationAsync(InstallableApplication application) {
        Task<Boolean> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> task.complete(applicationLoader.doSpecificApplicationInstall(application)));
        return task;
    }

    @Override
    public Task<Boolean> unloadApplicationAsync(LoadedApplication application) {
        Task<Boolean> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> task.complete(applicationLoader.doSpecificApplicationUninstall(application)));
        return task;
    }

    @Override
    public Task<Boolean> unloadApplicationAsync(String application) {
        Task<Boolean> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> task.complete(applicationLoader.doSpecificApplicationUninstall(application)));
        return task;
    }

    @Override
    public Task<LoadedApplication> getApplicationAsync(String name) {
        Task<LoadedApplication> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> task.complete(applicationLoader.getApplication(name)));
        return task;
    }

    @Override
    public Task<List<LoadedApplication>> getApplicationsAsync() {
        Task<List<LoadedApplication>> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> task.complete(applicationLoader.getApplications()));
        return task;
    }

    @Override
    public boolean loadApplication(InstallableApplication application) {
        return applicationLoader.doSpecificApplicationInstall(application);
    }

    @Override
    public boolean unloadApplication(LoadedApplication application) {
        return applicationLoader.doSpecificApplicationUninstall(application);
    }

    @Override
    public boolean unloadApplication(String application) {
        return applicationLoader.doSpecificApplicationUninstall(application);
    }

    @Override
    public LoadedApplication getApplication(String name) {
        return applicationLoader.getApplication(name);
    }

    @Override
    public List<LoadedApplication> getApplications() {
        return applicationLoader.getApplications();
    }

    @Override
    public Task<Boolean> isClientConnectedAsync(String name) {
        Task<Boolean> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> task.complete(this.nodeNetworkManager.getCluster().getNode(name) != null));
        return task;
    }

    @Override
    public Task<String> getClientStartHostAsync(String name) {
        return null;
    }

    @Override
    public Task<Integer> getMaxMemoryAsync(String name) {
        Task<Integer> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> task.complete((int) this.nodeNetworkManager.getCluster().getNode(name).getMaxMemory()));
        return task;
    }

    @Override
    public Task<Integer> getMaxProcessesAsync(String name) {
        return null;
    }

    @Override
    public Task<ClientRuntimeInformation> getClientInformationAsync(String name) {
        return null;
    }

    @Override
    public boolean isClientConnected(String name) {
        return this.nodeNetworkManager.getCluster().getNode(name) != null;
    }

    @Override
    public String getClientStartHost(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxMemory(String name) {
        if (!isClientConnected(name)) {
            return -1;
        }

        return (int) nodeNetworkManager.getCluster().getNode(name).getMaxMemory();
    }

    @Override
    public int getMaxProcesses(String name) {
        throw new UnsupportedOperationException("There is no config option for max node processes");
    }

    @Override
    public ClientRuntimeInformation getClientInformation(String name) {
        return null;
    }

    @Override
    public Task<Void> sendColouredLineAsync(String line) throws IllegalAccessException {
        Task<Void> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> {
            System.out.println(line);
            task.complete(null);
        });
        return task;
    }

    @Override
    public Task<Void> sendRawLineAsync(String line) {
        Task<Void> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> {
            System.out.println(line);
            task.complete(null);
        });
        return task;
    }

    @Override
    public Task<String> dispatchCommandAndGetResultAsync(String commandLine) {
        Task<String> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> this.commandManager.dispatchCommand(console, AllowedCommandSources.ALL, commandLine, task::complete));
        return task;
    }

    @Override
    public Task<Command> getCommandAsync(String name) {
        Task<Command> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> task.complete(commandManager.getCommand(name)));
        return task;
    }

    @Override
    public Task<Boolean> isCommandRegisteredAsync(String name) {
        Task<Boolean> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> task.complete(commandManager.getCommand(name) != null));
        return task;
    }

    @Override
    public void sendColouredLine(String line) throws IllegalAccessException {
        sendColouredLineAsync(line).awaitUninterruptedly();
    }

    @Override
    public void sendRawLine(String line) {
        sendRawLineAsync(line).awaitUninterruptedly();
    }

    @Override
    public String dispatchCommandAndGetResult(String commandLine) {
        return dispatchCommandAndGetResultAsync(commandLine).getUninterruptedly();
    }

    @Override
    public Command getCommand(String name) {
        return getCommandAsync(name).getUninterruptedly();
    }

    @Override
    public boolean isCommandRegistered(String name) {
        return isCommandRegisteredAsync(name).getUninterruptedly();
    }

    @Override
    public Task<JsonConfiguration> findAsync(String table, String key, String identifier) {
        Task<JsonConfiguration> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> {
            JsonConfiguration jsonConfiguration = this.database.createForTable(table).find(key).getUninterruptedly();
            if (jsonConfiguration == null) {
                jsonConfiguration = this.database.createForTable(table).findIfAbsent(identifier).getUninterruptedly();
            }

            task.complete(jsonConfiguration);
        });
        return task;
    }

    @Override
    public <T> Task<T> findAsync(String table, String key, String identifier, Function<JsonConfiguration, T> function) {
        Task<T> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> task.complete(function.apply(find(table, key, identifier))));
        return task;
    }

    @Override
    public Task<Void> insertAsync(String table, String key, String identifier, JsonConfiguration data) {
        Task<Void> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> {
            this.database.createForTable(table).insert(key, identifier, data);
            task.complete(null);
        });
        return task;
    }

    @Override
    public Task<Boolean> updateAsync(String table, String key, JsonConfiguration newData) {
        Task<Boolean> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> task.complete(this.database.createForTable(table).update(key, newData).getUninterruptedly()));
        return task;
    }

    @Override
    public Task<Boolean> updateIfAbsentAsync(String table, String identifier, JsonConfiguration newData) {
        Task<Boolean> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> task.complete(this.database.createForTable(table).updateIfAbsent(identifier, newData).getUninterruptedly()));
        return task;
    }

    @Override
    public Task<Void> removeAsync(String table, String key) {
        return database.createForTable(table).remove(key);
    }

    @Override
    public Task<Void> removeIfAbsentAsync(String table, String identifier) {
        return database.createForTable(table).removeIfAbsent(identifier);
    }

    @Override
    public Task<Boolean> createDatabaseAsync(String name) {
        Task<Boolean> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> task.complete(database.createDatabase(name)));
        return task;
    }

    @Override
    public Task<Boolean> deleteDatabaseAsync(String name) {
        Task<Boolean> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> task.complete(database.deleteDatabase(name)));
        return task;
    }

    @Override
    public Task<Boolean> containsAsync(String table, String key) {
        return database.createForTable(table).contains(key);
    }

    @Override
    public Task<Integer> sizeAsync(String table) {
        return database.createForTable(table).size();
    }

    @Override
    public JsonConfiguration find(String table, String key, String identifier) {
        return findAsync(table, key, identifier).getUninterruptedly();
    }

    @Override
    public <T> T find(String table, String key, String identifier, Function<JsonConfiguration, T> function) {
        return findAsync(table, key, identifier, function).getUninterruptedly();
    }

    @Override
    public void insert(String table, String key, String identifier, JsonConfiguration data) {
        insertAsync(table, key, identifier, data).awaitUninterruptedly();
    }

    @Override
    public boolean update(String table, String key, JsonConfiguration newData) {
        return updateAsync(table, key, newData).getUninterruptedly();
    }

    @Override
    public boolean updateIfAbsent(String table, String identifier, JsonConfiguration newData) {
        return updateIfAbsentAsync(table, identifier, newData).getUninterruptedly();
    }

    @Override
    public void remove(String table, String key) {
        removeAsync(table, key).awaitUninterruptedly();
    }

    @Override
    public void removeIfAbsent(String table, String identifier) {
        removeIfAbsentAsync(table, identifier).awaitUninterruptedly();
    }

    @Override
    public boolean createDatabase(String name) {
        return createDatabaseAsync(name).getUninterruptedly();
    }

    @Override
    public boolean deleteDatabase(String name) {
        return deleteDatabaseAsync(name).getUninterruptedly();
    }

    @Override
    public boolean contains(String table, String key) {
        return containsAsync(table, key).getUninterruptedly();
    }

    @Override
    public int size(String table) {
        return sizeAsync(table).getUninterruptedly();
    }

    @Override
    public Task<MainGroup> createMainGroupAsync(String name) {
        return createMainGroupAsync(name, new ArrayList<>());
    }

    @Override
    public Task<MainGroup> createMainGroupAsync(String name, List<String> subgroups) {
        Task<MainGroup> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> {
            if (this.clusterSyncManager.existsMainGroup(name)) {
                task.complete(null);
                return;
            }

            MainGroup mainGroup = new MainGroup(name, subgroups);
            this.clusterSyncManager.syncMainGroupCreate(mainGroup);
            task.complete(mainGroup);
        });
        return task;
    }

    @Override
    public Task<ProcessGroup> createProcessGroupAsync(String name) {
        return createProcessGroupAsync(name, null);
    }

    @Override
    public Task<ProcessGroup> createProcessGroupAsync(String name, String parent) {
        return createProcessGroupAsync(name, parent, Collections.singletonList(
                new Template(0, "default", FileBackend.NAME,"#", new RuntimeConfiguration(
                        512, new ArrayList<>(), new HashMap<>()
                ), Version.PAPER_1_8_8)
        ));
    }

    @Override
    public Task<ProcessGroup> createProcessGroupAsync(String name, String parent, List<Template> templates) {
        return createProcessGroupAsync(name, parent, templates, new StartupConfiguration(
                -1, 1, 1, 41000, StartupEnvironment.JAVA_RUNTIME, true, new ArrayList<>()
        ));
    }

    @Override
    public Task<ProcessGroup> createProcessGroupAsync(String name, String parent, List<Template> templates, StartupConfiguration startupConfiguration) {
        return createProcessGroupAsync(name, parent, templates, startupConfiguration, new PlayerAccessConfiguration(
                false, "reformcloud.join.maintenance", false,
                null, true, true, true, 50
        ));
    }

    @Override
    public Task<ProcessGroup> createProcessGroupAsync(String name, String parent, List<Template> templates, StartupConfiguration startupConfiguration, PlayerAccessConfiguration playerAccessConfiguration) {
        return createProcessGroupAsync(name, parent, templates, startupConfiguration, playerAccessConfiguration, false);
    }

    @Override
    public Task<ProcessGroup> createProcessGroupAsync(String name, String parent, List<Template> templates, StartupConfiguration startupConfiguration, PlayerAccessConfiguration playerAccessConfiguration, boolean staticGroup) {
        Task<ProcessGroup> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> {
            ProcessGroup processGroup = new ProcessGroup(
                    name,
                    true,
                    parent,
                    startupConfiguration,
                    templates,
                    playerAccessConfiguration,
                    staticGroup
            );
            task.complete(createProcessGroupAsync(processGroup).getUninterruptedly());
        });
        return task;
    }

    @Override
    public Task<ProcessGroup> createProcessGroupAsync(ProcessGroup processGroup) {
        Task<ProcessGroup> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> {
            if (clusterSyncManager.existsProcessGroup(processGroup.getName())) {
                task.complete(null);
                return;
            }

            this.clusterSyncManager.syncProcessGroupCreate(processGroup);
            task.complete(processGroup);
        });
        return task;
    }

    @Override
    public Task<MainGroup> updateMainGroupAsync(MainGroup mainGroup) {
        Task<MainGroup> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> {
            this.clusterSyncManager.syncMainGroupUpdate(mainGroup);
            task.complete(mainGroup);
        });
        return task;
    }

    @Override
    public Task<ProcessGroup> updateProcessGroupAsync(ProcessGroup processGroup) {
        Task<ProcessGroup> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> {
            this.clusterSyncManager.syncProcessGroupUpdate(processGroup);
            task.complete(processGroup);
        });
        return task;
    }

    @Override
    public Task<MainGroup> getMainGroupAsync(String name) {
        Task<MainGroup> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> task.complete(Links.filterToReference(clusterSyncManager.getMainGroups(), e -> e.getName().equals(name)).orNothing()));
        return task;
    }

    @Override
    public Task<ProcessGroup> getProcessGroupAsync(String name) {
        Task<ProcessGroup> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> task.complete(Links.filterToReference(clusterSyncManager.getProcessGroups(), e -> e.getName().equals(name)).orNothing()));
        return task;
    }

    @Override
    public Task<Void> deleteMainGroupAsync(String name) {
        Task<Void> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> {
            this.clusterSyncManager.syncMainGroupDelete(name);
            task.complete(null);
        });
        return task;
    }

    @Override
    public Task<Void> deleteProcessGroupAsync(String name) {
        Task<Void> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> {
            this.clusterSyncManager.syncProcessGroupDelete(name);
            task.complete(null);
        });
        return task;
    }

    @Override
    public Task<List<MainGroup>> getMainGroupsAsync() {
        Task<List<MainGroup>> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> task.complete(Links.newList(clusterSyncManager.getMainGroups())));
        return task;
    }

    @Override
    public Task<List<ProcessGroup>> getProcessGroupsAsync() {
        Task<List<ProcessGroup>> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> task.complete(Links.newList(clusterSyncManager.getProcessGroups())));
        return task;
    }

    @Override
    public MainGroup createMainGroup(String name) {
        return createMainGroupAsync(name).getUninterruptedly();
    }

    @Override
    public MainGroup createMainGroup(String name, List<String> subgroups) {
        return createMainGroupAsync(name, subgroups).getUninterruptedly();
    }

    @Override
    public ProcessGroup createProcessGroup(String name) {
        return createProcessGroupAsync(name).getUninterruptedly();
    }

    @Override
    public ProcessGroup createProcessGroup(String name, String parent) {
        return createProcessGroupAsync(name, parent).getUninterruptedly();
    }

    @Override
    public ProcessGroup createProcessGroup(String name, String parent, List<Template> templates) {
        return createProcessGroupAsync(name, parent, templates).getUninterruptedly();
    }

    @Override
    public ProcessGroup createProcessGroup(String name, String parent, List<Template> templates, StartupConfiguration startupConfiguration) {
        return createProcessGroupAsync(name, parent, templates, startupConfiguration).getUninterruptedly();
    }

    @Override
    public ProcessGroup createProcessGroup(String name, String parent, List<Template> templates, StartupConfiguration startupConfiguration, PlayerAccessConfiguration playerAccessConfiguration) {
        return createProcessGroupAsync(name, parent, templates, startupConfiguration, playerAccessConfiguration).getUninterruptedly();
    }

    @Override
    public ProcessGroup createProcessGroup(String name, String parent, List<Template> templates, StartupConfiguration startupConfiguration, PlayerAccessConfiguration playerAccessConfiguration, boolean staticGroup) {
        return createProcessGroupAsync(name, parent, templates, startupConfiguration, playerAccessConfiguration, staticGroup).getUninterruptedly();
    }

    @Override
    public ProcessGroup createProcessGroup(ProcessGroup processGroup) {
        return createProcessGroupAsync(processGroup).getUninterruptedly();
    }

    @Override
    public MainGroup updateMainGroup(MainGroup mainGroup) {
        return updateMainGroupAsync(mainGroup).getUninterruptedly();
    }

    @Override
    public ProcessGroup updateProcessGroup(ProcessGroup processGroup) {
        return updateProcessGroupAsync(processGroup).getUninterruptedly();
    }

    @Override
    public MainGroup getMainGroup(String name) {
        return getMainGroupAsync(name).getUninterruptedly();
    }

    @Override
    public ProcessGroup getProcessGroup(String name) {
        return getProcessGroupAsync(name).getUninterruptedly();
    }

    @Override
    public void deleteMainGroup(String name) {
        deleteMainGroupAsync(name).awaitUninterruptedly();
    }

    @Override
    public void deleteProcessGroup(String name) {
        deleteProcessGroupAsync(name).awaitUninterruptedly();
    }

    @Override
    public List<MainGroup> getMainGroups() {
        return getMainGroupsAsync().getUninterruptedly();
    }

    @Override
    public List<ProcessGroup> getProcessGroups() {
        return getProcessGroupsAsync().getUninterruptedly();
    }

    @Override
    public Task<Void> sendMessageAsync(UUID player, String message) {
        Task<Void> task = new DefaultTask<>();
        Task.EXECUTOR.execute(() -> {
            ProcessInformation processInformation = this.getPlayerOnProxy(player);
            if (processInformation == null) {
                task.complete(null);
                return;
            }

            if (this.nodeNetworkManager.getCluster().getConnectedNodes().size() == 0) {
                DefaultChannelManager.INSTANCE.get(processInformation.getName()).ifPresent(e -> e.sendPacket(new NodeAPIAction(
                        NodeAPIAction.APIAction.SEND_MESSAGE, Arrays.asList(player, message)
                )));
            } else {
                DefaultChannelManager.INSTANCE.get(processInformation.getParent()).ifPresent(e -> e.sendPacket(new NodeAPIAction(
                        NodeAPIAction.APIAction.SEND_MESSAGE, Arrays.asList(player, message)
                )));
            }

            task.complete(null);
        });
        return task;
    }

    @Override
    public Task<Void> kickPlayerAsync(UUID player, String message) {
        return null;
    }

    @Override
    public Task<Void> kickPlayerFromServerAsync(UUID player, String message) {
        return null;
    }

    @Override
    public Task<Void> playSoundAsync(UUID player, String sound, float f1, float f2) {
        return null;
    }

    @Override
    public Task<Void> sendTitleAsync(UUID player, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        return null;
    }

    @Override
    public Task<Void> playEffectAsync(UUID player, String entityEffect) {
        return null;
    }

    @Override
    public <T> Task<Void> playEffectAsync(UUID player, String effect, T data) {
        return null;
    }

    @Override
    public Task<Void> respawnAsync(UUID player) {
        return null;
    }

    @Override
    public Task<Void> teleportAsync(UUID player, String world, double x, double y, double z, float yaw, float pitch) {
        return null;
    }

    @Override
    public Task<Void> connectAsync(UUID player, String server) {
        return null;
    }

    @Override
    public Task<Void> connectAsync(UUID player, ProcessInformation server) {
        return null;
    }

    @Override
    public Task<Void> connectAsync(UUID player, UUID target) {
        return null;
    }

    @Override
    public Task<Void> setResourcePackAsync(UUID player, String pack) {
        return null;
    }

    @Override
    public void sendMessage(UUID player, String message) {

    }

    @Override
    public void kickPlayer(UUID player, String message) {

    }

    @Override
    public void kickPlayerFromServer(UUID player, String message) {

    }

    @Override
    public void playSound(UUID player, String sound, float f1, float f2) {

    }

    @Override
    public void sendTitle(UUID player, String title, String subTitle, int fadeIn, int stay, int fadeOut) {

    }

    @Override
    public void playEffect(UUID player, String entityEffect) {

    }

    @Override
    public <T> void playEffect(UUID player, String effect, T data) {

    }

    @Override
    public void respawn(UUID player) {

    }

    @Override
    public void teleport(UUID player, String world, double x, double y, double z, float yaw, float pitch) {

    }

    @Override
    public void connect(UUID player, String server) {

    }

    @Override
    public void connect(UUID player, ProcessInformation server) {

    }

    @Override
    public void connect(UUID player, UUID target) {

    }

    @Override
    public void setResourcePack(UUID player, String pack) {

    }

    @Override
    public Task<Void> installPluginAsync(String process, InstallablePlugin plugin) {
        return null;
    }

    @Override
    public Task<Void> installPluginAsync(ProcessInformation process, InstallablePlugin plugin) {
        return null;
    }

    @Override
    public Task<Void> unloadPluginAsync(String process, Plugin plugin) {
        return null;
    }

    @Override
    public Task<Void> unloadPluginAsync(ProcessInformation process, Plugin plugin) {
        return null;
    }

    @Override
    public Task<Plugin> getInstalledPluginAsync(String process, String name) {
        return null;
    }

    @Override
    public Task<Plugin> getInstalledPluginAsync(ProcessInformation process, String name) {
        return null;
    }

    @Override
    public Task<Collection<DefaultPlugin>> getPluginsAsync(String process, String author) {
        return null;
    }

    @Override
    public Task<Collection<DefaultPlugin>> getPluginsAsync(ProcessInformation process, String author) {
        return null;
    }

    @Override
    public Task<Collection<DefaultPlugin>> getPluginsAsync(String process) {
        return null;
    }

    @Override
    public Task<Collection<DefaultPlugin>> getPluginsAsync(ProcessInformation processInformation) {
        return null;
    }

    @Override
    public void installPlugin(String process, InstallablePlugin plugin) {

    }

    @Override
    public void installPlugin(ProcessInformation process, InstallablePlugin plugin) {

    }

    @Override
    public void unloadPlugin(String process, Plugin plugin) {

    }

    @Override
    public void unloadPlugin(ProcessInformation process, Plugin plugin) {

    }

    @Override
    public Plugin getInstalledPlugin(String process, String name) {
        return null;
    }

    @Override
    public Plugin getInstalledPlugin(ProcessInformation process, String name) {
        return null;
    }

    @Override
    public Collection<DefaultPlugin> getPlugins(String process, String author) {
        return null;
    }

    @Override
    public Collection<DefaultPlugin> getPlugins(ProcessInformation process, String author) {
        return null;
    }

    @Override
    public Collection<DefaultPlugin> getPlugins(String process) {
        return null;
    }

    @Override
    public Collection<DefaultPlugin> getPlugins(ProcessInformation processInformation) {
        return null;
    }

    @Override
    public Task<ProcessInformation> startProcessAsync(String groupName) {
        return null;
    }

    @Override
    public Task<ProcessInformation> startProcessAsync(String groupName, String template) {
        return null;
    }

    @Override
    public Task<ProcessInformation> startProcessAsync(String groupName, String template, JsonConfiguration configurable) {
        return null;
    }

    @Override
    public Task<ProcessInformation> stopProcessAsync(String name) {
        return null;
    }

    @Override
    public Task<ProcessInformation> stopProcessAsync(UUID uniqueID) {
        return null;
    }

    @Override
    public Task<ProcessInformation> getProcessAsync(String name) {
        return null;
    }

    @Override
    public Task<ProcessInformation> getProcessAsync(UUID uniqueID) {
        return null;
    }

    @Override
    public Task<List<ProcessInformation>> getAllProcessesAsync() {
        return null;
    }

    @Override
    public Task<List<ProcessInformation>> getProcessesAsync(String group) {
        return null;
    }

    @Override
    public Task<Void> executeProcessCommandAsync(String name, String commandLine) {
        return null;
    }

    @Override
    public Task<Integer> getGlobalOnlineCountAsync(Collection<String> ignoredProxies) {
        return null;
    }

    @Override
    public Task<ProcessInformation> getThisProcessInformationAsync() {
        return null;
    }

    @Override
    public ProcessInformation startProcess(String groupName) {
        return null;
    }

    @Override
    public ProcessInformation startProcess(String groupName, String template) {
        return null;
    }

    @Override
    public ProcessInformation startProcess(String groupName, String template, JsonConfiguration configurable) {
        return null;
    }

    @Override
    public ProcessInformation stopProcess(String name) {
        return null;
    }

    @Override
    public ProcessInformation stopProcess(UUID uniqueID) {
        return null;
    }

    @Override
    public ProcessInformation getProcess(String name) {
        return null;
    }

    @Override
    public ProcessInformation getProcess(UUID uniqueID) {
        return null;
    }

    @Override
    public List<ProcessInformation> getAllProcesses() {
        return null;
    }

    @Override
    public List<ProcessInformation> getProcesses(String group) {
        return null;
    }

    @Override
    public void executeProcessCommand(String name, String commandLine) {

    }

    @Override
    public int getGlobalOnlineCount(Collection<String> ignoredProxies) {
        return 0;
    }

    @Override
    public ProcessInformation getThisProcessInformation() {
        return null;
    }

    @Override
    public void update(ProcessInformation processInformation) {

    }

    @Override
    public void reload() throws Exception {

    }

    private void awaitConnectionsAndUpdate() {
        CommonHelper.EXECUTOR.execute(() -> {
            while (!this.clusterSyncManager.isConnectedAndSyncWithCluster()) {
                AbsoluteThread.sleep(100);
            }

            AbsoluteThread.sleep(100);
            if (this.nodeNetworkManager.getCluster().isSelfNodeHead()) {
                return;
            }

            this.clusterSyncManager.syncProcessGroups(
                    this.nodeExecutorConfig.getProcessGroups(), SyncAction.SYNC
            );
            this.clusterSyncManager.syncMainGroups(
                    this.nodeExecutorConfig.getMainGroups(), SyncAction.SYNC
            );
        });
    }

    private void loadCommands() {
        this.commandManager
                .register(CommandStop.class)
                .register(new CommandReload(this))
                .register(new CommandClear(loggerBase))
                .register(new CommandHelp(commandManager));
    }

    private void loadPacketHandlers() {
        new Reflections("systems.reformcloud.reformcloud2.executor.node.network.packet.in").getSubTypesOf(NetworkHandler.class).forEach(packetHandler::registerHandler);
    }

    private void sendGroups() {
        this.nodeExecutorConfig.getMainGroups().forEach(mainGroup -> System.out.println(LanguageManager.get("loading-main-group", mainGroup.getName())));
        this.nodeExecutorConfig.getProcessGroups().forEach(processGroup -> System.out.println(LanguageManager.get("loading-process-group", processGroup.getName(), processGroup.getParentGroup())));
    }

    private ProcessInformation getPlayerOnProxy(UUID uniqueID) {
        return Links.filter(this.nodeNetworkManager.getNodeProcessHelper().getClusterProcesses(), processInformation -> !processInformation.getTemplate().isServer() && Links.filterToReference(processInformation.getOnlinePlayers(), player -> player.getUniqueID().equals(uniqueID)).isPresent());
    }

    private ProcessInformation getPlayerOnServer(UUID uniqueID) {
        return Links.filter(this.nodeNetworkManager.getNodeProcessHelper().getClusterProcesses(), processInformation -> processInformation.getTemplate().isServer() && Links.filterToReference(processInformation.getOnlinePlayers(), player -> player.getUniqueID().equals(uniqueID)).isPresent());
    }
}
