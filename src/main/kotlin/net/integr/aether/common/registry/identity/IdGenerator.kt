package net.integr.aether.common.registry.identity

class IdGenerator private constructor(startId: Int = 0) {
    private var currentId: Int = startId - 1

    @Synchronized
    fun next(): Int {
        currentId += 1
        return currentId
    }

    companion object {
        fun newFromZero(): IdGenerator {
            return IdGenerator(0)
        }

        fun newFrom(startId: Int): IdGenerator {
            return IdGenerator(startId)
        }
    }
}