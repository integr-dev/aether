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

package net.integr.aether.common.codec.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.EmptySerializersModule
import net.integr.aether.common.codec.CodecReader

class CodecDecoder(private val codecReader: CodecReader) : Decoder, CompositeDecoder {
    override val serializersModule = EmptySerializersModule()

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder = this
    override fun endStructure(descriptor: SerialDescriptor) {}

    @ExperimentalSerializationApi
    override fun decodeNotNullMark(): Boolean = true

    @ExperimentalSerializationApi
    override fun decodeNull(): Nothing? {
        throw IllegalArgumentException("CodecDecoder does not support decoding null values")
    }

    override fun decodeBoolean(): Boolean = codecReader.boolean()
    override fun decodeByte(): Byte =codecReader.byte()
    override fun decodeShort(): Short = codecReader.short()
    override fun decodeChar(): Char = codecReader.char()
    override fun decodeInt(): Int = codecReader.int()
    override fun decodeLong(): Long = codecReader.long()
    override fun decodeFloat(): Float = codecReader.float()
    override fun decodeDouble(): Double = codecReader.double()
    override fun decodeString(): String = codecReader.string()
    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = codecReader.int()
    override fun decodeInline(descriptor: SerialDescriptor): Decoder = this

    private var index = 0
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = if (index < descriptor.elementsCount) index++ else CompositeDecoder.DECODE_DONE
    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean = codecReader.boolean()
    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte = codecReader.byte()
    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char = codecReader.char()
    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short = codecReader.short()
    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int = codecReader.int()
    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long = codecReader.long()
    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float = codecReader.float()
    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double = codecReader.double()
    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String = codecReader.string()
    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder = this

    @ExperimentalSerializationApi
    override fun decodeSequentially(): Boolean {
        return true
    }

    override fun <T> decodeSerializableElement(descriptor: SerialDescriptor, index: Int, deserializer: DeserializationStrategy<T>, previousValue: T?): T = deserializer.deserialize(this)

    @ExperimentalSerializationApi
    override fun <T : Any> decodeNullableSerializableElement(descriptor: SerialDescriptor, index: Int, deserializer: DeserializationStrategy<T?>, previousValue: T?): T? = deserializer.deserialize(this)

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
        return codecReader.int()
    }
}