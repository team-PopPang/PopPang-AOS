package com.poppang.PopPang.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.model.PushMessage
import com.poppang.PopPang.push.AlarmRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AlarmViewModel : ViewModel() {
    val notifications: StateFlow<List<PushMessage>> =
        AlarmRepository.notifications.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )
}
