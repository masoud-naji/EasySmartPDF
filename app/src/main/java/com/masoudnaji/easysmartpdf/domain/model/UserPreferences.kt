package com.masoudnaji.easysmartpdf.domain.model

data class UserPreferences(
    val theme: AppTheme = AppTheme.SYSTEM,
    val language: AppLanguage = AppLanguage.ENGLISH
)
