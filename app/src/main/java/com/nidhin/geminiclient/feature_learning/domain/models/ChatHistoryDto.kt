package com.nidhin.geminiclient.feature_learning.domain.models

import androidx.room.Entity

@Entity
data class ChatHistoryDto(


    val threadId: String,
    val promptId: String,
    val role: String,
    var aiResponse: String
)
