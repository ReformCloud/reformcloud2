package systems.reformcloud.reformcloud2.executor.api.spigot.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import systems.reformcloud.reformcloud2.executor.api.common.ExecutorAPI;
import systems.reformcloud.reformcloud2.executor.api.common.groups.utils.PlayerAccessConfiguration;
import systems.reformcloud.reformcloud2.executor.api.common.network.channel.PacketSender;
import systems.reformcloud.reformcloud2.executor.api.common.network.channel.manager.DefaultChannelManager;
import systems.reformcloud.reformcloud2.executor.api.common.network.packet.Packet;
import systems.reformcloud.reformcloud2.executor.api.common.process.ProcessInformation;
import systems.reformcloud.reformcloud2.executor.api.common.process.ProcessState;
import systems.reformcloud.reformcloud2.executor.api.packets.out.APIPacketOutHasPlayerAccess;
import systems.reformcloud.reformcloud2.executor.api.spigot.SpigotExecutor;

import java.util.concurrent.TimeUnit;

public final class PlayerListenerHandler implements Listener {

    @EventHandler (priority = EventPriority.HIGH)
    public void handle(final PlayerLoginEvent event) {
        if (ExecutorAPI.getInstance().getThisProcessInformation().getProcessGroup().getPlayerAccessConfiguration().isOnlyProxyJoin()) {
            PacketSender packetSender = DefaultChannelManager.INSTANCE.get("Controller").orElse(null);
            if (packetSender == null || !ExecutorAPI.getInstance().getThisProcessInformation().getNetworkInfo().isConnected()) {
                event.setResult(PlayerLoginEvent.Result.KICK_BANNED);
                event.setKickMessage("§4§lThe current server is not connected to the controller");
                return;
            }

            Packet result = SpigotExecutor.getInstance().packetHandler().getQueryHandler().sendQueryAsync(packetSender, new APIPacketOutHasPlayerAccess(
                    event.getPlayer().getUniqueId(),
                    event.getPlayer().getName()
            )).getTask().getUninterruptedly(TimeUnit.SECONDS, 2);
            if (result != null && result.content().getBoolean("access")) {
                event.setResult(PlayerLoginEvent.Result.ALLOWED);
            } else {
                event.setKickMessage("§4You have to connect through an internal proxy server");
                event.setResult(PlayerLoginEvent.Result.KICK_BANNED);
            }
        }

        final Player player = event.getPlayer();
        final ProcessInformation current = ExecutorAPI.getInstance().getThisProcessInformation();
        final PlayerAccessConfiguration configuration = current.getProcessGroup().getPlayerAccessConfiguration();

        if (configuration.isUseCloudPlayerLimit()
                && configuration.getMaxPlayers() < current.getOnlineCount() + 1
                && !player.hasPermission("reformcloud.join.full")) {
            event.setKickMessage("§4§lThe server is full");
            event.setResult(PlayerLoginEvent.Result.KICK_BANNED);
            return;
        }

        if (configuration.isJoinOnlyPerPermission()
                && configuration.getJoinPermission() != null
                && !player.hasPermission(configuration.getJoinPermission())) {
            event.setKickMessage("§4§lYou do not have permission to enter this server");
            event.setResult(PlayerLoginEvent.Result.KICK_BANNED);
            return;
        }

        if (configuration.isMaintenance()
                && configuration.getMaintenanceJoinPermission() != null
                && !player.hasPermission(configuration.getMaintenanceJoinPermission())) {
            event.setKickMessage("§4§lThis server is currently in maintenance");
            event.setResult(PlayerLoginEvent.Result.KICK_BANNED);
            return;
        }

        if (current.getProcessState().equals(ProcessState.FULL) && !player.hasPermission("reformcloud.join.full")) {
            event.setKickMessage("§4§lYou are not allowed to join this server in the current state");
            event.setResult(PlayerLoginEvent.Result.KICK_BANNED);
            return;
        }

        if (Bukkit.getOnlinePlayers().size() >= current.getMaxPlayers()
                && !current.getProcessState().equals(ProcessState.FULL)
                && !current.getProcessState().equals(ProcessState.INVISIBLE)) {
            current.setProcessState(ProcessState.FULL);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void handle(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final ProcessInformation current = ExecutorAPI.getInstance().getThisProcessInformation();

        current.updateRuntimeInformation();
        current.onLogin(player.getUniqueId(), player.getName());
        SpigotExecutor.getInstance().setThisProcessInformation(current);
        ExecutorAPI.getInstance().update(current);
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void handle(final PlayerQuitEvent event) {
        ProcessInformation current = ExecutorAPI.getInstance().getThisProcessInformation();
        if (!current.isPlayerOnline(event.getPlayer().getUniqueId())) {
            return;
        }

        if (Bukkit.getOnlinePlayers().size() < current.getMaxPlayers()
                && !current.getProcessState().equals(ProcessState.READY)
                && !current.getProcessState().equals(ProcessState.INVISIBLE)) {
            current.setProcessState(ProcessState.READY);
        }

        current.updateRuntimeInformation();
        current.onLogout(event.getPlayer().getUniqueId());
        SpigotExecutor.getInstance().setThisProcessInformation(current);
        ExecutorAPI.getInstance().update(current);
    }
}