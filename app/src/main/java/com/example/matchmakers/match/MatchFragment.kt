package com.example.matchmakers.match

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
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
        // 显示返回箭头
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)


        // observe LiveData and setup UI interactions
        matchViewModel.matchLiveData.observe(viewLifecycleOwner) { matches ->
            // update UI with matches
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 隐藏返回箭头，以免返回 Dashboard 时仍显示
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }


}
