package com.example.tugasakhirpam.ui.event

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tugasakhirpam.model.Event
import com.example.tugasakhirpam.model.Registration
import com.example.tugasakhirpam.model.RegistrationStatus
import com.example.tugasakhirpam.model.SampleData
import com.example.tugasakhirpam.ui.components.CategoryLabel
import com.example.tugasakhirpam.ui.components.CircleIconButton
import com.example.tugasakhirpam.ui.components.ConfirmDialog
import com.example.tugasakhirpam.ui.components.ErrorState
import com.example.tugasakhirpam.ui.components.EventImage
import com.example.tugasakhirpam.ui.components.InfoBadge
import com.example.tugasakhirpam.ui.components.LoadingState
import com.example.tugasakhirpam.ui.components.PrimaryButton
import com.example.tugasakhirpam.ui.components.SecondaryButton
import com.example.tugasakhirpam.ui.components.StatusBadge
import com.example.tugasakhirpam.ui.components.StatusPickerSheet
import com.example.tugasakhirpam.ui.theme.AppBackground
import com.example.tugasakhirpam.ui.theme.BorderSoft
import com.example.tugasakhirpam.ui.theme.BrandBlue
import com.example.tugasakhirpam.ui.theme.BrandBlueSoft
import com.example.tugasakhirpam.ui.theme.BrandBlueSoftBorder
import com.example.tugasakhirpam.ui.theme.DangerBg
import com.example.tugasakhirpam.ui.theme.DangerText
import com.example.tugasakhirpam.ui.theme.SegmentTrack
import com.example.tugasakhirpam.ui.theme.StarYellow
import com.example.tugasakhirpam.ui.theme.SurfaceWhite
import com.example.tugasakhirpam.ui.theme.TextMuted
import com.example.tugasakhirpam.ui.theme.TextPrimary
import com.example.tugasakhirpam.ui.theme.TextSecondary
import com.example.tugasakhirpam.ui.theme.TugasAkhirPAMTheme
import com.example.tugasakhirpam.util.DateFormatter
import com.example.tugasakhirpam.util.IntentHelper
import kotlinx.coroutines.launch

