package com.example.transipay.screen.payments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun PaymentDialog(
    state: PaymentUiState,
    onVehicleSelected: (com.example.transipay.service.vehicle.VehicleResponse) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onAmountChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (!state.showDialog) return

    val canDismiss = state.phase != PaymentPhase.WAITING

    AlertDialog(
        onDismissRequest = { if (canDismiss) onDismiss() },
        title = {
            Text(
                when (state.phase) {
                    PaymentPhase.FORM -> "Pay with M-PESA"
                    PaymentPhase.WAITING -> "Check your phone"
                    PaymentPhase.SUCCESS -> "Payment received"
                    PaymentPhase.FAILED -> "Payment not completed"
                }
            )
        },
        text = {
            when (state.phase) {
                PaymentPhase.FORM -> FormContent(state, onVehicleSelected, onPhoneChanged, onAmountChanged)
                PaymentPhase.WAITING -> WaitingContent(state)
                PaymentPhase.SUCCESS -> SuccessContent(state)
                PaymentPhase.FAILED -> Text(state.resultMessage ?: "Something went wrong.")
            }
        },
        confirmButton = {
            when (state.phase) {
                PaymentPhase.FORM -> TextButton(onClick = onSubmit, enabled = !state.submitting) {
                    Text(if (state.submitting) "Sending…" else "Send STK push")
                }
                PaymentPhase.WAITING -> {}
                PaymentPhase.SUCCESS -> TextButton(onClick = onDismiss) { Text("Done") }
                PaymentPhase.FAILED -> TextButton(onClick = onRetry) { Text("Try again") }
            }
        },
        dismissButton = {
            if (state.phase == PaymentPhase.FORM || state.phase == PaymentPhase.FAILED) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )
}

@Composable
private fun FormContent(
    state: PaymentUiState,
    onVehicleSelected: (com.example.transipay.service.vehicle.VehicleResponse) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onAmountChanged: (String) -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.loadingVehicles) {
            CircularProgressIndicator(Modifier.size(24.dp))
        } else {
            Column {
                OutlinedButton(onClick = { menuOpen = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        state.selectedVehicle?.let { "${it.numberPlate} (${it.code})" }
                            ?: "Select vehicle"
                    )
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    state.vehicles.forEach { v ->
                        DropdownMenuItem(
                            text = { Text("${v.numberPlate} (${v.code})") },
                            onClick = {
                                onVehicleSelected(v)
                                menuOpen = false
                            },
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = state.phoneNumber,
            onValueChange = onPhoneChanged,
            label = { Text("M-PESA phone number") },
            placeholder = { Text("0712345678") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = state.amount,
            onValueChange = onAmountChanged,
            label = { Text("Amount (KES)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )

        state.formError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun WaitingContent(state: PaymentUiState) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        CircularProgressIndicator(Modifier.size(32.dp))
        Text(
            "An M-PESA prompt was sent to ${state.payment?.phoneNumber ?: state.phoneNumber}. " +
                    "Enter your PIN to complete the payment of KES ${state.payment?.amount ?: state.amount}."
        )
    }
}

@Composable
private fun SuccessContent(state: PaymentUiState) {
    val p = state.payment
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
        Text("KES ${p?.amount} paid for ${p?.numberPlate}")
        p?.mpesaReceiptNumber?.let { Text("Receipt: $it", style = MaterialTheme.typography.bodyMedium) }
    }
}