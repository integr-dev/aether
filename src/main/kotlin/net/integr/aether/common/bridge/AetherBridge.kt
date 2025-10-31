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

package net.integr.aether.common.bridge


import net.integr.aether.common.eon.auto.Eon
import net.integr.aether.common.packet.Packet
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

class AetherBridge private constructor(val socket: Socket) : AutoCloseable {
    val outputStream: OutputStream = socket.getOutputStream()
    val inputStream: InputStream = socket.getInputStream()

    companion object {
        fun fromSocket(socket: Socket): AetherBridge {
            return AetherBridge(socket)
        }
    }

    fun write(data: ByteArray) {
        outputStream.write(data)
        outputStream.flush()
    }

    fun read(byteAmount: Int): ByteArray {
        return inputStream.readNBytes(byteAmount)
    }

    fun readPacketBuffer(): Pair<Int, ByteArray> {
        val length = Eon.readIntFromStream(inputStream)
        val objectId = Eon.readIntFromStream(inputStream)
        val packetData = read(length - Int.SIZE_BYTES - Int.SIZE_BYTES) // Subtract size of length int
        return objectId to packetData
    }

    inline fun <reified T> readPacketWithPayloadType(): Packet<T> {
        val (_, buffer) = readPacketBuffer()
        return Eon.decodeNoMetadata<Packet<T>>(buffer)
    }

    inline fun <reified T> decodeWithPayloadType(buffer: ByteArray): Packet<T> {
        return Eon.decodeNoMetadata<Packet<T>>(buffer)
    }

    inline fun <reified T> writePacket(packet: Packet<T>, objectId: Int = 0) {
        val buffer = Eon.encode(packet, objectId)
        write(buffer)
    }

    fun isConnected(): Boolean {
        return socket.isConnected && !socket.isClosed
    }

    fun allPacketsProcessed(): Boolean {
        return inputStream.available() == 0
    }

    override fun close() {
        outputStream.close()
        inputStream.close()
        socket.close()
    }
}