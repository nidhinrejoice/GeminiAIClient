package com.nidhin.geminiclient.feature_learning.di

import com.nidhin.geminiclient.feature_learning.data.GeminiRepository
import com.nidhin.geminiclient.feature_learning.data.data_source.ChatDao
import com.nidhin.geminiclient.feature_learning.data.data_source.ChatDatabase
import com.nidhin.geminiclient.feature_learning.domain.ChatHistoryUsecases
import com.nidhin.geminiclient.feature_learning.domain.ClearChatHistory
import com.nidhin.geminiclient.feature_learning.domain.GenerateGeminiResponse
import com.nidhin.geminiclient.feature_learning.domain.GetChatThreadDetails
import com.nidhin.geminiclient.feature_learning.domain.GetChatThreads
import com.nidhin.geminiclient.feature_learning.domain.IGeminiRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
open class AiModule {

    @Singleton
    @Provides
    fun provideGeminiRepository(geminiRepository: GeminiRepository): IGeminiRepository {
        return geminiRepository
    }

    @Singleton
    @Provides
    fun provideChatDatabase(chatDatabase: ChatDatabase): ChatDao {
        return chatDatabase.chatDao
    }


    @Singleton
    @Provides
    open fun provideGetUseCases(geminiRepository: IGeminiRepository): ChatHistoryUsecases {
        return ChatHistoryUsecases(
            generateGeminiResponse = GenerateGeminiResponse(geminiRepository),
            getChatThreadDetails = GetChatThreadDetails(geminiRepository),
            clearChatHistory = ClearChatHistory(geminiRepository),
            getChatThreads = GetChatThreads(geminiRepository)
        )

    }
}