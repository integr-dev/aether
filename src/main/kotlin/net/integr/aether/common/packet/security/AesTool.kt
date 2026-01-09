package net.integr.aether.common.packet.security

import javax.crypto.*

object AesTool {
    private val keyGenerator: KeyGenerator = KeyGenerator.getInstance("AES")

    init {
        keyGenerator.init(256)
    }

    fun generate(): SecretKey {
        return keyGenerator.generateKey()
    }

    fun generateHandler(): AesHandler {
        return AesHandler(keyGenerator.generateKey())
    }

    class AesHandler(key: SecretKey) {
        private val encryptor: Cipher = Cipher.getInstance("AES/GCM/NoPadding")
        private val decryptor: Cipher = Cipher.getInstance("AES/GCM/NoPadding")

        init {
            encryptor.init(Cipher.ENCRYPT_MODE, key)
            decryptor.init(Cipher.DECRYPT_MODE, key)
        }

        fun encrypt(byteArray: ByteArray): ByteArray {
            return encryptor.doFinal(byteArray)
        }

        fun decrypt(byteArray: ByteArray): ByteArray {
            return decryptor.doFinal(byteArray)
        }
    }
}