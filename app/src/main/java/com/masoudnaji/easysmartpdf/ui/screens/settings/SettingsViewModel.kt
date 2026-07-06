package com.masoudnaji.easysmartpdf.ui.screens.settings

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.masoudnaji.easysmartpdf.data.repository.SettingsRepositoryImpl
import com.masoudnaji.easysmartpdf.domain.model.AppLanguage
import com.masoudnaji.easysmartpdf.domain.model.AppTheme
import com.masoudnaji.easysmartpdf.domain.model.UserPreferences
import com.masoudnaji.easysmartpdf.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SettingsRepository = SettingsRepositoryImpl(application)

    val preferences: StateFlow<UserPreferences> = repository.preferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UserPreferences()
        )

    private val _showLanguageDialog = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        preferences,
        _showLanguageDialog
    ) { prefs, showDialog ->
        SettingsUiState(
            theme = prefs.theme,
            language = prefs.language,
            showLanguageDialog = showDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch { repository.setTheme(theme) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            repository.setLanguage(language)
            _showLanguageDialog.value = false
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(language.localeTag)
            )
        }
    }

    fun openLanguageDialog() { _showLanguageDialog.value = true }
    fun dismissLanguageDialog() { _showLanguageDialog.value = false }
}
