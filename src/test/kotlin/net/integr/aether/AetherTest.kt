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

package net.integr.aether

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import net.integr.aether.client.AetherClient
import net.integr.aether.server.AetherServer
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.random.Random

class AetherTest {
    @Test
    fun `Test Suspended Clientbound`() {

        val server = AetherServer.suspended.getStartable(7779) {}

        runBlocking {
            launch(Dispatchers.IO) {
                server.startup()
            }

            launch {
                val client1 = AetherClient.suspended.start("localhost", 7779) {
                    clientHooks()
                }

                val client2 = AetherClient.suspended.start("localhost", 7779) {
                    clientHooks()
                }

                testClientBound(server)

                while (!client1.allPacketsProcessed() || !client2.allPacketsProcessed() || !server.allPacketsProcessed()) {
                    Thread.sleep(100)
                }

                client1.close()
                client2.close()
                server.close()
            }
        }
    }

    @Test
    fun `Test Suspended Serverbound`() {
        val server = AetherServer.suspended.getStartable(7780) {
            serverHooks()
        }

        runBlocking {
            launch(Dispatchers.IO) {
                server.startup()
            }

            launch {
                val client1 = AetherClient.suspended.start("localhost", 7780) {}

                val client2 = AetherClient.suspended.start("localhost", 7780) {}

                testServerBound(client1, client2)

                while (!client1.allPacketsProcessed() || !client2.allPacketsProcessed() || !server.allPacketsProcessed()) {
                    Thread.sleep(100)
                }

                client1.close()
                client2.close()
                server.close()
            }
        }
    }

    fun AetherServer.serverHooks() {
        val correctResult = TestData(10, "Hello")
        var receivedCount = 0

        onPacketReceived += { bridge ->
            val (objectId, buffer) = bridge.readPacketBuffer()
            when (objectId) {
                1 -> {
                    val payload = bridge.decodeWithPayloadType<TestData>(buffer).payload
                    println("Server received: $payload")

                    Assertions.assertEquals(payload.number, correctResult.number)
                    Assertions.assertEquals(payload.text, correctResult.text)

                    receivedCount++
                }
                else -> {
                    Assertions.fail("Unexpected object ID: $objectId")
                }
            }
        }

        onClose += {
            println("Server closed after receiving $receivedCount packets.")
            Assertions.assertEquals(100, receivedCount)
        }
    }

    fun AetherClient.clientHooks() {
        val correctResult = TestData(10, "Hello")
        var receivedCount = 0

        onPacketReceived += { bridge ->
            val (objectId, buffer) = bridge.readPacketBuffer()
            when (objectId) {
                1 -> {
                    val payload = bridge.decodeWithPayloadType<TestData>(buffer).payload
                    println("Client received: $payload")

                    Assertions.assertEquals(payload.number, correctResult.number)
                    Assertions.assertEquals(payload.text, correctResult.text)

                    receivedCount++
                }
                else -> {
                    Assertions.fail("Unexpected object ID: $objectId")
                }
            }
        }

        onClose += {
            println("Client closed after receiving $receivedCount packets.")
            Assertions.assertEquals(100, receivedCount)
        }
    }

    fun testClientBound(server: AetherServer) {
        println("Testing clientbound with delayed sends")

        repeat(50) {
            server.broadcast(TestData(10, "Hello"), 1)
            Thread.sleep(100 * Random.nextLong(5))
        }

        println("Testing clientbound with immediate sends")

        repeat(50) {
            server.broadcast(TestData(10, "Hello"), 1)
        }
    }

    fun testServerBound(client1: AetherClient, client2: AetherClient) {
        println("Testing serverbound with delayed sends")

        repeat(25) {
            client1.send(TestData(10, "Hello"), 1)
            Thread.sleep(100 * Random.nextLong(5))

            client2.send(TestData(10, "Hello"), 1)
            Thread.sleep(100 * Random.nextLong(5))
        }

        println("Testing serverbound with immediate sends")

        repeat(25) {
            client1.send(TestData(10, "Hello"), 1)
            client2.send(TestData(10, "Hello"), 1)
        }
    }
}

@Serializable
data class TestData(
    val number: Int,
    val text: String
)