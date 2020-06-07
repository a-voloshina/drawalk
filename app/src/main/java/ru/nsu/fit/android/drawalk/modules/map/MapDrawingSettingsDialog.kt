package ru.nsu.fit.android.drawalk.modules.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import ru.nsu.fit.android.drawalk.R
import ru.nsu.fit.android.drawalk.databinding.DialogDrawingSettingsBinding

class MapDrawingSettingsDialog : DialogFragment() {
    companion object {
        private const val SET_ARGS_COLOR_MESSAGE_KEY = "color argument for MapSnapshotDialog"
        private const val SET_ARGS_PROGRESS_MESSAGE_KEY = "progress argument for MapSnapshotDialog"

        fun newInstance(
            color: Int,
            progress: Int
        ): DialogFragment {
            return MapDrawingSettingsDialog().apply {
                val arguments = Bundle()
                arguments.putInt(SET_ARGS_PROGRESS_MESSAGE_KEY, progress)
                arguments.putInt(SET_ARGS_COLOR_MESSAGE_KEY, color)
                setArguments(arguments)
            }
        }
    }

    private lateinit var binding: DialogDrawingSettingsBinding
    private lateinit var thumbView: View
    private var currentBackgroundColor: Int = 0
    private var currentProgress: Int = 0
    private lateinit var mapDrawingSettingsListener: MapDrawingSettingsListener

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
        thumbView = inflater.inflate(R.layout.layout_seekbar_thumb, null, false)
        binding = DialogDrawingSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentBackgroundColor = arguments?.getInt(SET_ARGS_COLOR_MESSAGE_KEY)
            ?: throw Exception("no color arguments received in MapSnapshotDialog")
        currentProgress = arguments?.getInt(SET_ARGS_PROGRESS_MESSAGE_KEY)
            ?: throw Exception("no progress arguments received in MapSnapshotDialog")
        binding.toolbar.setNavigationOnClickListener { dismiss() }
        binding.toolbar.setTitle(R.string.dialog_toolbar_title)
        binding.toolbar.inflateMenu(R.menu.menu_map)
        binding.toolbar.setOnMenuItemClickListener {
            mapDrawingSettingsListener.onMapDrawingSettingsChanged(
                currentBackgroundColor, currentProgress
            )
            dismiss()
            return@setOnMenuItemClickListener true
        }
        binding.buttonChooseColor.setBackgroundColor(currentBackgroundColor)
        binding.buttonChooseColor.setOnClickListener { button ->
            ColorPickerDialogBuilder
                .with(context)
                .setTitle(context?.getString(R.string.choose_color))
                .initialColor(currentBackgroundColor)
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                .density(12)
                .setPositiveButton(
                    R.string.save
                ) { _, lastSelectedColor, _ ->
                    button.setBackgroundColor(lastSelectedColor)
                    currentBackgroundColor = lastSelectedColor
                }
                .build()
                .show()
        }
        binding.lineWidthSeekbar.progress = currentProgress
        binding.lineWidthSeekbar.apply {
            thumb = getThumb(currentProgress)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    currentProgress = progress
                    seekBar.thumb = getThumb(progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}

            })
        }
    }

    private fun getThumb(progress: Int): Drawable? {
        (thumbView.findViewById(R.id.progress_indicator) as TextView).text =
            progress.toString()
        thumbView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val bitmap = Bitmap.createBitmap(
            thumbView.measuredWidth,
            thumbView.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        thumbView.layout(0, 0, thumbView.measuredWidth, thumbView.measuredHeight)
        thumbView.draw(canvas)
        return BitmapDrawable(resources, bitmap)
    }

    fun addListener(settingsListener: MapDrawingSettingsListener) = this.apply{
        mapDrawingSettingsListener = settingsListener
    }
}