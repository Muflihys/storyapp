package com.dicoding.storyapp.view.auth.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.model.StoryRepository
import com.dicoding.storyapp.network.response.RegisterResponse
import com.dicoding.storyapp.utils.Event
import kotlinx.coroutines.launch

class RegisterViewModel(private val repo: StoryRepository) : ViewModel() {
    val registerResponse: LiveData<RegisterResponse> = repo.registerResponse
    val isLoading: LiveData<Boolean> = repo.isLoading
    val toastText: LiveData<Event<String>> = repo.toastText

    fun postRegister(name: String, email: String, password: String) {
        viewModelScope.launch {
            repo.postRegister(name, email, password)
        }
    }
}