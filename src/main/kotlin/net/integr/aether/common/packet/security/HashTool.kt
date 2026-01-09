package net.integr.aether.common.packet.security

import java.security.MessageDigest

object HashTool {
    val digest = MessageDigest.getInstance("MD5")

    fun md5FromObj(obj: ByteArray): Int {
        return digest.digest(obj).fold(0) { acc, byte -> acc + byte.toInt() }
    }
}