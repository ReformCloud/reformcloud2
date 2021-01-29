/*
 * This file is part of reformcloud, licensed under the MIT License (MIT).
 *
 * Copyright (c) ReformCloud <https://github.com/ReformCloud>
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
package systems.reformcloud.commands.plugin.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import systems.refomcloud.embedded.Embedded;
import systems.reformcloud.commands.application.packet.PacketGetCommandsConfig;
import systems.reformcloud.commands.application.packet.PacketGetCommandsConfigResult;
import systems.reformcloud.commands.plugin.CommandConfigHandler;
import systems.reformcloud.commands.plugin.packet.PacketReleaseCommandsConfig;
import systems.reformcloud.commands.plugin.velocity.handler.VelocityCommandConfigHandler;
import systems.reformcloud.ExecutorAPI;
import systems.reformcloud.network.PacketIds;
import systems.reformcloud.network.packet.PacketProvider;

@Plugin(
  id = "reformcloud_commands",
  name = "ReformCloudCommands",
  version = "3.0",
  description = "Get access to default reformcloud commands",
  url = "https://reformcloud.systems",
  authors = {"derklaro"},
  dependencies = {@Dependency(id = "reformcloud_api_executor")}
)
public class VelocityPlugin {

  private final ProxyServer proxyServer;

  @Inject
  public VelocityPlugin(ProxyServer proxyServer) {
    this.proxyServer = proxyServer;
  }

  @Subscribe
  public void handle(ProxyInitializeEvent event) {
    CommandConfigHandler.setInstance(new VelocityCommandConfigHandler(this.proxyServer));

    ExecutorAPI.getInstance().getServiceRegistry().getProviderUnchecked(PacketProvider.class).registerPacket(PacketGetCommandsConfigResult.class);
    ExecutorAPI.getInstance().getServiceRegistry().getProviderUnchecked(PacketProvider.class).registerPacket(PacketReleaseCommandsConfig.class);

    Embedded.getInstance().sendSyncQuery(new PacketGetCommandsConfig()).ifPresent(e -> {
      if (e instanceof PacketGetCommandsConfigResult) {
        CommandConfigHandler.getInstance().handleCommandConfigRelease(((PacketGetCommandsConfigResult) e).getCommandsConfig());
      }
    });
  }

  @Subscribe
  public void handle(ProxyShutdownEvent event) {
    CommandConfigHandler.getInstance().unregisterAllCommands();
    ExecutorAPI.getInstance().getServiceRegistry().getProviderUnchecked(PacketProvider.class).unregisterPacket(PacketIds.RESERVED_EXTRA_BUS + 3);
    ExecutorAPI.getInstance().getServiceRegistry().getProviderUnchecked(PacketProvider.class).unregisterPacket(PacketIds.RESERVED_EXTRA_BUS + 2);
  }
}