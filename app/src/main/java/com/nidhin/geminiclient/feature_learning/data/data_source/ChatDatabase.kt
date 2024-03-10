package com.nidhin.geminiclient.feature_learning.data.data_source

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nidhin.geminiclient.feature_learning.domain.models.ChatHistory

@Database(
    entities = [ChatHistory::class],
    version = 1
)
abstract class ChatDatabase :RoomDatabase(){
    abstract val chatDao : ChatDao

    companion object {
        const val DATABASE_NAME: String = "gemini_chat_db"
    }
}