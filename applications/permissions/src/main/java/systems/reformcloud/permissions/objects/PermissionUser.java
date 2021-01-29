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
package systems.reformcloud.permissions.objects;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import systems.reformcloud.configuration.JsonConfiguration;
import systems.reformcloud.network.data.ProtocolBuffer;
import systems.reformcloud.network.data.SerializableObject;
import systems.reformcloud.permissions.PermissionManagement;
import systems.reformcloud.permissions.nodes.NodeGroup;
import systems.reformcloud.permissions.objects.holder.DefaultPermissionHolder;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class PermissionUser extends DefaultPermissionHolder implements SerializableObject {

  private UUID uuid;
  private Collection<NodeGroup> groups;
  private @Nullable String prefix;
  private @Nullable String suffix;
  private @Nullable String display;
  private @Nullable String colour;
  private JsonConfiguration extra;

  @ApiStatus.Internal
  public PermissionUser() {
  }

  public PermissionUser(@NotNull UUID uuid, @NotNull Collection<NodeGroup> groups) {
    this.uuid = uuid;
    this.groups = groups;
    this.extra = JsonConfiguration.newJsonConfiguration();
  }

  @NotNull
  public UUID getUniqueID() {
    return this.uuid;
  }

  @NotNull
  public Collection<NodeGroup> getGroups() {
    return this.groups;
  }

  @NotNull
  public Optional<String> getPrefix() {
    if (this.prefix == null) {
      return this.getHighestPermissionGroup().flatMap(PermissionGroup::getPrefix);
    }

    return Optional.of(this.prefix);
  }

  public void setPrefix(@Nullable String prefix) {
    this.prefix = prefix;
  }

  @NotNull
  public Optional<String> getSuffix() {
    if (this.suffix == null) {
      return this.getHighestPermissionGroup().flatMap(PermissionGroup::getSuffix);
    }

    return Optional.of(this.suffix);
  }

  public void setSuffix(@Nullable String suffix) {
    this.suffix = suffix;
  }

  @NotNull
  public Optional<String> getDisplay() {
    if (this.display == null) {
      return this.getHighestPermissionGroup().flatMap(PermissionGroup::getDisplay);
    }

    return Optional.of(this.display);
  }

  public void setDisplay(@Nullable String display) {
    this.display = display;
  }

  @NotNull
  public Optional<String> getColour() {
    if (this.colour == null) {
      return this.getHighestPermissionGroup().flatMap(PermissionGroup::getColour);
    }

    return Optional.of(this.colour);
  }

  public void setColour(@Nullable String colour) {
    this.colour = colour;
  }

  @NotNull
  public JsonConfiguration getExtra() {
    return this.extra == null ? JsonConfiguration.newJsonConfiguration() : this.extra;
  }

  @NotNull
  public Optional<PermissionGroup> getHighestPermissionGroup() {
    PermissionGroup permissionGroup = null;

    for (NodeGroup nodeGroup : this.groups) {
      PermissionGroup group = PermissionManagement.getInstance().getPermissionGroup(nodeGroup.getGroupName()).orElse(null);
      if (group == null) {
        continue;
      }

      if (permissionGroup == null) {
        permissionGroup = group;
      } else if (permissionGroup.getPriority() > group.getPriority()) {
        permissionGroup = group;
      }
    }

    return Optional.ofNullable(permissionGroup);
  }

  public boolean isInGroup(@NotNull String group) {
    for (NodeGroup nodeGroup : this.groups) {
      if (nodeGroup.getGroupName().equals(group) && nodeGroup.isValid()) {
        return true;
      }
    }

    return false;
  }

  @Override
  public @Nullable Boolean hasPermission(@NotNull String permission) {
    final Boolean result = super.hasPermission(permission);
    return result == null ? PermissionManagement.getInstance().hasPermission(this, permission) : result;
  }

  @Override
  public void write(@NotNull ProtocolBuffer buffer) {
    buffer.writeUniqueId(this.uuid);
    buffer.writeObjects(this.groups);

    buffer.writeString(this.prefix);
    buffer.writeString(this.suffix);
    buffer.writeString(this.display);
    buffer.writeString(this.colour);
    buffer.writeArray(this.getExtra().toPrettyBytes());

    super.write(buffer);
  }

  @Override
  public void read(@NotNull ProtocolBuffer buffer) {
    this.uuid = buffer.readUniqueId();
    this.groups = buffer.readObjects(NodeGroup.class);

    this.prefix = buffer.readString();
    this.suffix = buffer.readString();
    this.display = buffer.readString();
    this.colour = buffer.readString();
    this.extra = JsonConfiguration.newJsonConfiguration(buffer.readArray());

    super.read(buffer);
  }
}