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

import net.integr.aether.common.bridge.AetherBridge
import net.integr.aether.common.packet.Packet
import java.net.ServerSocket

abstract class AetherServer protected constructor(val port: Int) : AutoCloseable {
    protected val serverSocket = ServerSocket(port)

    protected val internalClients = mutableListOf<AetherBridge>()

    val clients: List<AetherBridge>
        get() = internalClients.toList()


    val onClientConnected = mutableListOf<(bridge: AetherBridge) -> Unit>()
    val onClientDisconnected = mutableListOf<(bridge: AetherBridge) -> Unit>()

    val onClose = mutableListOf<() -> Unit>()

    val onPacketReceived = mutableListOf<(bridge: AetherBridge) -> Unit>()

    protected fun handleClientConnection(bridge: AetherBridge) {
        while (bridge.isConnected()) {
            try {
                onPacketReceived.forEach { it.invoke(bridge) }
            } catch (e: Exception) {
                internalClients.remove(bridge)
                break
            }
        }

        internalClients.remove(bridge)
    }

    inline fun <reified T> broadcast(data: T, objectId: Int = 0) {
        val wrappingPacket = Packet(data)
        clients.forEach { it.writePacket(wrappingPacket, objectId) }
    }

    fun allPacketsProcessed(): Boolean {
        return internalClients.all { it.allPacketsProcessed() }
    }


    companion object {
        val suspended = SuspendedAetherServer.Companion
    }
}