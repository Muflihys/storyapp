package com.dicoding.storyapp.view.story.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.model.AuthModel
import com.dicoding.storyapp.model.StoryRepository
import com.dicoding.storyapp.network.response.AddStoryResponse
import com.dicoding.storyapp.utils.Event
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AddStoryViewModel(private val repo: StoryRepository) : ViewModel() {
    val isLoading: LiveData<Boolean> = repo.isLoading
    val toastText: LiveData<Event<String>> = repo.toastText
    val uploadResponse: LiveData<AddStoryResponse> = repo.uploadResponse

    fun uploadStory(token: String, file: MultipartBody.Part, description: RequestBody) {
        viewModelScope.launch {
            repo.uploadStory(token, file, description)
        }
    }

    fun getSession(): LiveData<AuthModel> {
        return repo.getSession()
    }
}