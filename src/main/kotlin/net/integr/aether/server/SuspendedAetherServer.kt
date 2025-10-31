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

package net.integr.aether.server

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.integr.aether.common.bridge.AetherBridge

class SuspendedAetherServer private constructor(port: Int) : AetherServer(port) {
    companion object {
        suspend fun start(port: Int, hooks: SuspendedAetherServer.() -> Unit): SuspendedAetherServer {
            val server = SuspendedAetherServer(port)

            server.hooks()
            server.startup()
            return server
        }

        fun getStartable(port: Int, hooks: SuspendedAetherServer.() -> Unit): SuspendedAetherServer {
            val server = SuspendedAetherServer(port)

            server.hooks()
            return server
        }
    }

    suspend fun startup() {
        coroutineScope {
            try {
                while (!serverSocket.isClosed) {
                    val connection = serverSocket.accept()
                    val bridge = AetherBridge.fromSocket(connection)
                    internalClients.add(bridge)
                    onClientConnected.forEach { it.invoke(bridge) }

                    launch {
                        handleClientConnection(bridge)

                        onClientDisconnected.forEach { it.invoke(bridge) }
                        bridge.close()
                    }
                }
            } catch (e: Exception) {
                // Server socket closed or error occurred, assume shutdown
                serverSocket.close()
            }
        }
    }

    override fun close() {
        onClose.forEach { it.invoke() }
        serverSocket.close()
    }
}