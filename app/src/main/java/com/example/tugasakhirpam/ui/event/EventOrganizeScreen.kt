package com.example.tugasakhirpam.ui.event

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tugasakhirpam.ui.components.CircleIconButton
import com.example.tugasakhirpam.ui.components.EventImage
import com.example.tugasakhirpam.ui.theme.AppBackground
import com.example.tugasakhirpam.ui.theme.BorderSoft
import com.example.tugasakhirpam.ui.theme.BrandBlue
import com.example.tugasakhirpam.ui.theme.BrandBlueSoft
import com.example.tugasakhirpam.ui.theme.DangerBg
import com.example.tugasakhirpam.ui.theme.DangerText
import com.example.tugasakhirpam.ui.theme.SurfaceWhite
import com.example.tugasakhirpam.ui.theme.TextMuted
import com.example.tugasakhirpam.ui.theme.TextPrimary
import com.example.tugasakhirpam.ui.theme.TextSecondary
import com.example.tugasakhirpam.ui.theme.TugasAkhirPAMTheme

data class EventOrganizeUiModel(
    val title: String = "Future Tech Summit 2025",
    val categoryLabel: String = "TECHNOLOGY",
    val dateText: String = "24 Mei 2025",
    val timeText: String = "09.00 – 15.00",
    val location: String = "Auditorium Utama",
    val registeredCount: Int = 108,
    val quota: Int = 150,
    val description: String = "Seminar teknologi kampus bersama praktisi industri, membahas tren AI dan peluang karier digital.",
    val imageUrl: String? = null,
) {
    val quotaProgress: Float
        get() = if (quota > 0) (registeredCount.toFloat() / quota.toFloat()).coerceIn(0f, 1f) else 0f

    val quotaPercentage: Int
        get() = (quotaProgress * 100).toInt()
}

@Composable
fun EventOrganizeScreen(
    event: EventOrganizeUiModel = EventOrganizeUiModel(),
    onBack: () -> Unit = {},
    onEditEvent: () -> Unit = {},
    onDeleteEvent: () -> Unit = {},
) {
    Scaffold(
        containerColor = AppBackground,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            OrganizeHeroHeader(
                imageUrl = event.imageUrl,
                title = event.title,
                onBack = onBack,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                // Category Badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = BrandBlueSoft,
                ) {
                    Text(
                        text = event.categoryLabel.uppercase(),
                        color = BrandBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Event Title
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    color = TextPrimary,
                )

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = BorderSoft)
                Spacer(Modifier.height(16.dp))

                // 2x2 Info Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OrganizeInfoItem(
                        icon = Icons.Outlined.CalendarToday,
                        label = "Tanggal",
                        value = event.dateText,
                    )
                    OrganizeInfoItem(
                        icon = Icons.Outlined.Schedule,
                        label = "Waktu",
                        value = event.timeText,
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OrganizeInfoItem(
                        icon = Icons.Outlined.LocationOn,
                        label = "Lokasi",
                        value = event.location,
                    )
                    OrganizeInfoItem(
                        icon = Icons.Outlined.Groups,
                        label = "Peserta",
                        value = "${event.registeredCount} / ${event.quota}",
                    )
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = BorderSoft)
                Spacer(Modifier.height(16.dp))

                // Description Section
                Text(
                    text = "Deskripsi",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = TextPrimary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.5.sp,
                        lineHeight = 19.sp,
                    ),
                    color = TextSecondary,
                )

                Spacer(Modifier.height(18.dp))

                // Quota Progress Card
                QuotaProgressCard(
                    percentage = event.quotaPercentage,
                    progress = event.quotaProgress,
                )

                Spacer(Modifier.height(20.dp))

                // Edit & Delete Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onEditEvent,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandBlue,
                            contentColor = Color.White,
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .shadow(
                                elevation = 10.dp,
                                shape = RoundedCornerShape(14.dp),
                                ambientColor = BrandBlue.copy(alpha = 0.45f),
                                spotColor = BrandBlue.copy(alpha = 0.45f),
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Edit Event",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    Button(
                        onClick = onDeleteEvent,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DangerBg,
                            contentColor = DangerText,
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Delete",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun OrganizeHeroHeader(
    imageUrl: String?,
    title: String,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
    ) {
        EventImage(
            url = imageUrl,
            contentDescription = title,
            modifier = Modifier.fillMaxSize()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleIconButton(
                icon = Icons.Outlined.ChevronLeft,
                contentDescription = "Kembali",
                onClick = onBack,
            )

            Spacer(Modifier.weight(1f))

            Surface(
                shape = CircleShape,
                color = SurfaceWhite,
                shadowElevation = 2.dp,
            ) {
                Text(
                    text = "Your Event",
                    color = BrandBlue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun RowScope.OrganizeInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BrandBlueSoft)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BrandBlue,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                fontSize = 10.sp,
                color = TextMuted,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
        }
    }
}

@Composable
private fun QuotaProgressCard(
    percentage: Int,
    progress: Float,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSoft),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$percentage%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Kuota terisi",
                    fontSize = 11.sp,
                    color = TextMuted,
                )
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(BrandBlueSoft)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(BrandBlue)
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 820)
@Composable
private fun EventOrganizeScreenPreview() {
    TugasAkhirPAMTheme {
        EventOrganizeScreen()
    }
}
