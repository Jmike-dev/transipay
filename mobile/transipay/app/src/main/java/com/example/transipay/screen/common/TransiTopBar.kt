package com.example.transipay.screen.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.transipay.service.auth.Session
import com.example.transipay.ui.theme.Orange700

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransiTopBar(onSignOut: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }

    val companyName = Session.companyName.ifBlank { "TransiPay" }
    val userName = Session.staffName.ifBlank { Session.role?.name ?: "Account" }
    val initials = userName.split(" ")
        .filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background),
        title = {
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { expanded = true }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(colors.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(initials, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Orange700)
                    }
                    Column(modifier = Modifier.widthIn(max = 220.dp)) {
                        Text(
                            companyName,
                            fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = colors.onBackground,
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            userName,
                            fontSize = 12.sp, color = colors.secondary,
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Account menu", tint = colors.secondary)
                }

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        enabled = false,
                        onClick = {},
                        text = {
                            Column {
                                Text(userName, fontWeight = FontWeight.Bold, color = colors.onSurface)
                                Text(
                                    "${Session.role?.name ?: ""} · $companyName",
                                    fontSize = 12.sp, color = colors.onSurfaceVariant,
                                )
                            }
                        },
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Log out", color = colors.error, fontWeight = FontWeight.SemiBold) },
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Filled.Logout, null, tint = colors.error)
                        },
                        onClick = {
                            expanded = false
                            onSignOut()
                        },
                    )
                }
            }
        },
    )
}