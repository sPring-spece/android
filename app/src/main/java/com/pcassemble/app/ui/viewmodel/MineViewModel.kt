package com.pcassemble.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pcassemble.app.data.ConsultationOut
import com.pcassemble.app.data.FavoriteOut
import com.pcassemble.app.data.Network
import com.pcassemble.app.data.RecommendResponse
import com.pcassemble.app.data.Repository
import com.pcassemble.app.data.UserOut
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MineViewModel(private val repo: Repository) : ViewModel() {

    private val _favorites = MutableStateFlow<List<FavoriteOut>>(emptyList())
    val favorites = _favorites.asStateFlow()

    private val _consults = MutableStateFlow<List<ConsultationOut>>(emptyList())
    val consults = _consults.asStateFlow()

    private val _recommend = MutableStateFlow<RecommendResponse?>(null)
    val recommend = _recommend.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    fun loadFavorites() {
        viewModelScope.launch {
            try {
                _favorites.value = repo.favorites()
            } catch (e: Exception) {
                _error.value = Network.errorMessage(e)
            }
        }
    }

    fun loadConsults() {
        viewModelScope.launch {
            try {
                _consults.value = repo.consultations()
            } catch (e: Exception) {
                _error.value = Network.errorMessage(e)
            }
        }
    }

    fun submitConsult(content: String, contact: String, appointmentDate: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                repo.createConsultation(
                    content,
                    contact.ifBlank { null },
                    appointmentDate.ifBlank { null },
                )
                onSuccess()
                loadConsults()
            } catch (e: Exception) {
                _error.value = Network.errorMessage(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun recommend(budget: String) {
        val b = budget.toDoubleOrNull() ?: run { _error.value = "请输入有效预算"; return }
        viewModelScope.launch {
            _loading.value = true
            try {
                _recommend.value = repo.recommend(b)
            } catch (e: Exception) {
                _error.value = Network.errorMessage(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            repo.logout()
            onDone()
        }
    }
}
