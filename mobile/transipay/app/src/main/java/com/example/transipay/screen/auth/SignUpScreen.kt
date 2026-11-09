package com.example.transipay.screen.auth

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SignUpScreen(
    companyId: String,
    companyName: String,
    onSignedUp: () -> Unit,
    onHaveAccount: () -> Unit,
    viewModel: SignUpViewModel = viewModel(),
) {
    val colors = MaterialTheme.colorScheme
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel.signedUp) {
        if (viewModel.signedUp) {
            password = ""; confirm = ""
            onSignedUp()
        }
    }

    val fieldModifier = Modifier.fillMaxWidth()
    val enabled = !viewModel.isLoading

    Scaffold(containerColor = colors.background) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Create your account", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    "You'll be the admin of $companyName.",
                    fontSize = 13.sp, color = colors.secondary,
                )

                OutlinedTextField(firstName, { firstName = it }, label = { Text("First name *") },
                    singleLine = true, enabled = enabled, modifier = fieldModifier)
                OutlinedTextField(lastName, { lastName = it }, label = { Text("Last name *") },
                    singleLine = true, enabled = enabled, modifier = fieldModifier)
                OutlinedTextField(email, { email = it }, label = { Text("Work email *") },
                    singleLine = true, enabled = enabled, modifier = fieldModifier,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                OutlinedTextField(phone, { phone = it }, label = { Text("Phone (optional)") },
                    singleLine = true, enabled = enabled, modifier = fieldModifier,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Password *") },
                    supportingText = { Text("10 to 72 characters") },
                    singleLine = true, enabled = enabled, modifier = fieldModifier,
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
                )
                OutlinedTextField(
                    value = confirm, onValueChange = { confirm = it },
                    label = { Text("Confirm password *") },
                    singleLine = true, enabled = enabled, modifier = fieldModifier,
                    visualTransformation =
                        if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                )

                viewModel.errorMessage?.let {
                    Text(it, color = colors.error, fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        viewModel.submit(companyId, firstName, lastName, email, phone, password, confirm)
                    },
                    enabled = enabled,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = colors.onPrimary,
                        )
                    } else {
                        Text("Create account", fontWeight = FontWeight.Bold)
                    }
                }

                TextButton(onClick = onHaveAccount, modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally)) {
                    Text("Already have an account? Sign in")
                }
            }
        }
    }
}