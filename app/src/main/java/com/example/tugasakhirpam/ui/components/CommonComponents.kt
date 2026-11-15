package com.example.tugasakhirpam.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.tugasakhirpam.model.RegistrationStatus
import com.example.tugasakhirpam.ui.theme.BorderSoft
import com.example.tugasakhirpam.ui.theme.BrandBlue
import com.example.tugasakhirpam.ui.theme.BrandBlueDark
import com.example.tugasakhirpam.ui.theme.BrandBlueLight
import com.example.tugasakhirpam.ui.theme.BrandBlueSoft
import com.example.tugasakhirpam.ui.theme.DangerBg
import com.example.tugasakhirpam.ui.theme.DangerText
import com.example.tugasakhirpam.ui.theme.SegmentTrack
import com.example.tugasakhirpam.ui.theme.SuccessBg
import com.example.tugasakhirpam.ui.theme.SuccessText
import com.example.tugasakhirpam.ui.theme.SurfaceWhite
import com.example.tugasakhirpam.ui.theme.TextPrimary
import com.example.tugasakhirpam.ui.theme.TextSecondary
import com.example.tugasakhirpam.ui.theme.WarningBg
import com.example.tugasakhirpam.ui.theme.WarningText
import kotlinx.coroutines.launch

@Composable
fun CategoryLabel(text: String, modifier: Modifier = Modifier, filled: Boolean = false) {
    val base = if (filled) {
        modifier
            .clip(RoundedCornerShape(6.dp))
            .background(BrandBlueSoft)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    } else modifier
    Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = BrandBlue, maxLines = 1, modifier = base)
}

fun statusColors(status: RegistrationStatus): Pair<Color, Color> = when (status) {
    RegistrationStatus.CONFIRMED -> SuccessBg to SuccessText
    RegistrationStatus.PENDING -> WarningBg to WarningText
    RegistrationStatus.CANCELLED -> DangerBg to DangerText
}

@Composable
fun StatusBadge(status: RegistrationStatus, modifier: Modifier = Modifier) {
    val (bg, fg) = statusColors(status)
    InfoBadge(text = status.label, background = bg, contentColor = fg, modifier = modifier)
}

@Composable
fun InfoBadge(
    text: String,
    modifier: Modifier = Modifier,
    background: Color = BrandBlueSoft,
    contentColor: Color = BrandBlue,
) {
    Text(
        text = text,
        color = contentColor,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
fun EventImage(url: String?, modifier: Modifier = Modifier, contentDescription: String? = null) {
    Box(modifier.background(BrandBlueSoft), contentAlignment = Alignment.Center) {
        Icon(Icons.Outlined.Event, null, tint = BrandBlue.copy(alpha = 0.35f), modifier = Modifier.size(28.dp))
        if (!url.isNullOrBlank()) {
            AsyncImage(
                model = url,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

@Composable
fun IconText(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 11.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun UserAvatar(initials: String, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 46.dp) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .shadow(6.dp, CircleShape, ambientColor = BrandBlue, spotColor = BrandBlue)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(BrandBlueLight, BrandBlue, BrandBlueDark)))
    ) {
        Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.32f).sp)
    }
}

@Composable
fun PageHeader(eyebrow: String, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp)) {
        CategoryLabel(eyebrow)
        Spacer(Modifier.height(4.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        Spacer(Modifier.height(2.dp))
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

@Composable
fun ScreenTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        CircleIconButton(
            icon = Icons.Outlined.ChevronLeft,
            contentDescription = "Kembali",
            onClick = onBack,
            bordered = true,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            modifier = Modifier.align(Alignment.Center)
        )
        Row(Modifier.align(Alignment.CenterEnd), content = actions)
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        if (actionText != null && onAction != null) {
            Text(
                actionText,
                color = BrandBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onAction)
                    .padding(4.dp)
            )
        }
    }
}

@Composable
fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = TextPrimary,
    bordered: Boolean = false,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = SurfaceWhite,
        shadowElevation = if (bordered) 0.dp else 3.dp,
        border = if (bordered) BorderStroke(1.dp, BorderSoft) else null,
        modifier = modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandBlue,
            contentColor = Color.White,
            disabledContainerColor = BrandBlue.copy(alpha = 0.45f),
            disabledContentColor = Color.White,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
        modifier = modifier.height(48.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
        } else {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1)
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    containerColor: Color = BrandBlueSoft,
    contentColor: Color = BrandBlue,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.6f),
            disabledContentColor = contentColor.copy(alpha = 0.6f),
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
        modifier = modifier.height(48.dp)
    ) {
        if (icon != null) {
            Icon(icon, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

@Composable
fun SegmentedTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SegmentTrack)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            val selected = index == selectedIndex
            val bg by animateColorAsState(if (selected) SurfaceWhite else Color.Transparent, label = "tabBg")
            val fg by animateColorAsState(if (selected) BrandBlue else TextSecondary, label = "tabFg")
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .shadow(if (selected) 1.dp else 0.dp, RoundedCornerShape(9.dp))
                    .clip(RoundedCornerShape(9.dp))
                    .background(bg)
                    .clickable { onSelect(index) }
                    .padding(vertical = 9.dp)
            ) {
                Text(title, color = fg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            }
        }
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = BrandBlue, strokeWidth = 3.dp)
    }
}

@Composable
fun MessageState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(BrandBlueSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = BrandBlue, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary, textAlign = TextAlign.Center)
        if (actionText != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            PrimaryButton(actionText, onAction, Modifier.width(180.dp))
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        MessageState(
            icon = Icons.Outlined.CloudOff,
            title = "Gagal memuat data",
            subtitle = message,
            actionText = "Coba Lagi",
            onAction = onRetry
        )
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = true,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(20.dp),
        title = { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
        text = { Text(message, style = MaterialTheme.typography.bodySmall, color = TextSecondary) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = if (destructive) DangerText else BrandBlue, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = TextSecondary) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusPickerSheet(
    title: String,
    current: RegistrationStatus,
    onSelect: (RegistrationStatus) -> Unit,
    onDismiss: () -> Unit,
    onCall: (() -> Unit)? = null,
    onEmail: (() -> Unit)? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = SurfaceWhite) {
        Column(
            Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Text("Kelola peserta", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.bodySmall, color = TextSecondary)

            if (onCall != null || onEmail != null) {
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (onCall != null) {
                        SecondaryButton("Telepon", onCall, Modifier.weight(1f), icon = Icons.Outlined.Call)
                    }
                    if (onEmail != null) {
                        SecondaryButton("Email", onEmail, Modifier.weight(1f), icon = Icons.Outlined.Email)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Ubah status", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
            Spacer(Modifier.height(8.dp))

            RegistrationStatus.entries.forEach { status ->
                val selected = status == current
                val (bg, fg) = statusColors(status)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) bg else SurfaceWhite)
                        .clickable {
                            scope.launch { sheetState.hide() }.invokeOnCompletion { onSelect(status) }
                        }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(fg)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        status.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    if (selected) Icon(Icons.Outlined.Check, null, tint = fg, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
