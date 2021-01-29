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
package systems.reformcloud.group.main;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import systems.reformcloud.ExecutorAPI;
import systems.reformcloud.builder.MainGroupBuilder;
import systems.reformcloud.network.data.SerializableObject;
import systems.reformcloud.utility.name.Nameable;
import systems.reformcloud.configuration.data.JsonDataHolder;

import java.util.Collection;
import java.util.Optional;

public interface MainGroup extends Nameable, JsonDataHolder<MainGroup>, SerializableObject, Cloneable {

  @NotNull
  @Contract(value = "_ -> new", pure = true)
  static MainGroupBuilder builder(@NotNull String name) {
    return ExecutorAPI.getInstance().getMainGroupProvider().createMainGroup(name);
  }

  @NotNull
  static Optional<MainGroup> getByName(@NotNull String name) {
    return ExecutorAPI.getInstance().getMainGroupProvider().getMainGroup(name);
  }

  @NotNull
  @UnmodifiableView
  Collection<String> getSubGroups();

  void setSubGroups(@NotNull Collection<String> subGroups);

  void addSubGroup(@NotNull String subGroup);

  void removeSubGroup(@NotNull String subGroup);

  boolean hasSubGroup(@NotNull String name);

  void removeAllSubGroups();

  void update();

  @NotNull
  MainGroup clone();
}