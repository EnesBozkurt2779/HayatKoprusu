package com.hayatkoprusu.core

import java.nio.ByteBuffer

object MessageChunker {
    private const val MAX_CHUNK_SIZE = 20

    data class MessageChunk(
        val msgId: Short,
        val totalChunks: Byte,
        val chunkIndex: Byte,
        val payload: ByteArray
    ) {
        fun toByteArray(): ByteArray {
            val buffer = ByteBuffer.allocate(4 + payload.size)
            buffer.putShort(msgId)
            buffer.put(totalChunks)
            buffer.put(chunkIndex)
            buffer.put(payload)
            return buffer.array()
        }

        companion object {
            fun fromByteArray(data: ByteArray): MessageChunk {
                val buffer = ByteBuffer.wrap(data)
                val msgId = buffer.short
                val totalChunks = buffer.get()
                val chunkIndex = buffer.get()
                val payload = ByteArray(data.size - 4)
                buffer.get(payload)
                return MessageChunk(msgId, totalChunks, chunkIndex, payload)
            }
        }
    }

    fun chunkify(msgId: Short, data: ByteArray): List<MessageChunk> {
        val chunks = mutableListOf<MessageChunk>()
        val totalChunks = ((data.size + MAX_CHUNK_SIZE - 1) / MAX_CHUNK_SIZE).toByte()
        
        for (i in 0 until totalChunks.toInt()) {
            val start = i * MAX_CHUNK_SIZE
            val end = minOf(start + MAX_CHUNK_SIZE, data.size)
            val payload = data.copyOfRange(start, end)
            chunks.add(MessageChunk(msgId, totalChunks, i.toByte(), payload))
        }
        return chunks
    }

    fun reassemble(chunks: List<MessageChunk>): ByteArray {
        val sortedChunks = chunks.sortedBy { it.chunkIndex }
        val totalSize = sortedChunks.sumOf { it.payload.size }
        val result = ByteBuffer.allocate(totalSize)
        for (chunk in sortedChunks) {
            result.put(chunk.payload)
        }
        return result.array()
    }
}
