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

package net.integr.aether.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.integr.aether.client.AetherClient
import net.integr.aether.common.packet.security.AesTool
import net.integr.aether.common.registry.ObjectRegistry
import net.integr.aether.server.AetherServer

fun main() {
    val encryptor = AesTool.generateHandler()

    runBlocking {
        launch(Dispatchers.IO) {
            println("[SERVER] Starting Aether Server on port 9999...")

            AetherServer.suspended.startEncrypted(9999, encryptor) {
                val registry = ObjectRegistry()

                registry.useHandler<TestObject1> { packet, id ->
                    println("[SERVER] Received TestObject1 with message: ${packet.payload.message} at ${packet.timestamp}")

                    broadcast(packet.payload, id)
                }

                registry.useHandler<TestObject2> { packet, id ->
                    println("[SERVER] Received TestObject2 with number: ${packet.payload.number} at ${packet.timestamp}")
                }


                onClientConnected += { connection ->
                    println("[SERVER] Client connected: ${connection.socket.inetAddress.hostAddress}")
                }

                onClientDisconnected += { connection ->
                    println("[SERVER] Client disconnected: ${connection.socket.inetAddress.hostAddress}")
                }

                onClientSendInvalid += { connection, message ->
                    println("[SERVER] Client sent invalid data: $message")
                }

                onClose += {
                    println("[SERVER] Server is closing.")
                }

                onPacketReceived += { connection, objectId, buffer ->

                    println("[SERVER] Packet received with object ID: $objectId")

                    registry.handle(objectId, buffer)
                }
            }
        }

        launch(Dispatchers.IO) { // Example of a client for one use
            println("[CLIENT 1] Starting Aether Client and connecting to server...")

            val client = AetherClient.suspended.startEncrypted("localhost", 9999, encryptor) {
                onClose += {
                    println("[CLIENT 1] Client is closing.")
                }
            }

            println("[CLIENT 1] Sending test objects to server...")

            client.use {
                client.send(TestObject1("Hello, Aether!"), TEST_OBJECT_1_ID)
                client.send(TestObject2(42), TEST_OBJECT_2_ID)
            }
        }
    }
}