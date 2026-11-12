package com.example.transipay.screen.terminal

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Contactless
import androidx.compose.runtime.Composable
import com.example.transipay.screen.common.PlaceholderScreen

@Composable
fun TerminalScreen() {
    PlaceholderScreen(
        title = "Terminal",
        subtitle = "Tap-to-pay fare collection will appear here.",
        icon = Icons.Outlined.Contactless,
    )
}