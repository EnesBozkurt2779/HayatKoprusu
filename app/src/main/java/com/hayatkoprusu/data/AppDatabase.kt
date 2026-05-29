package com.hayatkoprusu.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val msgId: Long,
    val senderId: String,
    val timestamp: Long,
    val content: String?,
    val originalLength: Int,
    val statusMask: Byte,
    val isComplete: Boolean,
    val expiration: Long
)

@Entity(tableName = "message_chunks", primaryKeys = ["msgId", "chunkIndex"])
data class ChunkEntity(
    val msgId: Long,
    val chunkIndex: Int,
    val totalChunks: Int,
    val payload: ByteArray,
    val receivedAt: Long
)

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertChunk(chunk: ChunkEntity)

    @Query("SELECT * FROM message_chunks WHERE msgId = :msgId")
    suspend fun getChunksForMessage(msgId: Long): List<ChunkEntity>

    @Update
    suspend fun updateMessage(message: MessageEntity)
}

@Database(entities = [MessageEntity::class, ChunkEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hayat_koprusu_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
