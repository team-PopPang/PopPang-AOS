package com.poppang.PopPang.push

import com.poppang.PopPang.model.PushMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AlarmRepository {
    private val _notifications = MutableStateFlow<List<PushMessage>>(emptyList())
    val notifications: StateFlow<List<PushMessage>> = _notifications.asStateFlow()

    fun add(message: PushMessage) {
        _notifications.value = listOf(message) + _notifications.value
    }

    fun clear() {
        _notifications.value = emptyList()
    }
}