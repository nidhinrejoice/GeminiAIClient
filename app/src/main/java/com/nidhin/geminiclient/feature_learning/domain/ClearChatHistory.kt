package com.nidhin.geminiclient.feature_learning.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ClearChatHistory @Inject constructor(
    private val geminiRepository: IGeminiRepository
) {

    suspend operator fun invoke(threadId: String): Flow<Boolean> {
        if (threadId.isEmpty())
            throw Exception("Thread Id is empty")
        else
            return geminiRepository.clearChatHistory(threadId)
    }
}