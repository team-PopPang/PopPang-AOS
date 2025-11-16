package com.poppang.PopPang.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.searchQueryDataStore by preferencesDataStore("search_queries")

class SearchQueryDataStore(private val context: Context) {
    private val SEARCH_QUERIES_KEY = stringSetPreferencesKey("search_queries")

    val queriesFlow: Flow<Set<String>> = context.searchQueryDataStore.data
        .map { it[SEARCH_QUERIES_KEY] ?: emptySet() }

    suspend fun addQuery(query: String) {
        context.searchQueryDataStore.edit { prefs ->
            val current = prefs[SEARCH_QUERIES_KEY] ?: emptySet()
            prefs[SEARCH_QUERIES_KEY] = setOf(query) + current.take(4)
        }
    }

    suspend fun getQueries(): List<String> {
        val prefs = context.searchQueryDataStore.data.first()
        return prefs[SEARCH_QUERIES_KEY]?.toList() ?: emptyList()
    }

    suspend fun removeQuery(query: String) {
        context.searchQueryDataStore.edit { prefs ->
            val current = prefs[SEARCH_QUERIES_KEY] ?: emptySet()
            prefs[SEARCH_QUERIES_KEY] = current.filter { it != query }.toSet()
        }
    }
}