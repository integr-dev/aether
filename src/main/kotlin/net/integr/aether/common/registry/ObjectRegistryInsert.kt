package net.integr.aether.common.registry

import net.integr.aether.common.codec.serialization.Codec
import net.integr.aether.common.packet.Packet
import kotlin.reflect.KType

class ObjectRegistryInsert<T : Any>(val type: KType, val handler: (Packet<T>, Int) -> Unit) {
    fun decode(buffer: ByteArray): Packet<T> {
        val packet = Codec.decodeNoMetadata<Packet<T>>(buffer, type)
        return packet
    }

    fun handle(buffer: ByteArray, id: Int) {
        val packet = decode(buffer)
        handler(packet, id)
    }
}