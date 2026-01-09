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

import net.integr.aether.common.connection.Connection
import net.integr.aether.common.packet.Packet
import net.integr.aether.common.packet.security.AesTool
import java.net.ServerSocket
import java.util.concurrent.CopyOnWriteArrayList

abstract class AetherServer protected constructor(port: Int, aesHandler: AesTool.AesHandler? = null) : AutoCloseable {
    protected val serverSocket = ServerSocket(port)

    protected val internalClients = CopyOnWriteArrayList<Connection>()

    val clients: List<Connection>
        get() = internalClients.toList()

    val onClientConnected = mutableListOf<(connection: Connection) -> Unit>()
    val onClientDisconnected = mutableListOf<(connection: Connection) -> Unit>()
    val onClientSendInvalid = mutableListOf<(connection: Connection, message: String) -> Unit>()

    val onClose = mutableListOf<() -> Unit>()

    val onPacketReceived = mutableListOf<(connection: Connection, objectId: Int, buffer: ByteArray) -> Unit>()

    protected fun handleClientConnection(connection: Connection) {
        while (connection.isConnected()) {
            try {
                val (objectId, buffer) = connection.readPacketBuffer()

                onPacketReceived.forEach { it.invoke(connection, objectId, buffer) }
            } catch (e: IllegalArgumentException) {
                internalClients.remove(connection)
                onClientSendInvalid.forEach { it.invoke(connection, e.message ?: "Unknown error.") }
                break
            } catch (_: Exception) {
                internalClients.remove(connection)
                break
            }
        }

        internalClients.remove(connection)
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