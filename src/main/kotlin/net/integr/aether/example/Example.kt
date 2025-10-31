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
import kotlinx.serialization.Serializable
import net.integr.aether.client.AetherClient
import net.integr.aether.server.AetherServer

fun example() {
    runBlocking {
        launch(Dispatchers.IO) {
            println("[SERVER] Starting Aether Server on port 9999...")

            AetherServer.suspended.start(9999) {
                onClientConnected += { bridge ->
                    println("[SERVER] Client connected: ${bridge.socket.inetAddress.hostAddress}")
                }

                onClientDisconnected += { bridge ->
                    println("[SERVER] Client disconnected: ${bridge.socket.inetAddress.hostAddress}")
                }

                onClose += {
                    println("[SERVER] Server is closing.")
                }

                onPacketReceived += { bridge ->
                    val (objectId, buffer) = bridge.readPacketBuffer()

                    println("[SERVER] Packet received with object ID: $objectId")

                    when (objectId) {
                        TEST_OBJECT_1_ID -> {
                            val packet = bridge.decodeWithPayloadType<TestObject1>(buffer)
                            val obj = packet.payload
                            val timestamp = packet.timestamp
                            println("[SERVER] Received TestObject1 with message: ${obj.message} at $timestamp")

                            this.broadcast(obj, TEST_OBJECT_1_ID)
                        }

                        TEST_OBJECT_2_ID -> {
                            val packet = bridge.decodeWithPayloadType<TestObject2>(buffer)
                            val obj = packet.payload
                            val timestamp = packet.timestamp
                            println("[SERVER] Received TestObject2 with number: ${obj.number} at $timestamp")
                        }

                        else -> {
                            println("[SERVER] Received unknown object ID: $objectId")
                        }
                    }
                }
            }
        }

        launch(Dispatchers.IO) { // Example of a client for one use
            println("[CLIENT 1] Starting Aether Client and connecting to server...")

            val client = AetherClient.suspended.start("localhost", 9999) {
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

        launch(Dispatchers.IO) { // Example of a client listening for incoming packets
            println("[CLIENT 2] Starting Aether Client and connecting to server...")

            val client = AetherClient.suspended.start("localhost", 9999) {
                onClose += {
                    println("[CLIENT 2] Client is closing.")
                }

                onPacketReceived += { bridge ->
                    val (objectId, buffer) = bridge.readPacketBuffer()

                    println("[CLIENT 2] Packet received with object ID: $objectId")
                }
            }
        }
    }
}

const val TEST_OBJECT_1_ID = 1
const val TEST_OBJECT_2_ID = 2

@Serializable
class TestObject1(val message: String)

@Serializable
class TestObject2(val number: Int)