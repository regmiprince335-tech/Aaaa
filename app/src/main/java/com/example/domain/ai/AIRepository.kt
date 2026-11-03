package com.example.domain.ai

import com.example.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface AIRepository {
    fun sendMessage(prompt: String, history: List<ChatMessage>): Flow<String>
    fun isConfigured(): Boolean
}
