package ru.nsu.fit.android.drawalk.modules.map

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ru.nsu.fit.android.drawalk.R
import ru.nsu.fit.android.drawalk.databinding.DialogMapSnapshotBinding
import ru.nsu.fit.android.drawalk.model.MapSegment

class MapSnapshotDialog : DialogFragment() {
    companion object {
        private const val URI_INSTANCE_MESSAGE_KEY = "image uri argument for MapSnapshotDialog"
        private const val BITMAP_INSTANCE_MESSAGE_KEY = "image bitmap argument for MapSnapshotDialog"
        private const val POINTS_INSTANCE_MESSAGE_KEY = "image points argument for MapSnapshotDialog"

        fun newInstance(imageUri: Uri, imageBitmap: Bitmap, points: ArrayList<MapSegment>): DialogFragment {
            return MapSnapshotDialog().apply {
                val arguments = Bundle()
                arguments.putParcelable(URI_INSTANCE_MESSAGE_KEY, imageUri)
                arguments.putParcelable(BITMAP_INSTANCE_MESSAGE_KEY, imageBitmap)
                arguments.putParcelableArrayList(POINTS_INSTANCE_MESSAGE_KEY, points)
                setArguments(arguments)
            }
        }
    }

    private lateinit var imageUri: Uri
    private lateinit var imageBitmap: Bitmap
    private lateinit var imagePoints: ArrayList<MapSegment>
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
        imageUri = arguments?.getParcelable(URI_INSTANCE_MESSAGE_KEY)
            ?: throw Exception("no Uri argument received in MapSnapshotDialog")
        imageBitmap = arguments?.getParcelable(BITMAP_INSTANCE_MESSAGE_KEY)
            ?: throw Exception("no Bitmap arguments received in MapSnapshotDialog")
        imagePoints = arguments?.getParcelableArrayList(POINTS_INSTANCE_MESSAGE_KEY)
            ?: throw Exception("no ArrayList argument received in MapSnapshotDialog")
        binding.mapImage.setImageURI(imageUri)
        binding.toolbar.setNavigationOnClickListener { dismiss() }
        binding.toolbar.setTitle(R.string.dialog_toolbar_title)
        //binding.toolbar.inflateMenu(R.menu.menu_map)
//        binding.toolbar.setOnMenuItemClickListener {
//            dismiss()
//            return@setOnMenuItemClickListener true
//        }
        binding.saveButton.setOnClickListener {
            saveArt()
            dismiss()
        }
    }

    //TODO: save at storage
    private fun saveArt(){

    }
}