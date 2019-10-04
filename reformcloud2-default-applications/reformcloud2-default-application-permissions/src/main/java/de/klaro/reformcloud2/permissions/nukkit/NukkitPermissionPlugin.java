package de.klaro.reformcloud2.permissions.nukkit;

import cn.nukkit.Server;
import cn.nukkit.plugin.PluginBase;
import de.klaro.reformcloud2.permissions.nukkit.listeners.NukkitPermissionListener;
import de.klaro.reformcloud2.permissions.packets.PacketHelper;

public class NukkitPermissionPlugin extends PluginBase {

    @Override
    public void onEnable() {
        Server.getInstance().getPluginManager().registerEvents(new NukkitPermissionListener(), this);
    }

    @Override
    public void onDisable() {
        PacketHelper.unregisterAPIPackets();
        Server.getInstance().getScheduler().cancelTask(this);
    }
}
