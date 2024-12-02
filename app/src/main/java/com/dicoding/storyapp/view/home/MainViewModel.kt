package com.dicoding.storyapp.view.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.storyapp.model.AuthModel
import com.dicoding.storyapp.model.StoryRepository
import com.dicoding.storyapp.network.response.ListStoryItem
import com.dicoding.storyapp.utils.Event
import kotlinx.coroutines.launch

class MainViewModel (private val repo: StoryRepository) : ViewModel() {
    val isLoading: LiveData<Boolean> = repo.isLoading
    val toastText: LiveData<Event<String>> = repo.toastText
    val getListStories: LiveData<PagingData<ListStoryItem>> =
        repo.getStories().cachedIn(viewModelScope)


    fun getSession(): LiveData<AuthModel> {
        return repo.getSession()
    }

    fun logout() {
        viewModelScope.launch {
            repo.logout()
        }
    }
}