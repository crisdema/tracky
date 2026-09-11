package com.crisdema.tracky.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("tracky_settings", Context.MODE_PRIVATE)

    private val _currencyCode = MutableStateFlow(prefs.getString(KEY_CURRENCY, "USD") ?: "USD")
    val currencyCode: StateFlow<String> = _currencyCode

    fun setCurrency(code: String) {
        prefs.edit().putString(KEY_CURRENCY, code).apply()
        _currencyCode.value = code
    }

    companion object {
        private const val KEY_CURRENCY = "currency_code"
    }
}