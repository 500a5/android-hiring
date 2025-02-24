package com.example.myapplication.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.network.SocketManager
import com.example.myapplication.domain.entity.TestRequest
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

private const val SERVER_ADDRESS = "challenge.ciliz.com"
private const val SERVER_PORT = 2222

class MainViewModel : ViewModel() {

    private val socketManager = SocketManager(SERVER_ADDRESS, SERVER_PORT)

    private val _isFormValid = MutableStateFlow(false)
    val isFormValid = _isFormValid.asSharedFlow()

    private val _messageFlow = MutableSharedFlow<String>()
    val messageFlow = _messageFlow.asSharedFlow()

    private val _savedData = MutableStateFlow<Pair<Int?, String?>>(null to null)
    val savedData = _savedData.asSharedFlow()

    private var selectedAge: Int? = null
    private var selectedGender: String? = null

    fun loadSavedData() {
        val age = Hawk.get<Int>("age", null)
        val gender = Hawk.get<String>("gender", null)
        selectedAge = age
        selectedGender = gender
        _savedData.value = age to gender
        updateFormState()
    }

    fun updateAge(age: Int) {
        selectedAge = age
        Hawk.put("age", age)
        updateFormState()
    }

    fun updateGender(gender: String) {
        selectedGender = gender
        Hawk.put("gender", gender)
        updateFormState()
    }

    private fun updateFormState() {
        _isFormValid.value = selectedAge != null && !selectedGender.isNullOrEmpty()
    }

    fun sendData() {
        if (selectedAge == null || selectedGender == null) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                socketManager.connect()
                val request = TestRequest(selectedGender!!, selectedAge!!)
                socketManager.send(request)
                val response = socketManager.receive()
                _messageFlow.emit("Ответ от сервера: ${response.allowed}")
            } catch (e: Exception) {
                _messageFlow.emit("Ошибка отправки: ${e.message}")
            } finally {
                socketManager.close()
            }
        }
    }
}
