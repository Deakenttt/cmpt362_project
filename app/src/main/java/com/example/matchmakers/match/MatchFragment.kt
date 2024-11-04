package com.example.matchmakers.match

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.matchmakers.R

class MatchFragment : Fragment() {

    private val matchViewModel: MatchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_match, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // observe LiveData and setup UI interactions
        matchViewModel.matchLiveData.observe(viewLifecycleOwner) { matches ->
            // update UI with matches
        }
    }
}
