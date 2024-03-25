package com.nidhin.geminiclient.feature_learning.data

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.asTextOrNull
import com.nidhin.geminiclient.feature_learning.data.data_source.ChatDao
import com.nidhin.geminiclient.feature_learning.domain.IGeminiRepository
import com.nidhin.geminiclient.feature_learning.domain.models.ChatHistory
import com.nidhin.geminiclient.feature_learning.domain.models.ChatHistoryDto
import com.nidhin.geminiclient.feature_learning.domain.models.toDto
import com.nidhin.geminiclient.persistance.SharedPrefsHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class GeminiRepository @Inject constructor(
    private val sharedPrefsHelper: SharedPrefsHelper,
    private val chatDao: ChatDao,
    @Named("GEMINI_API_KEY") val geminiKey: String
) : IGeminiRepository {
    override suspend fun getGeminiResponse(
        prompt: String,
        chatHistory: List<Content>,
        currentThreadId: String,
        promptId: String
    ): Flow<GenerateContentResponse> {

        val createdAt = Calendar.getInstance().timeInMillis


        val generativeModel = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = geminiKey,
            safetySettings = listOf(
                SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE),
                SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
                SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
                SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE)
            )
        )
        val chat = generativeModel.startChat(
            history = chatHistory
        )
        val flow = (chat.sendMessageStream(prompt))
        var streamIndex = 0
        val stringBuilder = StringBuilder()
        val responseId = UUID.randomUUID().toString()
        return flow.flatMapConcat { response ->
            response.candidates[0].content.let { it ->
                stringBuilder.append(it.parts[0].asTextOrNull() ?: "")
                if(streamIndex==0){
                    chatDao.insertChatHistory(
                        ChatHistory(
                            role = "user",
                            message = prompt,
                            threadId = currentThreadId,
                            createdAt = createdAt,
                            promptId = promptId
                        )
                    )
                }
                chatDao.insertChatHistory(
                    ChatHistory(
                        id = responseId,
                        role = it.role ?: "model",
                        message = stringBuilder.toString(),
                        threadId = currentThreadId,
                        createdAt = Calendar.getInstance().timeInMillis,
                        promptId = promptId
                    )
                )
                streamIndex++
            }
            flowOf(response)
        }
//        return generativeModel.generateContentStream(prompt)
    }

    override suspend fun clearChatHistory(threadId: String): Flow<Boolean> {
        return flow {
            chatDao.clearChatThreadHistory(threadId)
            emit(true)
        }
    }

    override suspend fun getChatHistoryThread(): Flow<List<ChatHistoryDto>> {
        return flowOf(chatDao.getChatHistory().distinctBy { it.threadId }.map { it.toDto() })
    }

    override suspend fun getThreadDetails(threadId: String): Flow<List<ChatHistoryDto>> {
        return chatDao.getThreadHistory(threadId).flatMapConcat { flowOf(it.map { it.toDto() }) }
    }
}