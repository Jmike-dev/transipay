package com.example.transipay.screen.fleet

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.transipay.screen.common.PlaceholderScreen

@Composable
fun FleetScreen(onSignOut: () -> Unit) {
    PlaceholderScreen(
        title = "Fleet",
        subtitle = "Your buses and vans will appear here.",
        icon = Icons.Outlined.DirectionsBus,
    ) {
        Spacer(Modifier.height(20.dp))
        OutlinedButton(onClick = onSignOut) { Text("Sign out") }
    }
}