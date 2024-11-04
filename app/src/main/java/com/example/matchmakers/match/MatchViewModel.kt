package com.example.matchmakers.match

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MatchViewModel : ViewModel() {

    private val _matchLiveData = MutableLiveData<List<MatchDetails>>()
    val matchLiveData: LiveData<List<MatchDetails>> get() = _matchLiveData

    fun loadMatches() {
        // load matches using MatchManager or MatchService
        _matchLiveData.value = MatchManager().getMatches()
    }
}
