package com.example.matchmakers.ui.profile

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.matchmakers.R

class BlockUserDialog: DialogFragment() {
    var activity: OtherProfileActivity? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bundle = arguments
        val name = bundle?.getString(DIALOG_NAME)

        val builder = AlertDialog.Builder(requireActivity())
        if (name != null){
            builder.setTitle("Block $name")
        }

        val view: View = requireActivity().layoutInflater.inflate(R.layout.dialog_block_user, null)

        builder.setView(view)
        builder.setPositiveButton("Yes", activity)
        builder.setNegativeButton("No", activity)

        return builder.create()
    }

    companion object{
        const val DIALOG_NAME = "name"
    }
}