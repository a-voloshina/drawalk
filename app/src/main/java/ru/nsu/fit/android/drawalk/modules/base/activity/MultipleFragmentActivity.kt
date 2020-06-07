package ru.nsu.fit.android.drawalk.modules.base.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import ru.nsu.fit.android.drawalk.R

abstract class MultipleFragmentActivity : AppCompatActivity() {
    protected lateinit var currentFragment: Fragment
    protected abstract fun createFragment(): Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment)

        currentFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container)
            ?: createFragment()
                .apply {
                    supportFragmentManager.beginTransaction()
                        .add(R.id.fragment_container, this)
                        .commit()
                }
    }

    fun addFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, fragment)
            .commit()
    }
}