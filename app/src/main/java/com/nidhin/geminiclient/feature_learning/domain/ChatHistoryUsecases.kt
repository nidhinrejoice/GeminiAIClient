package com.nidhin.geminiclient.feature_learning.domain

data class ChatHistoryUsecases(
    val clearChatHistory: ClearChatHistory,
    val generateGeminiResponse: GenerateGeminiResponse,
    val getChatThreadDetails: GetChatThreadDetails,
    val getChatThreads: GetChatThreads
)
