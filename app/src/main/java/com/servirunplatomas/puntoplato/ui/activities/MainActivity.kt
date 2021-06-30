package com.servirunplatomas.puntoplato.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.servirunplatomas.puntoplato.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setBottomNavigation()
    }

    private fun setBottomNavigation() {
        NavigationUI.setupWithNavController(bottomNavigationMenu, Navigation.findNavController(this, R.id.mainFragment))
    }
}