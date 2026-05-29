package com.hayatkoprusu.core

object SqueezeCodec {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789ÇĞİÖŞÜ .?!-()/:,"
    private val CHAR_TO_BIT = ALPHABET.withIndex().associate { it.value to it.index }
    private val BIT_TO_CHAR = ALPHABET.withIndex().associate { it.index to it.value }

    fun encode(input: String): ByteArray {
        val sanitized = input.uppercase().filter { it in ALPHABET }
        val bitCount = sanitized.length * 6
        val byteCount = (bitCount + 7) / 8
        val result = ByteArray(byteCount)

        var currentBit = 0
        for (char in sanitized) {
            val value = CHAR_TO_BIT[char] ?: 0
            for (i in 5 downTo 0) {
                if ((value shr i) and 1 == 1) {
                    val byteIdx = currentBit / 8
                    val bitIdx = 7 - (currentBit % 8)
                    result[byteIdx] = (result[byteIdx].toInt() or (1 shl bitIdx)).toByte()
                }
                currentBit++
            }
        }
        return result
    }

    fun decode(data: ByteArray, originalLength: Int): String {
        val sb = StringBuilder()
        var currentBit = 0
        
        repeat(originalLength) {
            var value = 0
            for (i in 5 downTo 0) {
                val byteIdx = currentBit / 8
                val bitIdx = 7 - (currentBit % 8)
                if (byteIdx < data.size) {
                    val bit = (data[byteIdx].toInt() shr bitIdx) and 1
                    if (bit == 1) {
                        value = value or (1 shl i)
                    }
                }
                currentBit++
            }
            sb.append(BIT_TO_CHAR[value] ?: '?')
        }
        return sb.toString()
    }
}
