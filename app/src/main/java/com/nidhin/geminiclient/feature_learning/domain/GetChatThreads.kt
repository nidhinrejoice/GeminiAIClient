package com.nidhin.geminiclient.feature_learning.domain

import com.nidhin.geminiclient.feature_learning.domain.models.ChatHistoryDto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChatThreads @Inject constructor(
    private val geminiRepository: IGeminiRepository
) {

    suspend operator fun invoke(): Flow<List<ChatHistoryDto>> {
            return geminiRepository.getChatHistoryThread()
    }
}