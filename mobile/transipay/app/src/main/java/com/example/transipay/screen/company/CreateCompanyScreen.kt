package com.example.transipay.screen.company

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.transipay.ui.theme.Orange500

private data class FleetOption(val range: String, val label: String)

private val fleetOptions = listOf(
    FleetOption("1 – 10", "Starter"),
    FleetOption("11 – 50", "Growing"),
    FleetOption("50+", "Sacco / Scale"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCompanyScreen(
    onCompanyCreated: (companyId: String, companyName: String) -> Unit = { _, _ -> },
    onHaveAccount: () -> Unit = {},
    viewModel: CreateCompanyViewModel = viewModel()
) {
    val focusManager = LocalFocusManager.current
    val colors = MaterialTheme.colorScheme

    // UI-only for now: the ViewModel/Company API doesn't take fleet size yet.
    var fleetIndex by rememberSaveable { mutableIntStateOf(0) }

    // Once the company is created, hand off to the caller (navigates to Sign Up)
    LaunchedEffect(viewModel.createdCompany) {
        viewModel.createdCompany?.let {
            onCompanyCreated(it.id, it.companyName)
            viewModel.clearCreated()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Icon(Icons.Filled.Devices, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(14.dp))
                Text("Company Onboarding", color = colors.onSurfaceVariant, fontSize = 12.sp)
            }

            // Main card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.surface)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BrandHeader()

                Spacer(Modifier.height(16.dp))
                Text(
                    "Get Started with TransiPay",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Effortless transit fleet operations and instant passenger fare collections.",
                    fontSize = 14.sp,
                    color = colors.secondary,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                // Form card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surface)
                        .padding(16.dp)
                ) {
                    Text(
                        text = buildLabel("Company Name", required = true, color = colors.primary),
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )
                    Spacer(Modifier.height(8.dp))

                    TextField(
                        value = viewModel.companyName,
                        onValueChange = viewModel::onCompanyNameChange,
                        placeholder = { Text("e.g., Metro Shuttle Sacco Ltd") },
                        leadingIcon = { Icon(Icons.Filled.Business, null) },
                        singleLine = true,
                        isError = viewModel.errorMessage != null,
                        enabled = !viewModel.isLoading,
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            viewModel.createCompany()
                        }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = colors.surfaceVariant,
                            unfocusedContainerColor = colors.surfaceVariant,
                            errorContainerColor = colors.surfaceVariant,
                            disabledContainerColor = colors.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = colors.error,
                            focusedLeadingIconColor = colors.secondary,
                            unfocusedLeadingIconColor = colors.secondary,
                            unfocusedPlaceholderColor = colors.onSurfaceVariant,
                            focusedPlaceholderColor = colors.onSurfaceVariant,
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))
                    val error = viewModel.errorMessage
                    Text(
                        text = error ?: "Appears on digital bus tickets and customer receipts.",
                        fontSize = 12.sp,
                        color = if (error != null) colors.error else colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(20.dp))

                    // Fleet size header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = buildLabel("Fleet Size", suffix = " (Optional)", color = colors.onSurfaceVariant),
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.DirectionsBus, null, tint = colors.primary, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Buses & Vans", color = colors.primary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        fleetOptions.forEachIndexed { i, option ->
                            FleetChip(
                                option = option,
                                selected = i == fleetIndex,
                                enabled = !viewModel.isLoading,
                                onClick = { fleetIndex = i },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    FeatureStrip()
                }

                Spacer(Modifier.height(28.dp))

                GradientButton(
                    loading = viewModel.isLoading,
                    onClick = viewModel::createCompany
                )

                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onHaveAccount,
                    enabled = !viewModel.isLoading,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, colors.outline),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(
                        "I already have an account · Log in",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }

                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.Shield, null, tint = colors.primary, modifier = Modifier.size(14.dp))
                    Text("Secure sign-in with email or Google", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.secondary)
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    "By creating a company, you accept TransiPay terms of service and operator privacy standards.",
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BrandHeader() {
    val colors = MaterialTheme.colorScheme
    Box(contentAlignment = Alignment.BottomCenter) {
        Box(
            modifier = Modifier
                .padding(bottom = 0.dp)
                .size(84.dp)
                .shadow(8.dp, RoundedCornerShape(22.dp), ambientColor = colors.primary, spotColor = colors.primary)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.DirectionsBus, null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
        // Pill overlapping the logo's bottom edge
        Row(
            modifier = Modifier
                .offset(y = 12.dp)
                .clip(RoundedCornerShape(50))
                .background(colors.primary)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(Modifier.size(5.dp).clip(CircleShape).background(Color.White))
            Text(
                "M-PESA DARAJA\nREADY",
                color = Color.White,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
    }
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun FleetChip(
    option: FleetOption,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) colors.primary else colors.surfaceVariant)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            option.range,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = if (selected) colors.onPrimary else colors.onSurface
        )
        Text(
            option.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) colors.onPrimary.copy(alpha = 0.9f) else colors.secondary
        )
    }
}

@Composable
private fun FeatureStrip() {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceVariant)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FeatureItem(Icons.Filled.Bolt, "STK Push", Modifier.weight(1f))
        VerticalDivider(modifier = Modifier.height(32.dp), color = colors.outline)
        FeatureItem(Icons.Filled.QrCode2, "Dynamic Pass", Modifier.weight(1f))
        VerticalDivider(modifier = Modifier.height(32.dp), color = colors.outline)
        FeatureItem(Icons.Filled.Hub, "Multi-Vehicle", Modifier.weight(1f))
    }
}

@Composable
private fun FeatureItem(icon: ImageVector, label: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = colors.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = colors.secondary)
    }
}

@Composable
private fun GradientButton(loading: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Button(
        onClick = onClick,
        enabled = !loading,
        shape = RoundedCornerShape(50),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(8.dp, RoundedCornerShape(50), ambientColor = colors.primary, spotColor = colors.primary)
            .alpha(if (loading) 0.8f else 1f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(listOf(colors.primary, Orange500))),
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = colors.onPrimary
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Continue to Create Account", color = colors.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = colors.onPrimary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

private fun buildLabel(
    text: String,
    required: Boolean = false,
    suffix: String = "",
    color: Color
) = androidx.compose.ui.text.buildAnnotatedString {
    append(text)
    if (suffix.isNotEmpty()) {
        pushStyle(androidx.compose.ui.text.SpanStyle(color = color, fontWeight = FontWeight.Normal, fontSize = 13.sp))
        append(suffix)
        pop()
    }
    if (required) {
        pushStyle(androidx.compose.ui.text.SpanStyle(color = color))
        append(" *")
        pop()
    }
}