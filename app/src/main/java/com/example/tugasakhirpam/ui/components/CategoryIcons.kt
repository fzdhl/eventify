package com.example.tugasakhirpam.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.ui.graphics.vector.ImageVector

/** Kategori baru memakai ikon "apps", sama seperti ikon di desain form tambah kategori. */
fun iconForCategory(iconKey: String): ImageVector = when (iconKey) {
    "mic" -> Icons.Outlined.Mic
    "laptop" -> Icons.Outlined.Laptop
    "trophy" -> Icons.Outlined.EmojiEvents
    "groups" -> Icons.Outlined.Groups
    else -> Icons.Outlined.Apps
}
