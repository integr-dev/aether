package net.integr.aether.common.registry

import net.integr.aether.common.packet.Packet
import net.integr.aether.common.registry.identity.IdGenerator
import kotlin.reflect.typeOf

class ObjectRegistry(val gen: IdGenerator = IdGenerator.newFromZero()) {
    val mapping = mutableMapOf<Int, ObjectRegistryInsert<*>>()

    inline fun <reified T : Any> useHandler(noinline handler: (Packet<T>, Int) -> Unit): Int {
        val id = gen.next()
        mapping[id] = ObjectRegistryInsert(typeOf<Packet<T>>(), handler)
        return id
    }

    fun get(id: Int): ObjectRegistryInsert<*>? {
        return mapping[id]
    }

    fun handle(id: Int, buffer: ByteArray) {
        val insertion = mapping[id] ?: return
        insertion.handle(buffer, id)
    }
}