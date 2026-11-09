package com.example.transipay.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Contactless
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.transipay.ui.theme.Orange500
import com.example.transipay.ui.theme.Orange700

enum class HomeTab(val route: String, val label: String, val icon: ImageVector) {
    Fleet("fleet", "Fleet", Icons.Outlined.DirectionsBus),
    Staff("staff", "Staff", Icons.Outlined.Badge),
    Payments("payments", "Payments", Icons.Outlined.Receipt),
    Terminal("terminal", "Terminal", Icons.Outlined.Contactless),
}

private val SelectedLabel = Color(0xFF0F6B3A)   // dark green label on the active tab
private val UnselectedTint = Color(0xFF38453D)  // dark slate for inactive icon + label

@Composable
fun TransiBottomBar(
    currentRoute: String?,
    onSelect: (HomeTab) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        modifier = Modifier.shadow(6.dp),
    ) {
        HomeTab.entries.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
                selected = selected,
                onClick = { if (!selected) onSelect(tab) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = {
                    Text(
                        tab.label,
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Orange700,
                    selectedTextColor = SelectedLabel,
                    indicatorColor = Orange500.copy(alpha = 0.18f),
                    unselectedIconColor = UnselectedTint,
                    unselectedTextColor = UnselectedTint,
                ),
            )
        }
    }
}