package com.example.matchmakers.match

class MatchService {

    fun fetchPotentialMatches(): List<MatchDetails> {
        // simulate fetching potential matches from a server or database
        return listOf(
            MatchDetails("User1", 85.0),
            MatchDetails("User2", 90.0)
        )
    }
}