@Composable
fun EventDetailScreen(
    state: EventDetailUiState,
    resultMessage: String?,
    onResultConsumed: () -> Unit,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRegisterClick: (Long) -> Unit,
    onViewRegistration: (Long) -> Unit,
    onEditEvent: (Long) -> Unit,
    onViewReviews: (Long) -> Unit,
    onDeleteEvent: () -> Unit,
    onUpdateParticipantStatus: (Long, RegistrationStatus) -> Unit,
    onSnackbarDismissed: () -> Unit,
    onDeleted: (String) -> Unit,
    onRetry: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var participantToEdit by remember { mutableStateOf<Registration?>(null) }

    LaunchedEffect(resultMessage) {
        resultMessage?.let {
            snackbarHostState.showSnackbar(it)
            onResultConsumed()
        }
    }
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            onSnackbarDismissed()
        }
    }
    LaunchedEffect(state.deletedMessage) {
        state.deletedMessage?.let(onDeleted)
    }

    val launchIntent: (Boolean) -> Unit = { opened ->
        if (!opened) scope.launch { snackbarHostState.showSnackbar("Tidak ada aplikasi yang bisa membuka ini") }
    }

    EventDetailContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onRetry = onRetry,
        onToggleFavorite = onToggleFavorite,
        onShare = { event -> launchIntent(IntentHelper.shareEvent(context, event)) },
        onOpenMaps = { event -> launchIntent(IntentHelper.openMaps(context, event.location)) },
        onAddToCalendar = { event -> launchIntent(IntentHelper.addToCalendar(context, event)) },
        onRegisterClick = { state.event?.id?.let(onRegisterClick) },
        onViewRegistration = onViewRegistration,
        onEditEvent = { state.event?.id?.let(onEditEvent) },
        onViewReviews = { state.event?.id?.let(onViewReviews) },
        onDeleteClick = { showDeleteDialog = true },
        onParticipantClick = { participantToEdit = it },
    )

    if (showDeleteDialog) {
        val activeCount = state.participants.count { it.isActive }
        ConfirmDialog(
            title = "Hapus event?",
            message = if (activeCount > 0) {
                "Event akan dihapus permanen beserta $activeCount pendaftaran peserta. Tindakan ini tidak bisa dibatalkan."
            } else {
                "Event akan dihapus permanen dan tidak bisa dikembalikan."
            },
            confirmText = "Hapus",
            onConfirm = {
                showDeleteDialog = false
                onDeleteEvent()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    participantToEdit?.let { participant ->
        StatusPickerSheet(
            title = "${participant.fullName} • ${participant.nim}",
            current = participant.status,
            onSelect = { status ->
                participantToEdit = null
                if (status != participant.status) onUpdateParticipantStatus(participant.id, status)
            },
            onDismiss = { participantToEdit = null },
            onCall = { launchIntent(IntentHelper.dialPhone(context, participant.phone)) },
            onEmail = {
                launchIntent(
                    IntentHelper.sendEmail(context, participant.email, "Info event ${state.event?.title.orEmpty()}")
                )
            },
        )
    }
}

@Composable
fun EventDetailContent(
    state: EventDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShare: (Event) -> Unit,
    onOpenMaps: (Event) -> Unit,
    onAddToCalendar: (Event) -> Unit,
    onRegisterClick: () -> Unit,
    onViewRegistration: (Long) -> Unit,
    onEditEvent: () -> Unit,
    onViewReviews: () -> Unit,
    onDeleteClick: () -> Unit,
    onParticipantClick: (Registration) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val event = state.event

    Scaffold(
        containerColor = AppBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (event != null) {
                DetailBottomBar(
                    state = state,
                    event = event,
                    onToggleFavorite = onToggleFavorite,
                    onRegisterClick = onRegisterClick,
                    onViewRegistration = onViewRegistration,
                    onEditEvent = onEditEvent,
                    onDeleteClick = onDeleteClick,
                )
            }
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading -> LoadingState()
                event == null -> ErrorState(state.errorMessage ?: "Event tidak ditemukan", onRetry)
                else -> Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    DetailHeader(
                        event = event,
                        showFavorite = !state.isOwner,
                        onBack = onBack,
                        onShare = { onShare(event) },
                        onToggleFavorite = onToggleFavorite,
                    )
                    Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                        EventInfoSection(event = event, isOwner = state.isOwner)

                        Spacer(Modifier.height(16.dp))
                        RatingRow(event = event, onClick = onViewReviews)

                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            QuickActionChip(Icons.Outlined.CalendarMonth, "Simpan ke Kalender", Modifier.weight(1f)) {
                                onAddToCalendar(event)
                            }
                            QuickActionChip(Icons.Outlined.Map, "Buka Peta", Modifier.weight(1f)) {
                                onOpenMaps(event)
                            }
                        }

                        val myRegistration = state.myRegistration
                        if (myRegistration != null) {
                            Spacer(Modifier.height(16.dp))
                            RegisteredBanner(myRegistration, onClick = { onViewRegistration(myRegistration.id) })
                        }

                        if (state.isOwner) {
                            Spacer(Modifier.height(24.dp))
                            ParticipantSection(
                                participants = state.participants,
                                enabled = !state.isProcessing,
                                onParticipantClick = onParticipantClick
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailHeader(
    event: Event,
    showFavorite: Boolean,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(230.dp)
    ) {
        EventImage(
            url = event.imageUrl,
            contentDescription = event.title,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleIconButton(Icons.Outlined.ChevronLeft, "Kembali", onBack)
            Spacer(Modifier.weight(1f))
            CircleIconButton(Icons.Outlined.Share, "Bagikan", onShare)
            if (showFavorite) {
                Spacer(Modifier.width(10.dp))
                CircleIconButton(
                    icon = if (event.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorit",
                    onClick = onToggleFavorite,
                    tint = BrandBlue
                )
            }
        }
    }
}

@Composable
private fun EventInfoSection(event: Event, isOwner: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        CategoryLabel(event.categoryName, filled = true)
        Spacer(Modifier.weight(1f))
        if (isOwner) InfoBadge("Event kamu")
        if (event.isPast) InfoBadge("Selesai", background = SegmentTrack, contentColor = TextSecondary)
    }
    Spacer(Modifier.height(10.dp))
    Text(event.title, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
    Spacer(Modifier.height(4.dp))
    Text(
        buildAnnotatedString {
            append("Oleh ")
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = TextPrimary)) { append(event.organizer) }
        },
        fontSize = 11.sp,
        color = TextSecondary
    )

    SectionDivider()

    Row(Modifier.fillMaxWidth()) {
        InfoItem(Icons.Outlined.CalendarToday, "Tanggal", DateFormatter.full(event.eventDate))
        InfoItem(Icons.Outlined.Schedule, "Waktu", DateFormatter.timeRange(event.startTime, event.endTime))
    }
    Spacer(Modifier.height(16.dp))
    Row(Modifier.fillMaxWidth()) {
        InfoItem(Icons.Outlined.LocationOn, "Lokasi", event.location)
        InfoItem(
            Icons.Outlined.Groups, "Kuota",
            if (event.isFull) "Penuh (${event.quota} peserta)" else "${event.remainingQuota} dari ${event.quota} tersisa"
        )
    }

    SectionDivider()

    Text("Tentang event", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
    Spacer(Modifier.height(8.dp))
    Text(
        event.description.ifBlank { "Belum ada deskripsi untuk event ini." },
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary,
        lineHeight = 18.sp
    )
}

@Composable
private fun SectionDivider() {
    Spacer(Modifier.height(16.dp))
    HorizontalDivider(color = BorderSoft)
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun RowScope.InfoItem(icon: ImageVector, label: String, value: String) {
    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = BrandBlue, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 10.sp, color = TextMuted)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
    }
}

@Composable
private fun QuickActionChip(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSoft),
        modifier = modifier
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = BrandBlue, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1)
        }
    }
}

