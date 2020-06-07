package ru.nsu.fit.android.drawalk.modules.example

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.nsu.fit.android.drawalk.R
import ru.nsu.fit.android.drawalk.databinding.FragmentExampleBinding

class ExampleFragment: IExampleFragment(R.layout.fragment_example) {
    private lateinit var binding: FragmentExampleBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentExampleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun showError(cause: Throwable) {

    }

    override fun setExampleText(text: String) {
        binding.mainText.text = text
    }
}