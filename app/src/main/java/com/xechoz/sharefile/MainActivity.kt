package com.xechoz.sharefile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.xechoz.sharefile.ui.App
import com.xechoz.sharefile.ui.theme.ShareFileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShareFileTheme {
                App()
            }
        }
    }
}