@Composable
private fun RatingRow(event: Event, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSoft),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Star, null, tint = StarYellow, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            if (event.reviewCount > 0) {
                Text(
                    DateFormatter.rating(event.ratingAverage),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarYellow
                )
                Spacer(Modifier.width(6.dp))
                Text("${event.reviewCount} ulasan", fontSize = 11.sp, color = TextSecondary)
            } else {
                Text("Belum ada ulasan", fontSize = 12.sp, color = TextSecondary)
            }
            Spacer(Modifier.weight(1f))
            Text("Lihat ulasan", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Icon(Icons.Outlined.ChevronRight, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun RegisteredBanner(registration: Registration, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = BrandBlueSoft,
        border = BorderStroke(1.dp, BrandBlueSoftBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (registration.isActive) "Kamu sudah terdaftar di event ini" else "Pendaftaranmu dibatalkan",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                StatusBadge(registration.status)
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = BrandBlue)
        }
    }
}

@Composable
private fun ParticipantSection(
    participants: List<Registration>,
    enabled: Boolean,
    onParticipantClick: (Registration) -> Unit,
) {
    Text(
        "Peserta (${participants.count { it.isActive }} aktif)",
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary
    )
    Spacer(Modifier.height(4.dp))
    Text("Ketuk peserta untuk menghubungi atau mengubah statusnya.", fontSize = 11.sp, color = TextSecondary)
    Spacer(Modifier.height(12.dp))

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSoft),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (participants.isEmpty()) {
            Text(
                "Belum ada peserta yang mendaftar.",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            Column {
                participants.forEachIndexed { index, participant ->
                    ParticipantRow(participant, enabled) { onParticipantClick(participant) }
                    if (index < participants.lastIndex) {
                        HorizontalDivider(color = BorderSoft, modifier = Modifier.padding(horizontal = 14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantRow(participant: Registration, enabled: Boolean, onClick: () -> Unit) {
    val initials = participant.fullName.split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }

    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(BrandBlueSoft)
        ) {
            Text(initials, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(participant.fullName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(
                "${participant.nim} • ${participant.email}",
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        StatusBadge(participant.status)
    }
}

@Composable
private fun DetailBottomBar(
    state: EventDetailUiState,
    event: Event,
    onToggleFavorite: () -> Unit,
    onRegisterClick: () -> Unit,
    onViewRegistration: (Long) -> Unit,
    onEditEvent: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Surface(color = SurfaceWhite, shadowElevation = 8.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.isOwner) {
                SecondaryButton(
                    text = "Hapus",
                    onClick = onDeleteClick,
                    enabled = !state.isProcessing,
                    icon = Icons.Outlined.DeleteOutline,
                    containerColor = DangerBg,
                    contentColor = DangerText,
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    text = "Edit Event",
                    onClick = onEditEvent,
                    loading = state.isProcessing,
                    icon = Icons.Outlined.Edit,
                    modifier = Modifier.weight(1.4f)
                )
            } else {
                SecondaryButton(
                    text = if (event.isFavorite) "Favorited" else "Favorite",
                    onClick = onToggleFavorite,
                    icon = if (event.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    modifier = Modifier.weight(1f)
                )
                val registration = state.myRegistration
                when (state.registerAction) {
                    RegisterAction.REGISTER -> PrimaryButton("Register Now", onRegisterClick, Modifier.weight(1.4f))
                    RegisterAction.VIEW_REGISTRATION -> PrimaryButton(
                        "Lihat Pendaftaran",
                        { registration?.let { onViewRegistration(it.id) } },
                        Modifier.weight(1.4f)
                    )
                    RegisterAction.EVENT_PAST -> PrimaryButton("Event Selesai", {}, Modifier.weight(1.4f), enabled = false)
                    RegisterAction.QUOTA_FULL -> PrimaryButton("Kuota Penuh", {}, Modifier.weight(1.4f), enabled = false)
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun EventDetailContentPreview() {
    TugasAkhirPAMTheme {
        EventDetailContent(
            state = EventDetailUiState(isLoading = false, event = SampleData.event),
            onBack = {},
            onRetry = {},
            onToggleFavorite = {},
            onShare = {},
            onOpenMaps = {},
            onAddToCalendar = {},
            onRegisterClick = {},
            onViewRegistration = {},
            onEditEvent = {},
            onViewReviews = {},
            onDeleteClick = {},
            onParticipantClick = {},
        )
    }
}
