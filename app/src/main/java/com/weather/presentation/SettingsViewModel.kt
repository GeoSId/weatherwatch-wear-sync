package com.weather.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _apiKey = mutableStateOf("")
    val apiKey: State<String> = _apiKey

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    init {
        loadApiKey()
    }

    private fun loadApiKey() {
        viewModelScope.launch {
            _isLoading.value = true
            settingsRepository.getApiKey().onEach { key ->
                _apiKey.value = key
                _isLoading.value = false
            }.launchIn(viewModelScope)
        }
    }

    fun saveApiKey(newApiKey: String) {
        viewModelScope.launch {
            _isLoading.value = true
            settingsRepository.saveApiKey(newApiKey).first()
            _apiKey.value = newApiKey
            _isLoading.value = false
        }
    }
} 