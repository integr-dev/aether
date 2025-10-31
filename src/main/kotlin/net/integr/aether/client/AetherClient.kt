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


import net.integr.aether.common.bridge.AetherBridge
import net.integr.aether.common.packet.Packet
import java.net.Socket

abstract class AetherClient protected constructor(val address: String, val port: Int) : AutoCloseable {
    protected val clientSocket = Socket(address, port)

    protected val isActiveConnection: Boolean = true

    val onServerConnected = mutableListOf<() -> Unit>()
    val onServerDisconnected = mutableListOf<() -> Unit>()

    val onClose = mutableListOf<() -> Unit>()

    val onPacketReceived = mutableListOf<(bridge: AetherBridge) -> Unit>()

    val bridge = AetherBridge.fromSocket(clientSocket)

    protected fun handleServerConnection() {
        while (bridge.isConnected() && isActiveConnection) {
            try {
                onPacketReceived.forEach { it.invoke(bridge) }
            } catch (e: Exception) {
                break
            }
        }
    }
    inline fun <reified T> send(data: T, objectId: Int = 0) {
        val wrappingPacket = Packet(data)
        bridge.writePacket(wrappingPacket, objectId)
    }

    fun allPacketsProcessed(): Boolean {
        return bridge.allPacketsProcessed()
    }

    companion object {
        val suspended = SuspendedAetherClient.Companion
    }
}