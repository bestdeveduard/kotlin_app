package dev.lazarev.calltasks.utils

import android.content.Context
import android.security.KeyPairGeneratorSpec
import android.util.Base64

import javax.crypto.Cipher
import javax.security.auth.x500.X500Principal
import java.io.IOException
import java.math.BigInteger
import java.security.*
import java.security.cert.CertificateException
import java.util.Date
import java.util.concurrent.TimeUnit


object CryptUtils {
    const val AUTH_ALIAS = "auth"
    private const val TRANSFORMATION_RSA = "RSA/ECB/PKCS1Padding"
    private const val TRANSFORMATION_AES = "AES/CBC/PKCS5PADDING"
    private val aesKey = byteArrayOf(123, -120, -113, 44, -29, -125, 109, 96, -55, -73, -122, 8, 99, -49, 23, -18)


    fun decrypt(context: Context, alias: String, encrypted: String): String? {
        try {
            val cipher: Cipher = Cipher.getInstance(TRANSFORMATION_RSA)
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            if (!keyStore.containsAlias(alias)) {
                return null
            }
            val entry = keyStore.getEntry(alias, null) as KeyStore.PrivateKeyEntry
            val privateKey = entry.privateKey
            cipher.init(Cipher.DECRYPT_MODE, privateKey)

            val original = cipher.doFinal(Base64.decode(encrypted, Base64.DEFAULT))

            return String(original)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return null
    }


    fun encrypt(context: Context, alias: String, value: String): String? {
        try {
            val cipher: Cipher = Cipher.getInstance(TRANSFORMATION_RSA)
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val publicKey = if (!keyStore.containsAlias(alias)) {
                createNewKeyPair(context, alias, keyStore.provider).public
            } else {
                val entry = keyStore.getEntry(alias, null) as KeyStore.PrivateKeyEntry
                entry.certificate.publicKey
            }
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)

            val encrypted = cipher.doFinal(value.toByteArray())

            return Base64.encodeToString(encrypted, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    @Throws(InvalidAlgorithmParameterException::class, NoSuchAlgorithmException::class)
    private fun createNewKeyPair(context: Context, alias: String, provider: Provider): KeyPair {
        val startTime = Date()
        val spec = KeyPairGeneratorSpec.Builder(context)
            .setAlias(alias)
            .setSubject(X500Principal("CN=CERT, O=LAZAREV"))
            .setSerialNumber(BigInteger.ONE)
            .setStartDate(startTime)
            .setEndDate(Date(startTime.time + TimeUnit.DAYS.toMillis(1000)))
            .build()
        val generator = KeyPairGenerator.getInstance("RSA", provider)
        generator.initialize(spec)

        return generator.generateKeyPair()
    }

    fun removeEntry(alias: String) {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.deleteEntry(alias)
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}
