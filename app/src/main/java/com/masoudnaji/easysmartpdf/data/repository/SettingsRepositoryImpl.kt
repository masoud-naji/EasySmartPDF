package com.masoudnaji.easysmartpdf.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.masoudnaji.easysmartpdf.domain.model.AppLanguage
import com.masoudnaji.easysmartpdf.domain.model.AppTheme
import com.masoudnaji.easysmartpdf.domain.model.UserPreferences
import com.masoudnaji.easysmartpdf.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepositoryImpl(context: Context) : SettingsRepository {

    private val store = context.applicationContext.dataStore

    private object Keys {
        val theme = stringPreferencesKey("theme")
        val language = stringPreferencesKey("language")
    }

    override val preferences: Flow<UserPreferences> = store.data.map { prefs ->
        UserPreferences(
            theme = prefs[Keys.theme]?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() }
                ?: AppTheme.SYSTEM,
            language = prefs[Keys.language]?.let { runCatching { AppLanguage.valueOf(it) }.getOrNull() }
                ?: AppLanguage.ENGLISH
        )
    }

    override suspend fun setTheme(theme: AppTheme) {
        store.edit { it[Keys.theme] = theme.name }
    }

    override suspend fun setLanguage(language: AppLanguage) {
        store.edit { it[Keys.language] = language.name }
    }
}
