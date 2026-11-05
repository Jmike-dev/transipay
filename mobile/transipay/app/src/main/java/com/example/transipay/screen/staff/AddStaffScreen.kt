package com.example.transipay.screen.staff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.transipay.service.auth.Role

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStaffScreen(
    onBack: () -> Unit,
    viewModel: AddStaffViewModel = viewModel(),
) {
    val colors = MaterialTheme.colorScheme
    val clipboard = LocalClipboardManager.current

    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var role by rememberSaveable { mutableStateOf(Role.STAFF) }

    // Success dialog: show credentials once, then reset the form
    val createdName = viewModel.createdName
    if (createdName != null) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Staff member created") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("$createdName can now sign in with:")
                    Text("Email: ${email.trim().lowercase()}", fontWeight = FontWeight.SemiBold)
                    Text("Password: $password", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Share this securely and ask them to change it after first login. " +
                                "This is the only time it is shown.",
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearCreated()
                    firstName = ""; lastName = ""; email = ""; phone = ""; password = ""
                    role = Role.STAFF
                    onBack()
                }) { Text("Done") }
            },
            dismissButton = {
                TextButton(onClick = { clipboard.setText(AnnotatedString(password)) }) {
                    Text("Copy password")
                }
            },
        )
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Add staff member") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedTextField(
                value = firstName, onValueChange = { firstName = it },
                label = { Text("First name *") }, singleLine = true,
                enabled = !viewModel.isLoading, modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = lastName, onValueChange = { lastName = it },
                label = { Text("Last name *") }, singleLine = true,
                enabled = !viewModel.isLoading, modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Work email *") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                enabled = !viewModel.isLoading, modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = phone, onValueChange = { phone = it },
                label = { Text("Phone (optional)") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                enabled = !viewModel.isLoading, modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Temporary password *") },
                supportingText = { Text("10 to 72 characters") },
                singleLine = true,
                visualTransformation =
                    if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showPassword) "Hide password" else "Show password",
                        )
                    }
                },
                enabled = !viewModel.isLoading, modifier = Modifier.fillMaxWidth(),
            )
            TextButton(onClick = {
                password = viewModel.generatePassword()
                showPassword = true
            }) { Text("Generate a strong password") }

            Text("Role", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Role.entries.forEach { r ->
                    FilterChip(
                        selected = role == r,
                        onClick = { role = r },
                        label = { Text(r.name) },
                    )
                }
            }

            viewModel.errorMessage?.let {
                Text(it, color = colors.error, fontSize = 13.sp)
            }

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = { viewModel.submit(firstName, lastName, email, phone, role, password) },
                enabled = !viewModel.isLoading,
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = colors.onPrimary,
                    )
                } else {
                    Text("Create staff member", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}