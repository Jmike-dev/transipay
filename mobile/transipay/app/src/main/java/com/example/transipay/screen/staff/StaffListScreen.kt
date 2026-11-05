package com.example.transipay.screen.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.transipay.service.auth.Session
import com.example.transipay.service.staff.StaffResponse
import com.example.transipay.ui.theme.Orange700

@Composable
fun StaffListScreen(
    onAddStaff: () -> Unit,
    viewModel: StaffListViewModel = viewModel(),
) {
    val colors = MaterialTheme.colorScheme

    // Loads on first show and again when returning from Add Staff
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.load() }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("Staff", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = colors.onBackground)
                Text("${viewModel.staff.size} members", fontSize = 12.sp, color = colors.secondary)
            }
            if (Session.isAdmin) {
                FilledTonalButton(onClick = onAddStaff) {
                    Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add")
                }
            }
        }

        when {
            viewModel.isLoading && viewModel.staff.isEmpty() ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

            viewModel.errorMessage != null && viewModel.staff.isEmpty() ->
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(viewModel.errorMessage!!, color = colors.error, fontSize = 13.sp)
                    TextButton(onClick = viewModel::load) { Text("Retry") }
                }

            viewModel.staff.isEmpty() ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No staff yet.", color = colors.secondary)
                }

            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                items(viewModel.staff, key = { it.id }) { StaffRow(it) }
            }
        }
    }
}

@Composable
private fun StaffRow(s: StaffResponse) {
    val colors = MaterialTheme.colorScheme
    val initials = "${s.firstName.firstOrNull() ?: ""}${s.lastName.firstOrNull() ?: ""}".uppercase()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(42.dp).clip(CircleShape).background(colors.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(initials, fontWeight = FontWeight.Bold, color = Orange700)
        }
        Column(Modifier.weight(1f)) {
            Text(
                "${s.firstName} ${s.lastName}",
                fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.onSurface,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Text(
                s.email,
                fontSize = 12.sp, color = colors.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                s.role,
                fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Orange700,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(colors.primaryContainer)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
            if (!s.active) {
                Spacer(Modifier.height(4.dp))
                Text("Inactive", fontSize = 11.sp, color = colors.error)
            }
        }
    }
}