package com.dicoding.storyapp.view.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.model.AuthModel
import com.dicoding.storyapp.model.StoryRepository
import com.dicoding.storyapp.network.response.LoginResponse
import com.dicoding.storyapp.utils.Event
import kotlinx.coroutines.launch

class LoginViewModel(private val repo: StoryRepository) : ViewModel() {
    val loginResponse: LiveData<LoginResponse> = repo.loginResponse
    val isLoading: LiveData<Boolean> = repo.isLoading
    val toastText: LiveData<Event<String>> = repo.toastText


    fun postLogin(email: String, password: String) {
        viewModelScope.launch {
            repo.postLogin(email, password)
        }
    }

    fun saveSession(session: AuthModel) {
        viewModelScope.launch {
            repo.saveSession(session)
        }
    }

    fun login() {
        viewModelScope.launch {
            repo.login()
        }
    }

}