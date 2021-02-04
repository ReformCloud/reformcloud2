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
package systems.refomcloud.embedded.plugin.bungee.controller;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import org.jetbrains.annotations.NotNull;
import systems.refomcloud.embedded.controller.SharedProxyServerController;
import systems.reformcloud.process.ProcessInformation;

public class BungeeProxyServerController extends SharedProxyServerController {

  public BungeeProxyServerController() {
    ProxyServer.getInstance().getConfig().getListeners().forEach(listenerInfo -> listenerInfo.getServerPriority().clear());
    ProxyServer.getInstance().getConfig().getServers().clear();
  }

  @Override
  protected void registerServer(@NotNull ProcessInformation information) {
    ProxyServer.getInstance().getServers().put(information.getName(), this.constructServerInfo(information));
  }

  @Override
  protected void registerServerWhenUnknown(@NotNull ProcessInformation information) {
    if (ProxyServer.getInstance().getServerInfo(information.getName()) == null) {
      this.registerServer(information);
    }
  }

  @Override
  protected void unregisterServer(@NotNull ProcessInformation information) {
    ProxyServer.getInstance().getServers().remove(information.getName());
  }

  private @NotNull ServerInfo constructServerInfo(@NotNull ProcessInformation processInformation) {
    return ProxyServer.getInstance().constructServerInfo(
      processInformation.getName(),
      processInformation.getHost().toInetSocketAddress(),
      "ReformCloud",
      false
    );
  }
}
