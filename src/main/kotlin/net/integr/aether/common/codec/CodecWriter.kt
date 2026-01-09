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

package net.integr.aether.common.codec

import net.integr.aether.common.packet.security.HashTool
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class CodecWriter private constructor() {
    companion object {
        fun begin(objectId: Int = 0): CodecWriter {
            return CodecWriter().int(0).int(0).int(objectId) // Placeholder for size
        }
    }

    val outStream = ByteArrayOutputStream()
    val data = DataOutputStream(outStream)

    fun int(value: Int): CodecWriter {
        data.writeInt(value)
        return this
    }

    fun float(value: Float): CodecWriter {
        data.writeFloat(value)
        return this
    }

    fun double(value: Double): CodecWriter {
        data.writeDouble(value)
        return this
    }

    fun long(value: Long): CodecWriter {
        data.writeLong(value)
        return this
    }

    fun short(value: Short): CodecWriter {
        data.writeShort(value.toInt())
        return this
    }

    fun byte(value: Byte): CodecWriter {
        data.writeByte(value.toInt())
        return this
    }

    fun boolean(value: Boolean): CodecWriter {
        data.writeBoolean(value)
        return this
    }

    fun char(value: Char): CodecWriter {
        data.writeChar(value.code)
        return this
    }

    fun string(value: String): CodecWriter {
        data.writeUTF(value)
        return this
    }

    fun < E : Enum<E>> enum(value: E): CodecWriter {
        data.writeInt(value.ordinal)
        return this
    }

    fun <T> list(elements: List<T>, codecWriter: CodecWriter.(element: T) -> Unit): CodecWriter {
        data.writeInt(elements.size)

        for (element in elements) {
            codecWriter(this, element!!)
        }

        return this
    }

    fun bake(): ByteArray {
        data.flush()
        val byteArr = outStream.toByteArray()
        val size = byteArr.size

        data.close()
        outStream.close()

        // Write the size at the start
        byteArr[0] = ((size shr 24) and 0xFF).toByte()
        byteArr[1] = ((size shr 16) and 0xFF).toByte()
        byteArr[2] = ((size shr 8) and 0xFF).toByte()
        byteArr[3] = (size and 0xFF).toByte()

        // Write the hash (everything after objectId and without objectId) after the size
        val toHash = byteArr.copyOfRange(12, byteArr.size)
        val hash = HashTool.md5FromObj(toHash)

        byteArr[4] = ((hash shr 24) and 0xFF).toByte()
        byteArr[5] = ((hash shr 16) and 0xFF).toByte()
        byteArr[6] = ((hash shr 8) and 0xFF).toByte()
        byteArr[7] = (hash and 0xFF).toByte()

        return byteArr
    }
}