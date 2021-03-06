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
package systems.reformcloud.reformcloud2.cloudflare.listener;

import systems.reformcloud.reformcloud2.cloudflare.api.CloudFlareHelper;
import systems.reformcloud.reformcloud2.executor.api.event.events.process.ProcessRegisterEvent;
import systems.reformcloud.reformcloud2.executor.api.event.events.process.ProcessUnregisterEvent;
import systems.reformcloud.reformcloud2.executor.api.event.events.process.ProcessUpdateEvent;
import systems.reformcloud.reformcloud2.executor.api.event.handler.Listener;

public final class ProcessListener {

    @Listener
    public void handle(ProcessRegisterEvent event) {
        if (!CloudFlareHelper.shouldHandle(event.getProcessInformation()) || !event.getProcessInformation().getNetworkInfo().isConnected()) {
            return;
        }

        CloudFlareHelper.createForProcess(event.getProcessInformation());
    }

    @Listener
    public void handle(ProcessUpdateEvent event) {
        if (!CloudFlareHelper.shouldHandle(event.getProcessInformation())) {
            return;
        }

        if (!event.getProcessInformation().getNetworkInfo().isConnected() && CloudFlareHelper.hasEntry(event.getProcessInformation())) {
            CloudFlareHelper.deleteRecord(event.getProcessInformation());
        } else if (event.getProcessInformation().getNetworkInfo().isConnected() && !CloudFlareHelper.hasEntry(event.getProcessInformation())) {
            CloudFlareHelper.createForProcess(event.getProcessInformation());
        }
    }

    @Listener
    public void handle(ProcessUnregisterEvent event) {
        CloudFlareHelper.deleteRecord(event.getProcessInformation());
    }
}
