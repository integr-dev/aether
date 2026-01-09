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


import net.integr.aether.common.connection.Connection
import net.integr.aether.common.packet.Packet
import net.integr.aether.common.packet.security.AesTool
import java.net.Socket

abstract class AetherClient protected constructor(address: String, port: Int, aesHandler: AesTool.AesHandler? = null) : AutoCloseable {
    protected val clientSocket = Socket(address, port)

    val onServerConnected = mutableListOf<() -> Unit>()
    val onServerDisconnected = mutableListOf<() -> Unit>()
    val onServerSendInvalid = mutableListOf<(connection: Connection, message: String) -> Unit>()

    val onClose = mutableListOf<() -> Unit>()

    val onPacketReceived = mutableListOf<(connection: Connection, objectId: Int, buffer: ByteArray) -> Unit>()

    val connection = Connection.fromSocket(clientSocket)

    protected fun handleServerConnection() {
        try {
            while (connection.isConnected()) {
                val (objectId, buffer) = connection.readPacketBuffer()

                onPacketReceived.forEach { it.invoke(connection, objectId, buffer) }
            }
        } catch (e: IllegalArgumentException) {
            onServerSendInvalid.forEach { it.invoke(connection, e.message ?: "Unknown error.") }
            connection.close()
        } catch (_: Exception) {
            connection.close()
        }

    }
    inline fun <reified T> send(data: T, objectId: Int = 0) {
        val wrappingPacket = Packet(data)
        connection.writePacket(wrappingPacket, objectId)
    }

    fun allPacketsProcessed(): Boolean {
        return connection.allPacketsProcessed()
    }

    companion object {
        val suspended = SuspendedAetherClient.Companion
    }
}