package com.example.matchmakers.ui.profile

import com.example.matchmakers.R

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class EditAvatarDialog: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bundle = arguments
        val name = bundle?.getString(DIALOG_NAME)

        val builder = AlertDialog.Builder(requireActivity())
        if (name != null){
            builder.setTitle(name)
        }

        val view: View = requireActivity().layoutInflater.inflate(R.layout.dialog_edit_avatar, null)
        val useCamera = view.findViewById<Button>(R.id.edit_camera)
        val useGallery = view.findViewById<Button>(R.id.edit_gallery)

        useCamera.setOnClickListener{
            println("Edit using camera")
        }
        useGallery.setOnClickListener{
            println("Edit using gallery")
        }

        builder.setView(view)

        return builder.create()
    }

    companion object{
        const val DIALOG_NAME = "name"
    }
}