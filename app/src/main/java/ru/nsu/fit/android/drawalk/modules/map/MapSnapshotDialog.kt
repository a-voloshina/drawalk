package ru.nsu.fit.android.drawalk.modules.map

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ru.nsu.fit.android.drawalk.R
import ru.nsu.fit.android.drawalk.databinding.DialogMapSnapshotBinding

class MapSnapshotDialog : DialogFragment() {
    companion object {
        private const val INSTANCE_MESSAGE_KEY = "arguments for MapSnapshotDialog"

        fun newInstance(imageUri: Uri): DialogFragment {
            return MapSnapshotDialog().apply {
                val arguments = Bundle()
                arguments.putParcelable(INSTANCE_MESSAGE_KEY, imageUri)
                setArguments(arguments)
            }
        }
    }

    private lateinit var imageUri: Uri
    private lateinit var binding: DialogMapSnapshotBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.AppTheme_FullScreenDialog
        )
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)
            dialog.window?.setWindowAnimations(R.style.AppTheme_Slide)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogMapSnapshotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //binding.description.text = imageUri.toString()
        imageUri = arguments?.getParcelable(INSTANCE_MESSAGE_KEY)
            ?: throw Exception("no arguments received in MapSnapshotDialog")
        binding.mapImage.setImageURI(imageUri)
        binding.toolbar.setNavigationOnClickListener { dismiss() }
        binding.toolbar.setTitle(R.string.dialog_toolbar_title)
        binding.toolbar.inflateMenu(R.menu.menu_map)
        binding.toolbar.setOnMenuItemClickListener {
            dismiss()                           //TODO: save at storage
            return@setOnMenuItemClickListener true
        }
    }
}