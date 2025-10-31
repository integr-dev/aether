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

package net.integr.aether.common.eon.auto

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.serializer
import net.integr.aether.common.eon.EonReader
import net.integr.aether.common.eon.EonWriter
import java.io.InputStream

class Eon {

    fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T, objectId: Int = 0): ByteArray {
        val writer = EonWriter.begin(objectId)
        serializer.serialize(EonEncoder(writer), value)
        return writer.bake()
    }

    fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
        val reader = EonReader.begin(bytes)
        return deserializer.deserialize(EonDecoder(reader))
    }

    fun <T> decodeFromByteArrayNoMetadata(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
        val reader = EonReader.begin(bytes, false)
        return deserializer.deserialize(EonDecoder(reader))
    }

    companion object {
        val instance = Eon()

        inline fun <reified T> encode(value: T, objectId: Int = 0): ByteArray {
            return instance.encodeToByteArray(serializer<T>(), value, objectId)
        }

        inline fun <reified T> decode(bytes: ByteArray): T {
            return instance.decodeFromByteArray(serializer<T>(), bytes)
        }

        inline fun <reified T> decodeNoMetadata(bytes: ByteArray): T {
            return instance.decodeFromByteArrayNoMetadata(serializer<T>(), bytes)
        }

        fun readIntFromStream(stream: InputStream): Int {
            val bytes = stream.readNBytes(Int.SIZE_BYTES)
            return bytes.foldIndexed(0) { index, acc, byte ->
                acc or ((byte.toInt() and 0xFF) shl ((3 - index) * 8))
            }
        }
    }
}