package com.example.transipay.screen.fleet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.transipay.service.vehicle.VehicleResponse

@Composable
fun FleetScreen(viewModel: FleetViewModel = viewModel()) {
    val colors = MaterialTheme.colorScheme
    var showAdd by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.load() }

    if (showAdd) {
        AddVehicleDialog(
            isSaving = viewModel.isSaving,
            error = viewModel.saveError,
            onDismiss = { showAdd = false; viewModel.clearSaveError() },
            onSave = { plate -> viewModel.addVehicle(plate) { showAdd = false } },
        )
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("Fleet", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = colors.onBackground)
                Text("${viewModel.vehicles.size} vehicles", fontSize = 12.sp, color = colors.secondary)
            }
            FilledTonalButton(onClick = { showAdd = true }) {
                Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Add")
            }
        }

        when {
            viewModel.isLoading && viewModel.vehicles.isEmpty() ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

            viewModel.errorMessage != null && viewModel.vehicles.isEmpty() ->
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(viewModel.errorMessage!!, color = colors.error, fontSize = 13.sp)
                    TextButton(onClick = viewModel::load) { Text("Retry") }
                }

            viewModel.vehicles.isEmpty() ->
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.Outlined.DirectionsBus, null, tint = colors.primary, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No vehicles yet", fontWeight = FontWeight.Bold, color = colors.onBackground)
                    Text("Tap Add to register your first vehicle.", fontSize = 13.sp, color = colors.secondary)
                }

            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                items(viewModel.vehicles, key = { it.id }) { VehicleRow(it) }
            }
        }
    }
}

@Composable
private fun VehicleRow(v: VehicleResponse) {
    val colors = MaterialTheme.colorScheme
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
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(colors.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.DirectionsBus, null, tint = colors.primary)
        }
        Column(Modifier.weight(1f)) {
            Text(v.numberPlate, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = colors.onSurface)
            Text("Code: ${v.code}", fontSize = 12.sp, color = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun AddVehicleDialog(
    isSaving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var plate by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text("Add vehicle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = plate,
                    onValueChange = { if (it.length <= 20) plate = it },
                    label = { Text("Number plate *") },
                    placeholder = { Text("KDA 123A") },
                    singleLine = true,
                    enabled = !isSaving,
                    isError = error != null,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier.fillMaxWidth(),
                )
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(plate) }, enabled = !isSaving && plate.isNotBlank()) {
                if (isSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancel") }
        },
    )
}