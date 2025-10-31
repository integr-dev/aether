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
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.EmptySerializersModule
import net.integr.aether.common.eon.EonReader

class EonDecoder(private val codec: EonReader) : Decoder, CompositeDecoder {
    override val serializersModule = EmptySerializersModule()

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder = this
    override fun endStructure(descriptor: SerialDescriptor) {}

    @ExperimentalSerializationApi
    override fun decodeNotNullMark(): Boolean = true

    @ExperimentalSerializationApi
    override fun decodeNull(): Nothing? {
        throw IllegalArgumentException("CodecDecoder does not support decoding null values")
    }

    override fun decodeBoolean(): Boolean = codec.boolean()
    override fun decodeByte(): Byte =codec.byte()
    override fun decodeShort(): Short = codec.short()
    override fun decodeChar(): Char = codec.char()
    override fun decodeInt(): Int = codec.int()
    override fun decodeLong(): Long = codec.long()
    override fun decodeFloat(): Float = codec.float()
    override fun decodeDouble(): Double = codec.double()
    override fun decodeString(): String = codec.string()
    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = codec.int()
    override fun decodeInline(descriptor: SerialDescriptor): Decoder = this

    private var index = 0
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = if (index < descriptor.elementsCount) index++ else CompositeDecoder.DECODE_DONE
    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean = codec.boolean()
    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte = codec.byte()
    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char = codec.char()
    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short = codec.short()
    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int = codec.int()
    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long = codec.long()
    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float = codec.float()
    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double = codec.double()
    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String = codec.string()
    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder = this

    @ExperimentalSerializationApi
    override fun decodeSequentially(): Boolean {
        return true
    }

    override fun <T> decodeSerializableElement(descriptor: SerialDescriptor, index: Int, deserializer: DeserializationStrategy<T>, previousValue: T?): T = deserializer.deserialize(this)

    @ExperimentalSerializationApi
    override fun <T : Any> decodeNullableSerializableElement(descriptor: SerialDescriptor, index: Int, deserializer: DeserializationStrategy<T?>, previousValue: T?): T? = deserializer.deserialize(this)

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
        return codec.int()
    }
}