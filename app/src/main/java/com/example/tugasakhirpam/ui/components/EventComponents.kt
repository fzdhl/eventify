package com.example.tugasakhirpam.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tugasakhirpam.model.Event
import com.example.tugasakhirpam.model.EventCategory
import com.example.tugasakhirpam.ui.theme.BorderSoft
import com.example.tugasakhirpam.ui.theme.BrandBlue
import com.example.tugasakhirpam.ui.theme.BrandBlueSoft
import com.example.tugasakhirpam.ui.theme.SurfaceWhite
import com.example.tugasakhirpam.ui.theme.TextMuted
import com.example.tugasakhirpam.ui.theme.TextPrimary
import com.example.tugasakhirpam.ui.theme.TextSecondary
import com.example.tugasakhirpam.util.DateFormatter

fun categoryIcon(category: EventCategory): ImageVector = when (category) {
    EventCategory.SEMINAR -> Icons.Outlined.Mic
    EventCategory.WORKSHOP -> Icons.Outlined.Laptop
    EventCategory.COMPETITION -> Icons.Outlined.EmojiEvents
    EventCategory.ORGANIZATION -> Icons.Outlined.Groups
}

@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
    footer: @Composable (() -> Unit)? = null,
) {
    val imageWidth = 96.dp

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSoft),
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(Modifier.fillMaxWidth()) {
            Box(Modifier.matchParentSize()) {
                EventImage(
                    url = event.imageUrl,
                    contentDescription = event.title,
                    modifier = Modifier
                        .width(imageWidth)
                        .fillMaxHeight()
                )
            }
            Column(
                Modifier
                    .padding(start = imageWidth)
                    .padding(start = 14.dp, end = 12.dp, top = 14.dp, bottom = 14.dp)
                    .heightIn(min = 92.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    CategoryLabel(
                        event.category.label,
                        Modifier
                            .weight(1f)
                            .padding(top = 2.dp)
                    )
                    trailing?.invoke()
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    event.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                IconText(Icons.Outlined.CalendarToday, DateFormatter.short(event.eventDate))
                Spacer(Modifier.height(4.dp))
                IconText(Icons.Outlined.LocationOn, event.location)
                if (footer != null) {
                    Spacer(Modifier.height(10.dp))
                    footer()
                }
            }
        }
    }
}

@Composable
fun FavoriteToggle(isFavorite: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .clickable(onClick = onToggle)
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isFavorite) "Hapus dari favorit" else "Tambah ke favorit",
            tint = if (isFavorite) BrandBlue else TextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun CategoryTile(category: EventCategory, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSoft),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 4.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BrandBlueSoft)
            ) {
                Icon(categoryIcon(category), null, tint = BrandBlue, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                category.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CategoryFilterRow(
    selected: EventCategory?,
    onSelect: (EventCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChipItem("Semua", Icons.Outlined.Apps, selected == null) { onSelect(null) }
        }
        items(EventCategory.entries) { category ->
            FilterChipItem(category.label, categoryIcon(category), selected == category) { onSelect(category) }
        }
    }
}

@Composable
private fun FilterChipItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) BrandBlue else SurfaceWhite,
        border = if (selected) null else BorderStroke(1.dp, BorderSoft),
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = if (selected) Color.White else TextSecondary, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) Color.White else TextSecondary
            )
        }
    }
}
