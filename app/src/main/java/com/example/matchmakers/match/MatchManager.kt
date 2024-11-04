package com.example.matchmakers.match

class MatchManager {

    private val matchService = MatchService()
    private val matchAlgorithm = MatchAlgorithm()

    fun getMatches(): List<MatchDetails> {
        val potentialMatches = matchService.fetchPotentialMatches()
        // Additional logic to sort or filter matches can be added here
        return potentialMatches
    }
}
