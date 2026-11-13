package com.example.tugasakhirpam.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tugasakhirpam.ui.theme.BorderSoft
import com.example.tugasakhirpam.ui.theme.BrandBlue
import com.example.tugasakhirpam.ui.theme.SurfaceWhite
import com.example.tugasakhirpam.ui.theme.TextMuted

enum class BottomNavItem(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Outlined.Home),
    CATEGORIES("Categories", Icons.Outlined.GridView),
    FAVORITES("Favorites", Icons.Outlined.FavoriteBorder),
    REGISTRATIONS("Registrations", Icons.Outlined.ConfirmationNumber),
    MY_EVENTS("My Events", Icons.Outlined.CalendarToday),
}

@Composable
fun AppBottomBar(selected: BottomNavItem, onItemClick: (BottomNavItem) -> Unit) {
    Column {
        HorizontalDivider(color = BorderSoft)
        NavigationBar(containerColor = SurfaceWhite, tonalElevation = 0.dp) {
            BottomNavItem.entries.forEach { item ->
                NavigationBarItem(
                    selected = item == selected,
                    onClick = { if (item != selected) onItemClick(item) },
                    icon = { Icon(item.icon, item.label, modifier = Modifier.size(22.dp)) },
                    label = {
                        Text(
                            item.label,
                            fontSize = 10.sp,
                            maxLines = 1,
                            fontWeight = if (item == selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandBlue,
                        selectedTextColor = BrandBlue,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = Color.Transparent,
                    )
                )
            }
        }
    }
}
