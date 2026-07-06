package com.masoudnaji.easysmartpdf.domain.repository

import com.masoudnaji.easysmartpdf.domain.model.AppLanguage
import com.masoudnaji.easysmartpdf.domain.model.AppTheme
import com.masoudnaji.easysmartpdf.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val preferences: Flow<UserPreferences>
    suspend fun setTheme(theme: AppTheme)
    suspend fun setLanguage(language: AppLanguage)
}
