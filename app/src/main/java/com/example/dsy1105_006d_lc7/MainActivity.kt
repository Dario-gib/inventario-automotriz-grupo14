package com.example.dsy1105_006d_lc7

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.dsy1105_006d_lc7.ui.navigation.AppNavigation
import com.example.dsy1105_006d_lc7.ui.theme.DSY1105006DLC7Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DSY1105006DLC7Theme {
                AppNavigation()
            }
        }
    }
}
