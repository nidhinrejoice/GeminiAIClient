package com.nidhin.geminiclient.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.nidhin.geminiclient.BuildConfig
import com.nidhin.geminiclient.feature_learning.data.data_source.ChatDatabase
import com.nidhin.geminiclient.persistance.SharedPrefsHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(
            SharedPrefsHelper.PREF_NAME, Context.MODE_PRIVATE
        )
    }

    @Provides
    open fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient().newBuilder()
            .connectTimeout(40, TimeUnit.SECONDS)
            .readTimeout(40, TimeUnit.SECONDS)
        return builder.build()
    }

    @Singleton
    @Provides
    open fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Named("GEMINI_API_KEY")
    fun provideGeminiKey(): String {
        return BuildConfig.geminiKey
    }

    @Provides
    @Singleton
    fun provideChatDatabase(app: Application): ChatDatabase {
        return Room.databaseBuilder(app, ChatDatabase::class.java, ChatDatabase.DATABASE_NAME)
            .build()
    }


}