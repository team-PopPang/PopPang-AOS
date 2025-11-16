package com.poppang.PopPang.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.model.KeywordDeleteRequest
import com.poppang.PopPang.model.KeywordInsertRequest
import com.poppang.PopPang.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AlarmKeywordViewModel : ViewModel() {
    private val _keywords = MutableStateFlow<List<String>>(emptyList())
    val keywords: StateFlow<List<String>> = _keywords

    fun getKeywords(userUuid: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.keywordApi.getKeyword(userUuid)
                _keywords.value = response.map { it.alertKeyword }
            } catch (e: Exception) {
                Log.e("AlarmKeywordViewModel", "Error fetching keywords", e)
            }
        }
    }

    fun insertKeyword(userUuid: String, newAlertKeyword: String) {
        viewModelScope.launch {
            try {
                val request = KeywordInsertRequest(userUuid, newAlertKeyword)
                RetrofitInstance.keywordApi.insertKeyword(request)
                getKeywords(userUuid)
            } catch (e: Exception) {
                Log.e("AlarmKeywordViewModel", "Error inserting keyword", e)
            }
        }
    }

    fun deleteKeyword(userUuid: String, deleteAlertKeyword: String) {
        viewModelScope.launch {
            try {
                val request = KeywordDeleteRequest(userUuid, deleteAlertKeyword)
                RetrofitInstance.keywordApi.deleteKeyword(request)
                getKeywords(userUuid)
            } catch (e: Exception) {
                Log.e("AlarmKeywordViewModel", "Error deleting keyword", e)
            }
        }
    }
}
