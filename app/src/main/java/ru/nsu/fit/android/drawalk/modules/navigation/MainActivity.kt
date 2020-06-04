package ru.nsu.fit.android.drawalk.modules.navigation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import ru.nsu.fit.android.drawalk.R
import ru.nsu.fit.android.drawalk.databinding.ActivityMainBinding
import ru.nsu.fit.android.drawalk.modules.example.ExampleActivity
import ru.nsu.fit.android.drawalk.utils.FirebaseHolder

class MainActivity : AppCompatActivity() {
    companion object {
        private const val RC_SIGN_IN = 322
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_arts, R.id.nav_users
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Will open art creation", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            startActivity(Intent(this, ExampleActivity::class.java))
        }

        updateUIForUser()
    }

    private fun updateUIForUser() {
        val currentUser = FirebaseHolder.AUTH.currentUser
        val navView = binding.navView
        navView.removeHeaderView(navView.getHeaderView(0))
        if (currentUser == null) {
            val header = navView.inflateHeaderView(R.layout.nav_header_main_unauthorized)
            header.findViewById<Button>(R.id.sign_in).setOnClickListener {
                val providers = listOf(AuthUI.IdpConfig.GoogleBuilder().build())
                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(), RC_SIGN_IN
                )
            }
        } else {
            FirebaseHolder.tryToAddNewUserData(
                currentUser.uid,
                currentUser.displayName ?: "USER ${currentUser.uid}",
                currentUser.photoUrl?.toString()
            )
            val header = navView.inflateHeaderView(R.layout.nav_header_main_authorized)
            header.findViewById<Button>(R.id.sign_out).setOnClickListener {
                AuthUI.getInstance()
                    .signOut(this)
                    .addOnSuccessListener {
                        updateUIForUser()
                    }
            }
            header.findViewById<TextView>(R.id.user_name).text = currentUser.displayName
            header.findViewById<TextView>(R.id.user_email).text = currentUser.email
            val avatar = header.findViewById<ImageView>(R.id.imageView)
            Picasso.get().load(currentUser.photoUrl).into(avatar)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            updateUIForUser()
            if (resultCode != Activity.RESULT_OK) {
                val response = IdpResponse.fromResultIntent(data)
                Toast.makeText(this, response?.error?.message ?: "NULL", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}