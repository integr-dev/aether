/*
 * Copyright © 2025 Integr
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.integr.aether.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SuspendedAetherClient private constructor(address: String, port: Int) : AetherClient(address, port) {
    private val clientCoroutineScope = CoroutineScope(Dispatchers.IO + Job())

    companion object {
        fun start(address: String, port: Int, hooks: AetherClient.() -> Unit): SuspendedAetherClient {
            val client = SuspendedAetherClient(address, port)

            client.hooks()
            client.startup()

            return client
        }
    }

    private fun startup() {
        clientCoroutineScope.launch {
            onServerConnected.forEach { it.invoke() }
            handleServerConnection()
            onServerDisconnected.forEach { it.invoke() }
        }
    }

    override fun close() {
        onClose.forEach { it.invoke() }
        bridge.close()
        clientCoroutineScope.coroutineContext[Job]?.cancel()
    }
}