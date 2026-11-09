package com.example.transipay.screen.payments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.transipay.service.payment.PaymentResponse
import com.example.transipay.service.payment.PaymentStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(
    companyId: String,   // keep sourcing it the same way you already do
    viewModel: PaymentViewModel = viewModel(),
) {
    val dialogState by viewModel.state.collectAsState()
    val history by viewModel.history.collectAsState()

    LaunchedEffect(companyId) { viewModel.initHistory(companyId) }

    val listState = rememberLazyListState()
    val nearEnd by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && last >= info.totalItemsCount - 3
        }
    }
    LaunchedEffect(nearEnd, history.items.size) {
        if (nearEnd) viewModel.loadMore()
    }

    Column(Modifier.fillMaxSize()) {

        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Payments",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = viewModel::refresh) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
            }
            Button(
                onClick = { viewModel.openDialog(companyId) },
                modifier = Modifier.padding(end = 8.dp),
            ) { Text("Collect") }
        }

        // Vehicle filter
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FilterChip(
                    selected = history.filterVehicle == null,
                    onClick = { viewModel.onFilterSelected(null) },
                    label = { Text("All vehicles") },
                )
            }
            items(history.vehicles, key = { it.id }) { v ->
                FilterChip(
                    selected = history.filterVehicle?.id == v.id,
                    onClick = { viewModel.onFilterSelected(v) },
                    label = { Text(v.numberPlate) },
                )
            }
        }

        // Content
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                history.loading && history.items.isEmpty() ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))

                history.error != null && history.items.isEmpty() ->
                    Column(
                        Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(history.error!!, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = viewModel::refresh) { Text("Retry") }
                    }

                history.items.isEmpty() ->
                    Text(
                        if (history.filterVehicle == null) "No payments yet."
                        else "No payments for ${history.filterVehicle!!.numberPlate}.",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                else -> LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(history.items, key = { it.id }) { PaymentRow(it) }

                    item {
                        Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                            when {
                                history.loadingMore -> CircularProgressIndicator(Modifier.size(24.dp))
                                history.error != null -> TextButton(onClick = viewModel::loadMore) {
                                    Text("Couldn't load more. Tap to retry")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    PaymentDialog(
        state = dialogState,
        onVehicleSelected = viewModel::onVehicleSelected,
        onPhoneChanged = viewModel::onPhoneChanged,
        onAmountChanged = viewModel::onAmountChanged,
        onSubmit = viewModel::submit,
        onRetry = viewModel::retry,
        onDismiss = viewModel::dismiss,
    )
}

@Composable
private fun PaymentRow(p: PaymentResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${p.numberPlate} (${p.vehicleCode})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "KES ${formatAmount(p.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    p.status,
                    color = statusColor(p.status),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    formatDate(p.transactionDate ?: p.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                p.phoneNumber,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            p.mpesaReceiptNumber?.let {
                Text("Receipt: $it", style = MaterialTheme.typography.bodySmall)
            }

            if (p.status != PaymentStatus.SUCCESS && p.status != PaymentStatus.PENDING) {
                p.resultDesc?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun statusColor(status: String): Color = when (status) {
    PaymentStatus.SUCCESS -> MaterialTheme.colorScheme.primary
    PaymentStatus.PENDING -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.error
}

private fun formatAmount(amount: Double): String =
    if (amount % 1.0 == 0.0) amount.toLong().toString() else "%.2f".format(amount)

private val dateFormat = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm")

private fun formatDate(iso: String?): String {
    if (iso == null) return ""
    return runCatching {
        Instant.parse(iso).atZone(ZoneId.systemDefault()).format(dateFormat)
    }.getOrDefault(iso)
}