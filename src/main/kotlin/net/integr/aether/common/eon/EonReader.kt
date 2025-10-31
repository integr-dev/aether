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

package net.integr.aether.common.eon

import java.io.ByteArrayInputStream
import java.io.DataInputStream

class EonReader private constructor(byteArray: ByteArray) {
    companion object {
        fun begin(byteArray: ByteArray, dataContainsMeta: Boolean = true): EonReader {
            val codec = EonReader(byteArray)
            if (dataContainsMeta) {
                codec.int()
                codec.int()
            } // Read and discard size placeholder
            return codec
        }
    }

    val inStream = ByteArrayInputStream(byteArray)
    val data = DataInputStream(inStream)

    fun int(): Int {
        return data.readInt()
    }

    fun float(): Float {
        return data.readFloat()
    }

    fun double(): Double {
        return data.readDouble()
    }

    fun long(): Long {
        return data.readLong()
    }

    fun short(): Short {
        return data.readShort()
    }

    fun byte(): Byte {
        return data.readByte()
    }

    fun boolean(): Boolean {
        return data.readBoolean()
    }

    fun char(): Char {
        return data.readChar()
    }

    fun string(): String {
        return data.readUTF()
    }

    inline fun < reified E : Enum<E>> enum(): E {
        val ordinal = data.readInt()
        val enumConstants = enumValues<E>()
        return enumConstants[ordinal]
    }

    fun <T> list(codecReader: EonReader.() -> T): List<T> {
        val size = data.readInt()
        val listItems = mutableListOf<T>()

        for (i in 0 until size) {
            listItems += codecReader()
        }

        return listItems
    }
}