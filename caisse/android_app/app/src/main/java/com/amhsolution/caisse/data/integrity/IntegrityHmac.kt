package com.amhsolution.caisse.data.integrity

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey

/**
 * HMAC-SHA256 via Android Keystore (clé non exportable).
 * En environnement JVM/test sans Keystore, [compute] retombe sur SHA-256 du payload.
 */
object IntegrityHmac {
    private const val KEY_ALIAS = "kebaba_ticket_integrity_hmac"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    fun compute(payloadHash: String): String {
        return runCatching {
            val key = getOrCreateKey()
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(key)
            mac.doFinal(payloadHash.toByteArray(Charsets.UTF_8))
                .joinToString("") { byte -> "%02x".format(byte) }
        }.getOrElse {
            // JVM unit tests / appareils sans Keystore : empreinte SHA déjà fournie
            payloadHash
        }
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_HMAC_SHA256,
            ANDROID_KEYSTORE
        )
        keyGenerator.init(
            KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_SIGN)
                .setDigests(KeyProperties.DIGEST_SHA256)
                .build()
        )
        return keyGenerator.generateKey()
    }
}
