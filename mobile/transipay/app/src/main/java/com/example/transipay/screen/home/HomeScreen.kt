package com.example.transipay.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.transipay.service.auth.Session

@Composable
fun HomeScreen(onAddStaff: () -> Unit, onSignOut: () -> Unit) {
    Scaffold { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Welcome to TransiPay", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
            Text("Signed in as ${Session.role?.name ?: "-"}")

            if (Session.isAdmin) {
                Button(onClick = onAddStaff, modifier = Modifier.fillMaxWidth()) {
                    Text("Add staff member")
                }
            }
            OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                Text("Sign out")
            }
        }
    }
}