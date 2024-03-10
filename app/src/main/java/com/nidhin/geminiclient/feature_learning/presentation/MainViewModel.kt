package com.nidhin.geminiclient.feature_learning.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.type.HarmProbability
import com.google.ai.client.generativeai.type.PromptBlockedException
import com.nidhin.geminiclient.feature_learning.domain.ChatHistoryUsecases
import com.nidhin.geminiclient.feature_learning.domain.models.ChatHistoryDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.text.StringBuilder

@HiltViewModel
class MainViewModel @Inject constructor(
    private val chatHistoryUsecases: ChatHistoryUsecases
) : ViewModel() {


    private var job: Job? = null
    var isAiResponseLoading = mutableStateOf(false)
        private set
    var enableRetryPrompt = mutableStateOf("")
        private set
    private var _state = mutableStateOf(StockScreenState())
    val state: State<StockScreenState> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        newChat()
    }

    private fun retryPrompt() {
        state.value.promptInProgress?.let { generateAiContent(it) }
    }

    private fun generateAiContent(prompt: String) {
        job?.cancel()
        job = viewModelScope.launch {
            _state.value = state.value.copy(
                promptInProgress = prompt
            )
            isAiResponseLoading.value = true
            enableRetryPrompt.value = ""
            val stringResponse = StringBuilder()
            try {
                if (state.value.currentThreadId == null) {
                    _state.value = _state.value.copy(
                        currentThreadId = UUID.randomUUID().toString()
                    )
                }
                val promptId =
                    UUID.randomUUID().toString()
                val index = state.value.chatHistory.size
                chatHistoryUsecases.generateGeminiResponse.invoke(
                    prompt,
                    state.value.chatHistory,
                    state.value.currentThreadId!!,
                    promptId
                ).collect { contentRes ->
                    if (isAiResponseLoading.value) {
                        _state.value = state.value.copy(
                            promptInProgress = ""
                        )

                        _state.value.chatHistory.add(
                            ChatHistoryDto(
                                role = "user", promptId = promptId,
                                threadId = state.value.currentThreadId!!,
                                aiResponse = prompt
                            )
                        )
                    }
                    if (promptId == contentRes.promptId) {
                        stringResponse.append(contentRes.aiResponse)
                        contentRes.aiResponse = stringResponse.toString()
                        if (isAiResponseLoading.value) {
                            _state.value.chatHistory.add(index + 1, contentRes)
                        } else {
                            _state.value.chatHistory.removeAt(index + 1)
                            val chatH: MutableList<ChatHistoryDto> = mutableListOf()
                            chatH.addAll(_state.value.chatHistory)
                            _state.value.chatHistory.clear()
                            _state.value = _state.value.copy(
                                chatHistory = chatH
                            )
                            _state.value.chatHistory.add(index + 1, contentRes)

                        }
                    }
                    isAiResponseLoading.value = false
                }
            } catch (ex: Exception) {
                if (stringResponse.isEmpty()) {
                    enableRetryPrompt.value = "Something went wrong"
                }
                isAiResponseLoading.value = false
                if (ex is PromptBlockedException) {
                    val stringBuilder = StringBuilder()
                    ex.response.promptFeedback?.safetyRatings?.forEach {
                        if (it.probability != HarmProbability.NEGLIGIBLE) {
                            stringBuilder.append("${it.probability.name} ${it.category.name} Content\n")
                        }
                    }
                    enableRetryPrompt.value = stringBuilder.toString()
//                    _eventFlow.emit(UiEvent.ShowToast(stringBuilder.toString()))
                } else {
                    _eventFlow.emit(UiEvent.ShowToast(ex.message.toString()))
                }
            }
        }
    }

    private fun clearChatHistory() {
        job?.cancel()
        job = viewModelScope.launch {
            try {
                chatHistoryUsecases.clearChatHistory(state.value.currentThreadId ?: "")
                    .collectLatest {
                        _state.value = state.value.copy(
                            chatHistory = mutableListOf()
                        )
                    }
            } catch (ex: Exception) {
                _eventFlow.emit(UiEvent.ShowToast(ex.message.toString()))
            }
        }
    }

    private fun newChat() {
        job?.cancel()
        _state.value = state.value.copy(
            chatHistory = mutableListOf(),
            currentThreadId = UUID.randomUUID().toString(),
            promptInProgress = ""
        )
        enableRetryPrompt.value = ""
        isAiResponseLoading.value = false
        getThreadHistory()
    }


    private fun getThreadHistory() {
        job?.cancel()
        job = viewModelScope.launch {
            try {
                chatHistoryUsecases.getChatThreads().collectLatest { res ->
                    _state.value = state.value.copy(
                        threadHistory = res.toMutableList()
                    )
                }
            } catch (ex: Exception) {
                _eventFlow.emit(UiEvent.ShowToast(ex.message.toString()))
            }
        }
    }

    private fun getChatDetails(threadId: String) {
        job?.cancel()
        job = viewModelScope.launch {
            try {
                chatHistoryUsecases.getChatThreadDetails(threadId).collectLatest { res ->
                    _state.value = state.value.copy(
                        chatHistory = res.toMutableList(),
                        currentThreadId = res[0]?.threadId
                    )
                }
            } catch (ex: Exception) {
                _eventFlow.emit(UiEvent.ShowToast(ex.message.toString()))
            }
        }
    }

    data class StockScreenState(
        val promptInProgress: String? = null,
        val currentThreadId: String? = null,
        val chatHistory: MutableList<ChatHistoryDto> = mutableListOf(),
        val threadHistory: MutableList<ChatHistoryDto> = mutableListOf()
    )


    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
    }

    fun userAction(userAction: UserAction) {
        when (userAction) {
            UserAction.ClearChat -> clearChatHistory()
            is UserAction.GenerateAiContent -> generateAiContent(userAction.prompt)
            UserAction.NewChat -> newChat()
            is UserAction.GetChatDetails -> getChatDetails(userAction.threadId)
            UserAction.RetryPrompt -> retryPrompt()
            UserAction.GetThreadHistory -> getThreadHistory()
        }
    }

    sealed class UserAction {
        object NewChat : UserAction()
        object ClearChat : UserAction()
        object RetryPrompt : UserAction()
        object GetThreadHistory : UserAction()
        class GenerateAiContent(val prompt: String) : UserAction()
        class GetChatDetails(val threadId: String) : UserAction()
    }
}