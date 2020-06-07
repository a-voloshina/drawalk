package ru.nsu.fit.android.drawalk.modules.map

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import ru.nsu.fit.android.drawalk.R
import ru.nsu.fit.android.drawalk.databinding.DialogMapSnapshotBinding
import ru.nsu.fit.android.drawalk.model.MapSegment
import ru.nsu.fit.android.drawalk.utils.FirebaseHolder

class MapSnapshotDialog : DialogFragment() {
    companion object {
        private const val BITMAP_INSTANCE_MESSAGE_KEY = "image bitmap argument for MapSnapshotDialog"
        private const val POINTS_INSTANCE_MESSAGE_KEY = "image points argument for MapSnapshotDialog"

        fun newInstance(imageBitmap: Bitmap, points: ArrayList<MapSegment>): DialogFragment {
            return MapSnapshotDialog().apply {
                val arguments = Bundle()
                arguments.putParcelable(BITMAP_INSTANCE_MESSAGE_KEY, imageBitmap)
                arguments.putParcelableArrayList(POINTS_INSTANCE_MESSAGE_KEY, points)
                setArguments(arguments)
            }
        }
    }

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
        imageBitmap = arguments?.getParcelable(BITMAP_INSTANCE_MESSAGE_KEY)
            ?: throw Exception("no Bitmap arguments received in MapSnapshotDialog")
        imagePoints = arguments?.getParcelableArrayList(POINTS_INSTANCE_MESSAGE_KEY)
            ?: throw Exception("no ArrayList argument received in MapSnapshotDialog")
        binding.mapImage.setImageBitmap(imageBitmap)
        binding.toolbar.setNavigationOnClickListener { dismiss() }
        binding.toolbar.setTitle(R.string.dialog_toolbar_title)
        binding.saveButton.setOnClickListener {
            saveArt()
        }
    }

    private fun saveArt(){
        FirebaseHolder.createNewArt(
            imageBitmap,
            binding.newArtName.text.toString(),
            imagePoints,
            {
                Toast.makeText(context, getString(R.string.art_saved), Toast.LENGTH_SHORT).show()
                dismiss()
            },
            {
                Toast.makeText(context, "${getString(R.string.error)}: ${it.message}", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        )
    }
}