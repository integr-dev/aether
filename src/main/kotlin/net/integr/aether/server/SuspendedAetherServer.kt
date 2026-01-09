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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.integr.aether.common.connection.Connection
import net.integr.aether.common.packet.security.AesTool

class SuspendedAetherServer private constructor(port: Int, aesHandler: AesTool.AesHandler? = null) : AetherServer(port, aesHandler) {
    private val serverCoroutineScope = CoroutineScope(Dispatchers.IO + Job())

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

        suspend fun startEncrypted(port: Int, aesHandler: AesTool.AesHandler, hooks: SuspendedAetherServer.() -> Unit): SuspendedAetherServer {
            val server = SuspendedAetherServer(port, aesHandler)

            server.hooks()
            server.startup()
            return server
        }

        fun getStartableEncrypted(port: Int, aesHandler: AesTool.AesHandler, hooks: SuspendedAetherServer.() -> Unit): SuspendedAetherServer {
            val server = SuspendedAetherServer(port, aesHandler)

            server.hooks()
            return server
        }
    }

    suspend fun startup() {
        val job = serverCoroutineScope.launch {
            try {
                while (!serverSocket.isClosed) {
                    val socketConnection = serverSocket.accept()
                    val connection = Connection.fromSocket(socketConnection)

                    internalClients.add(connection)
                    onClientConnected.forEach { it.invoke(connection) }

                    launch {
                        try {
                            handleClientConnection(connection)
                        } finally {
                            onClientDisconnected.forEach { it.invoke(connection) }
                            connection.close()
                        }
                    }
                }
            } catch (_: Exception) {
                // Server socket closed or error occurred, assume shutdown
                serverSocket.close()
            } finally {
                // Socket is closed, perform cleanup
                serverSocket.close()
            }
        }

        job.join()
    }

    override fun close() {
        onClose.forEach { it.invoke() }
        internalClients.forEach { it.close() }
        serverSocket.close()
        serverCoroutineScope.cancel()
    }
}