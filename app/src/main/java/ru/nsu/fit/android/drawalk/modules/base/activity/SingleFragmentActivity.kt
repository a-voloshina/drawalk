package ru.nsu.fit.android.drawalk.modules.base.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import ru.nsu.fit.android.drawalk.R

abstract class SingleFragmentActivity : AppCompatActivity() {
    protected lateinit var fragment: Fragment
    protected abstract fun createFragment(): Fragment
    private val instanceStateMessage = "Save Fragment in Instance State"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment)

        fragment = if (savedInstanceState != null) {
            supportFragmentManager.getFragment(savedInstanceState, instanceStateMessage)
                ?: throw Exception("No fragment for SingleFragmentActivity after restore")
        } else {
            supportFragmentManager
                .findFragmentById(R.id.fragment_container)
                ?: createFragment()
                    .apply {
                        supportFragmentManager.beginTransaction()
                            .add(R.id.fragment_container, this)
                            .commit()
                    }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        supportFragmentManager.putFragment(outState, instanceStateMessage, fragment)
    }
}