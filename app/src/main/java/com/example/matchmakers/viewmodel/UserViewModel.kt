package com.example.matchmakers.viewmodel

import androidx.lifecycle.*
import com.example.matchmakers.model.User
import com.example.matchmakers.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    private val _recommendedUsers = MutableLiveData<List<User>>()
    val recommendedUsers: LiveData<List<User>> get() = _recommendedUsers

    fun fetchRecommendedUsersForCurrentUser(): Job {
        return viewModelScope.launch {
            val users = repository.getRecommendedUsersForCurrentUser()
            _recommendedUsers.value = users
        }
    }

    // Fetching data locally with filtering
    val usersByInterest: LiveData<List<User>> = repository.getUsersByInterestLocal("swimming").asLiveData()

    // Function to fetch data remotely with filtering
    fun fetchUsersByInterestRemote(interest: String) {
        viewModelScope.launch {
            val users = repository.getUsersByInterestRemote(interest)
            // Handle the data fetched from remote, e.g., update LiveData or process further
        }
    }

    // Function to insert data
    fun insertUser(user: User) {
        viewModelScope.launch {
            repository.insertUserLocal(user)
            repository.insertUserRemote(user)
        }
    }

    // Function to delete data
    fun deleteUser(user: User) {
        viewModelScope.launch {
            repository.deleteUserLocal(user)
            repository.deleteUserRemote(user.id)
        }
    }
}
