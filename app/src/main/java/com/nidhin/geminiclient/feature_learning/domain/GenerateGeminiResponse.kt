package com.nidhin.geminiclient.feature_learning.domain

import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import com.nidhin.geminiclient.feature_learning.domain.models.ChatHistoryDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class GenerateGeminiResponse @Inject constructor(
    private val geminiRepository: IGeminiRepository
) {

    suspend operator fun invoke(
        prompt: kotlin.String,
        chatHistory: List<ChatHistoryDto>,
        currentThreadId: String, promptId: String
    ): Flow<ChatHistoryDto> {
        val contentList = chatHistory.map {
            content(role = it.role) {
                text(it.aiResponse)
            }
        }
        return geminiRepository.getGeminiResponse(prompt, contentList, currentThreadId, promptId)
            .flatMapConcat { contentRes ->
                var role = ""
                var text = ""
                contentRes.candidates[0].content.parts[0].asTextOrNull()?.let {

                    role = contentRes.candidates[0].content.role.toString()
                    text = it
                }
                flowOf(
                    ChatHistoryDto(
                        threadId = currentThreadId,
                        promptId = promptId,
                        role = role,
                        aiResponse = text
                    )
                )
            }
    }
}