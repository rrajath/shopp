package com.rrajath.milk.domain

import java.security.SecureRandom

interface IdGenerator {
    fun newId(): String
}

object Uuidv7IdGenerator : IdGenerator {
    override fun newId(): String = Uuidv7.generate()
}

/**
 * UUIDv7 generator using the monotonic-counter variant (RFC 9562 section 6.2,
 * "Fixed-Length Dedicated Counter"): IDs generated within the same millisecond
 * increment a 12-bit counter instead of using fresh random bits, so
 * `ORDER BY created_at ASC, id ASC` matches insertion order even when an
 * entire multi-line paste lands in one transaction/millisecond.
 */
object Uuidv7 {
    private const val COUNTER_MAX = 0xFFF // 12 bits
    private val random = SecureRandom()
    private var lastTimestampMs = -1L
    private var counter = 0

    @Synchronized
    fun generate(): String {
        var timestampMs = System.currentTimeMillis()
        if (timestampMs > lastTimestampMs) {
            counter = initialCounter()
        } else {
            timestampMs = lastTimestampMs
            counter++
            if (counter > COUNTER_MAX) {
                timestampMs += 1
                counter = initialCounter()
            }
        }
        lastTimestampMs = timestampMs
        return format(timestampMs, counter)
    }

    // Seed with random bits each new millisecond (unguessability), leaving
    // headroom below COUNTER_MAX so a burst of inserts doesn't immediately
    // roll into the next millisecond.
    private fun initialCounter(): Int = random.nextInt(COUNTER_MAX / 2)

    private fun format(timestampMs: Long, counter: Int): String {
        val bytes = ByteArray(16)
        for (i in 0..5) {
            bytes[i] = (timestampMs shr (40 - 8 * i)).toByte()
        }
        bytes[6] = (0x70 or ((counter shr 8) and 0x0F)).toByte() // version 0111 + counter hi
        bytes[7] = (counter and 0xFF).toByte() // counter lo
        val randB = ByteArray(8)
        random.nextBytes(randB)
        bytes[8] = ((randB[0].toInt() and 0x3F) or 0x80).toByte() // variant 10 + rand_b hi
        for (i in 1..7) {
            bytes[8 + i] = randB[i]
        }
        return toUuidString(bytes)
    }

    private fun toUuidString(bytes: ByteArray): String {
        val hex = StringBuilder(36)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hex.append(HEX_CHARS[v shr 4]).append(HEX_CHARS[v and 0x0F])
            if (i == 3 || i == 5 || i == 7 || i == 9) hex.append('-')
        }
        return hex.toString()
    }

    private val HEX_CHARS = "0123456789abcdef".toCharArray()
}
