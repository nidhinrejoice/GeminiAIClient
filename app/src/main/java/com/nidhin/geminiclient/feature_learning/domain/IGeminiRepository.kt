package com.nidhin.geminiclient.feature_learning.domain

import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.nidhin.geminiclient.feature_learning.domain.models.ChatHistoryDto
import kotlinx.coroutines.flow.Flow

interface IGeminiRepository {

    suspend fun getGeminiResponse(
        prompt: String,
        chatHistory: List<Content>,
        currentThreadId: String,
        promptId: String
    )  : Flow<GenerateContentResponse>
    suspend fun clearChatHistory(threadId: String): Flow<Boolean>
    suspend fun getChatHistoryThread(): Flow<List<ChatHistoryDto>>
    suspend fun getThreadDetails(threadId: String): Flow<List<ChatHistoryDto>>
}