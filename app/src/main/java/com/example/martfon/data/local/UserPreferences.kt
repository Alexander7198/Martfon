package com.example.martfon.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("user_preferences")

class UserPreferences(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val PHONE_KEY = stringPreferencesKey("phone_number")
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_LOGGED_IN_KEY] ?: false
        }

    val authToken: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[TOKEN_KEY]
        }

    suspend fun saveAuthData(token: String, phone: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[PHONE_KEY] = phone
            preferences[IS_LOGGED_IN_KEY] = true
        }
    }

    suspend fun clearAuthData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}