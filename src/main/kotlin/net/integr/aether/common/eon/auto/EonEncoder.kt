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

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.EmptySerializersModule
import net.integr.aether.common.eon.EonWriter

class EonEncoder(private val codec: EonWriter) : Encoder, CompositeEncoder {
    override val serializersModule = EmptySerializersModule()

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder = this
    override fun endStructure(descriptor: SerialDescriptor) {}

    @ExperimentalSerializationApi
    override fun encodeNull() {
        throw IllegalArgumentException("CodecEncoder does not support encoding null values")
    }

    override fun encodeInt(value: Int) {
        codec.int(value)
    }

    override fun encodeLong(value: Long) {
        codec.long(value)
    }

    override fun encodeFloat(value: Float) {
        codec.float(value)
    }

    override fun encodeString(value: String) {
        codec.string(value)
    }


    override fun encodeBoolean(value: Boolean) {
        codec.boolean(value)
    }

    override fun encodeByte(value: Byte) {
        codec.byte(value)
    }

    override fun encodeShort(value: Short) {
        codec.short(value)
    }

    override fun encodeChar(value: Char) {
        codec.char(value)
    }

    override fun encodeDouble(value: Double) {
        codec.double(value)
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        codec.int(index)
    }

    override fun encodeInline(descriptor: SerialDescriptor): Encoder {
        return this
    }

    @ExperimentalSerializationApi
    override fun <T : Any> encodeNullableSerializableElement(descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T?) {
        if (value == null) {
            encodeNull()
        } else {
            serializer.serialize(this, value)
        }
    }

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
        codec.boolean(value)
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        codec.byte(value)
    }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
        codec.short(value)
    }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
        codec.char(value)
    }

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
        codec.int(value)
    }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
        codec.long(value)
    }

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
        codec.float(value)
    }

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
        codec.double(value)
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        codec.string(value)
    }

    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
        return this
    }

    override fun <T> encodeSerializableElement(descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T) {
        serializer.serialize(this, value)
    }

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        codec.int(collectionSize)
        return this
    }
}