package com.nidhin.geminiclient.feature_learning.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nidhin.geminiclient.feature_learning.domain.models.ChatHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Query("select * from chathistory")
    suspend fun getChatHistory(): List<ChatHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatHistory(chatHistory: ChatHistory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatHistoryList(chatHistory: List<ChatHistory>)

    @Query("delete from chathistory where threadId=:threadId")
    suspend fun clearChatThreadHistory(threadId: String)

    @Query("select * from chathistory where threadId=:threadId")
    fun getThreadHistory(threadId: String) : Flow<List<ChatHistory>>


}