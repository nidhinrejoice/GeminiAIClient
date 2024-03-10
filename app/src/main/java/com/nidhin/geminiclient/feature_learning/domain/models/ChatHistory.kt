package com.nidhin.geminiclient.feature_learning.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class ChatHistory(

    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val threadId: String,
    val promptId: String,
    val role: String,
    val message: String,
    val createdAt : Long
)

fun ChatHistory.toDto():ChatHistoryDto{
    return ChatHistoryDto(
        threadId = threadId,
        promptId = promptId,
        role = role,
        aiResponse = message
    )
}
