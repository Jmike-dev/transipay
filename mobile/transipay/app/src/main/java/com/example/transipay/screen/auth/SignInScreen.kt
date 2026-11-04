package com.example.transipay.screen.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.transipay.ui.theme.Orange700
import com.example.transipay.ui.theme.Slate200

@Composable
fun SignInScreen(
    companyName: String = "Metro Shuttle Sacco Ltd",
    defaultRoute: String = "NBO - Thika Su...",
    fleetCount: String = "42 Shuttles",
    onSignedIn: () -> Unit = {},
    onSendMagicLink: (email: String) -> Unit = {}, // no endpoint wired yet
    onContactDispatch: () -> Unit = {},
    viewModel: SignInViewModel = viewModel()
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    var email by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(viewModel.isSignedIn) {
        if (viewModel.isSignedIn) {
            onSignedIn()
            viewModel.clearSignedIn()
        }
    }

    Scaffold(containerColor = colors.background) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Icon(Icons.Filled.Devices, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(14.dp))
                Text("Sign In & Magic Link", color = colors.onSurfaceVariant, fontSize = 12.sp)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.surface)
                    .padding(16.dp)
            ) {
                CompanyHeader(companyName)

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(colors.tertiaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Filled.VpnKey, null, tint = colors.tertiary, modifier = Modifier.size(12.dp))
                    Text("Zero-Password Auth", color = colors.tertiary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(8.dp))
                Text("Sign in to TransiPay", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = colors.onBackground)
//                Spacer(Modifier.height(6.dp))
//                Text(
//                    "Select your preferred authentication method to access your fleet portal",
//                    fontSize = 14.sp,
//                    color = colors.secondary
//                )

                Spacer(Modifier.height(20.dp))
                GoogleButton(
                    loading = viewModel.isLoading,
                    onClick = {
                        viewModel.signInWithGoogle { GoogleSignInHelper.getIdToken(context) }
                    }
                )
                viewModel.errorMessage?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = colors.error, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                }

                Spacer(Modifier.height(16.dp))
//                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
//                    HorizontalDivider(modifier = Modifier.weight(1f), color = colors.outlineVariant)
//                    Text(
//                        "OR SIGN IN WITH EMAIL MAGIC LINK",
//                        fontSize = 10.sp,
//                        letterSpacing = 0.6.sp,
//                        fontWeight = FontWeight.SemiBold,
//                        color = colors.secondary
//                    )
//                    HorizontalDivider(modifier = Modifier.weight(1f), color = colors.outlineVariant)
//                }
//
//                Spacer(Modifier.height(16.dp))
//                MagicLinkCard(
//                    email = email,
//                    onEmailChange = { email = it },
//                    enabled = !viewModel.isLoading,
//                    onSend = { onSendMagicLink(email.trim()) }
//                )
//
//                Spacer(Modifier.height(16.dp))
//                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
//                    InfoTile(Icons.Filled.AltRoute, "Default Route", defaultRoute, Modifier.weight(1f))
//                    InfoTile(Icons.Filled.DirectionsBus, "Registered Fleet", fleetCount, Modifier.weight(1f))
//                }
//
//                Spacer(Modifier.height(16.dp))
//                SecurityCard()
//
//                Spacer(Modifier.height(16.dp))
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.Center,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text("Need help accessing your Sacco portal?", fontSize = 12.sp, color = colors.secondary)
//                    Spacer(Modifier.width(8.dp))
//                    Text(
//                        "Contact Dispatch",
//                        fontSize = 13.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = colors.primary,
//                        modifier = Modifier.clickable(onClick = onContactDispatch)
//                    )
//                }
            }
        }
    }
}

@Composable
private fun CompanyHeader(companyName: String) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(colors.surfaceVariant, colors.primaryContainer)))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Business, null, tint = colors.secondary, modifier = Modifier.size(24.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(companyName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF0D9488)))
                Text("Setup in progress", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = colors.secondary)
            }
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(colors.primaryContainer)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Filled.Verified, null, tint = Orange700, modifier = Modifier.size(14.dp))
            Text("KYC Pending", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Orange700)
        }
    }
}

@Composable
private fun GoogleButton(loading: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    OutlinedButton(
        onClick = onClick,
        enabled = !loading,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, colors.outline),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = colors.onSurface),
        modifier = Modifier.fillMaxWidth().height(52.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = colors.primary)
        } else {
            // Approximation of the Google "G". For production, swap for the official
            // asset from Google's branding guidelines: Image(painterResource(R.drawable.ic_google), ...)
            Text(
                "G",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                style = TextStyle(
                    brush = Brush.linearGradient(
                        0.00f to Color(0xFFEA4335), 0.30f to Color(0xFFEA4335),
                        0.30f to Color(0xFFFBBC05), 0.50f to Color(0xFFFBBC05),
                        0.50f to Color(0xFF34A853), 0.75f to Color(0xFF34A853),
                        0.75f to Color(0xFF4285F4), 1.00f to Color(0xFF4285F4),
                    )
                )
            )
            Spacer(Modifier.width(10.dp))
            Text("Sign in with Google", fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun MagicLinkCard(
    email: String,
    onEmailChange: (String) -> Unit,
    enabled: Boolean,
    onSend: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .padding(16.dp)
    ) {
        Text(
            androidx.compose.ui.text.buildAnnotatedString {
                append("Work Email Address")
                pushStyle(androidx.compose.ui.text.SpanStyle(color = colors.error))
                append(" *")
                pop()
            },
            fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = colors.onSurface
        )
        Spacer(Modifier.height(8.dp))
        TextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = { Text("operations@metroshuttle.co.ke") },
            singleLine = true,
            enabled = enabled,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colors.surfaceVariant,
                unfocusedContainerColor = colors.surfaceVariant,
                disabledContainerColor = colors.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedPlaceholderColor = colors.onSurfaceVariant,
                unfocusedPlaceholderColor = colors.onSurfaceVariant,
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Outlined.Info, null, tint = Color(0xFF0D9488), modifier = Modifier.size(13.dp))
            Text("We'll send a passwordless login link to your inbox", fontSize = 11.sp, color = colors.onSurfaceVariant)
        }
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = onSend,
            enabled = enabled && email.isNotBlank(),
            shape = RoundedCornerShape(50),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Send Magic Link", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun InfoTile(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceVariant)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(Slate200),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = colors.primary, modifier = Modifier.size(18.dp))
        }
        Column {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = colors.secondary)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SecurityCard() {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceVariant)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(colors.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Shield, null, tint = Orange700, modifier = Modifier.size(20.dp))
        }
        Column {
            Text("Transit Grade Security", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.onSurface)
            Spacer(Modifier.height(2.dp))
            Text(
                "Secured with end-to-end tokenized credentials and biometric access. Compliant with National Transport Safety Authority (NTSA) and CBK fintech mandates.",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = colors.secondary
            )
        }
    }
}