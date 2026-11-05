package com.example.transipay.screen.payments

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.runtime.Composable
import com.example.transipay.screen.common.PlaceholderScreen

@Composable
fun PaymentsScreen() {
    PlaceholderScreen(
        title = "Payments",
        subtitle = "M-PESA collections and receipts will appear here.",
        icon = Icons.Outlined.Receipt,
    )
}