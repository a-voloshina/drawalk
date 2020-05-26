package ru.nsu.ccfit.molochev.android.rssample.modules.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import ru.nsu.fit.android.drawalk.R

abstract class SingleFragmentActivity : AppCompatActivity() {
    protected lateinit var fragment: Fragment
    protected abstract fun createFragment(): Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment)

        fragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container)
            ?: createFragment()
                .apply {
                    supportFragmentManager.beginTransaction()
                        .add(R.id.fragment_container, this)
                        .commit()
                }
    }
}