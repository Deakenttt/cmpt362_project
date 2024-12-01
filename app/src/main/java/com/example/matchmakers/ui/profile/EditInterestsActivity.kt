package com.example.matchmakers.ui.profile

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.matchmakers.R
import java.io.Serializable

class EditInterestsActivity: AppCompatActivity(), OnItemSelectedListener {
    private val newInterests: ArrayList<String> = arrayListOf()

    private lateinit var interestsList: ListView
    private lateinit var options: Spinner
    private lateinit var saveButton: Button
    private lateinit var discardButton: Button

    private lateinit var interestsAdapter: ArrayAdapter<String>
    private lateinit var optionsAdapter: ArrayAdapter<String>

    // These are the interests that the user can choose from
    // This current list only includes some of the interests that were already in our db but we can add more later
    private val interestChoices = arrayListOf(
        "Select Interest", "Travel", "Football", "Snowboarding", "Swimming", "Drinking", "Poker",
        "Interest 1", "Interest 2", "Interest 3", "Interest 4", "Interest 5", "Interest 6"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_interests)

        val interests = if (Build.VERSION.SDK_INT >= 33){
            intent.getSerializableExtra("interests", Serializable::class.java) as List<String>?
        } else{
            intent.getSerializableExtra("interests") as List<String>?
        }
        if (interests != null){
            newInterests.addAll(interests)
        }

        interestsList = findViewById(R.id.edit_interests_list)
        options = findViewById(R.id.edit_interests_options)
        saveButton = findViewById(R.id.edit_interests_save)
        discardButton = findViewById(R.id.edit_interests_discard)

        interestsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, newInterests)
        interestsList.adapter = interestsAdapter

        optionsAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, interestChoices)
        options.adapter = optionsAdapter


        interestsList.setOnItemClickListener{ _, _, position: Int, _ ->
            newInterests.removeAt(position)
            interestsAdapter.notifyDataSetChanged()
        }
        options.onItemSelectedListener = this


        saveButton.setOnClickListener{
            val result = Intent()
            result.putExtra(INTERESTS_KEY, newInterests.toList() as Serializable)
            setResult(RESULT_OK, result)
            finish()
        }
        discardButton.setOnClickListener{
            finish()
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (position == 0) return
        options.setSelection(0)
        val interest = interestChoices[position]
        // The user can add up to a maximum of 3 interests
        if (!newInterests.contains(interest) && newInterests.size < 3){
            newInterests.add(interest)
            interestsAdapter.notifyDataSetChanged()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        return
    }

    companion object{
        const val INTERESTS_KEY = "interests"
    }
}