package com.example.tugasakhirpam.ui.profile

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.tugasakhirpam.model.User
import com.example.tugasakhirpam.ui.components.UserAvatar
import com.example.tugasakhirpam.ui.theme.BrandBlue
import com.example.tugasakhirpam.ui.theme.SurfaceWhite

/**
 * Foto profil bulat. Urutan sumber: pratinjau foto yang baru dipilih, foto tersimpan di Supabase Storage,
 * lalu inisial nama jika user belum punya foto.
 */
@Composable
fun ProfileAvatar(
    user: User?,
    modifier: Modifier = Modifier,
    size: Dp = 88.dp,
    previewUri: String? = null,
) {
    val model = previewUri ?: user?.avatarUrl
    if (model.isNullOrBlank()) {
        UserAvatar(initials = user?.initials ?: "?", modifier = modifier, size = size)
    } else {
        Box(modifier.size(size)) {
            AsyncImage(
                model = model,
                contentDescription = "Foto profil",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .shadow(8.dp, CircleShape, ambientColor = BrandBlue, spotColor = BrandBlue)
                    .clip(CircleShape)
                    .border(3.dp, SurfaceWhite, CircleShape),
            )
        }
    }
}
