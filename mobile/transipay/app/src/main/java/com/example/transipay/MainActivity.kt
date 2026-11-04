package com.example.transipay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.transipay.screen.company.CreateCompanyScreen
import com.example.transipay.ui.theme.TransipayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TransipayTheme {
                CreateCompanyScreen()
            }
        }
    }
}