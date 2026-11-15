package com.example.tugasakhirpam.ui.registration

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tugasakhirpam.model.Registration
import com.example.tugasakhirpam.model.RegistrationStatus
import com.example.tugasakhirpam.model.SampleData
import com.example.tugasakhirpam.ui.components.CategoryLabel
import com.example.tugasakhirpam.ui.components.CircleIconButton
import com.example.tugasakhirpam.ui.components.ConfirmDialog
import com.example.tugasakhirpam.ui.components.ErrorState
import com.example.tugasakhirpam.ui.components.EventImage
import com.example.tugasakhirpam.ui.components.LoadingState
import com.example.tugasakhirpam.ui.components.PrimaryButton
import com.example.tugasakhirpam.ui.components.SecondaryButton
import com.example.tugasakhirpam.ui.components.StatusBadge
import com.example.tugasakhirpam.ui.components.statusColors
import com.example.tugasakhirpam.ui.theme.AppBackground
import com.example.tugasakhirpam.ui.theme.BorderSoft
import com.example.tugasakhirpam.ui.theme.BrandBlue
import com.example.tugasakhirpam.ui.theme.DangerBg
import com.example.tugasakhirpam.ui.theme.DangerText
import com.example.tugasakhirpam.ui.theme.SurfaceWhite
import com.example.tugasakhirpam.ui.theme.TextMuted
import com.example.tugasakhirpam.ui.theme.TextPrimary
import com.example.tugasakhirpam.ui.theme.TextSecondary
import com.example.tugasakhirpam.ui.theme.TugasAkhirPAMTheme
import com.example.tugasakhirpam.util.DateFormatter

@Composable
fun RegistrationDetailScreen(
    registrationId: Long,
    resultMessage: String?,
    onResultConsumed: () -> Unit,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onViewEvent: (Long) -> Unit,
    onViewReviews: (Long) -> Unit,
    onDeleted: (String) -> Unit,
    viewModel: RegistrationDetailViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCancelDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(registrationId) { viewModel.load(registrationId) }

    LaunchedEffect(resultMessage) {
        resultMessage?.let {
            snackbarHostState.showSnackbar(it)
            onResultConsumed()
        }
    }
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.snackbarShown()
        }
    }
    LaunchedEffect(state.deletedMessage) {
        state.deletedMessage?.let(onDeleted)
    }

    RegistrationDetailContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onRetry = { viewModel.load(registrationId) },
        onEdit = { onEdit(registrationId) },
        onViewEvent = onViewEvent,
        onViewReviews = onViewReviews,
        onCancel = { showCancelDialog = true },
        onReRegister = viewModel::reRegister,
        onDelete = { showDeleteDialog = true },
    )

    if (showCancelDialog) {
        ConfirmDialog(
            title = "Batalkan pendaftaran?",
            message = "Kursimu akan dilepas untuk peserta lain. Kamu masih bisa daftar ulang selama kuota tersedia.",
            confirmText = "Ya, batalkan",
            onConfirm = {
                showCancelDialog = false
                viewModel.cancel()
            },
            onDismiss = { showCancelDialog = false }
        )
    }
    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Hapus pendaftaran?",
            message = "Data pendaftaran akan dihapus permanen dari riwayatmu.",
            confirmText = "Hapus",
            onConfirm = {
                showDeleteDialog = false
                viewModel.delete()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
fun RegistrationDetailContent(
    state: RegistrationDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onEdit: () -> Unit,
    onViewEvent: (Long) -> Unit,
    onViewReviews: (Long) -> Unit,
    onCancel: () -> Unit,
    onReRegister: () -> Unit,
    onDelete: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val registration = state.registration

    Scaffold(
        containerColor = AppBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (registration != null) {
                DetailActionBar(
                    registration = registration,
                    isProcessing = state.isProcessing,
                    onEdit = onEdit,
                    onViewEvent = { onViewEvent(registration.eventId) },
                    onViewReviews = { onViewReviews(registration.eventId) },
                    onCancel = onCancel,
                    onReRegister = onReRegister,
                    onDelete = onDelete,
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
                registration == null -> ErrorState(state.errorMessage ?: "Data tidak ditemukan", onRetry)
                else -> DetailBody(registration, onBack)
            }
        }
    }
}

@Composable
private fun DetailBody(registration: Registration, onBack: () -> Unit) {
    val event = registration.event

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(210.dp)
        ) {
            EventImage(
                url = event?.imageUrl,
                contentDescription = event?.title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
            )
            CircleIconButton(Icons.Outlined.ChevronLeft, "Kembali", onBack, Modifier.padding(16.dp))
        }

        Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryLabel(event?.category?.label ?: "Event", filled = true)
                Spacer(Modifier.weight(1f))
                StatusBadge(registration.status)
            }
            Spacer(Modifier.height(10.dp))
            Text(event?.title ?: "Event tidak ditemukan", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            if (event != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    buildAnnotatedString {
                        append("Oleh ")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = TextPrimary)) { append(event.organizer) }
                    },
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            SectionDivider()

            Row(Modifier.fillMaxWidth()) {
                InfoItem(Icons.Outlined.CalendarToday, "Tanggal", DateFormatter.full(event?.eventDate))
                InfoItem(Icons.Outlined.Schedule, "Waktu", DateFormatter.timeRange(event?.startTime, event?.endTime))
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth()) {
                InfoItem(Icons.Outlined.LocationOn, "Lokasi", event?.location ?: "-")
                InfoItem(Icons.Outlined.History, "Didaftarkan", DateFormatter.timestamp(registration.createdAt))
            }

            Spacer(Modifier.height(20.dp))
            StatusInfoCard(registration)

            Spacer(Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SurfaceWhite,
                border = BorderStroke(1.dp, BorderSoft),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Data Pendaftaran", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                    Spacer(Modifier.height(12.dp))
                    InfoLine(Icons.Outlined.Person, "Nama lengkap", registration.fullName)
                    InfoLine(Icons.Outlined.Badge, "NIM", registration.nim)
                    InfoLine(Icons.Outlined.Email, "Email", registration.email)
                    InfoLine(Icons.Outlined.Phone, "Nomor HP", registration.phone, last = true)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatusInfoCard(registration: Registration) {
    val isPast = registration.event?.isPast == true
    val message = when {
        registration.status == RegistrationStatus.CANCELLED ->
            if (isPast) "Pendaftaran dibatalkan dan event sudah selesai."
            else "Pendaftaran dibatalkan. Kamu bisa daftar ulang selama kuota masih tersedia."
        isPast -> "Event sudah selesai. Terima kasih sudah ikut! Bagikan pengalamanmu lewat ulasan."
        registration.status == RegistrationStatus.PENDING -> "Menunggu konfirmasi dari penyelenggara event."
        else -> "Pendaftaranmu sudah dikonfirmasi. Datang 15 menit sebelum acara dimulai, ya!"
    }
    val (bg, fg) = statusColors(registration.status)

    Surface(shape = RoundedCornerShape(14.dp), color = bg, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Info, null, tint = fg, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(message, fontSize = 12.sp, color = TextPrimary, lineHeight = 17.sp)
        }
    }
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
private fun InfoLine(icon: ImageVector, label: String, value: String, last: Boolean = false) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = TextMuted, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(1f))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
    if (!last) HorizontalDivider(color = BorderSoft)
}

@Composable
private fun DetailActionBar(
    registration: Registration,
    isProcessing: Boolean,
    onEdit: () -> Unit,
    onViewEvent: () -> Unit,
    onViewReviews: () -> Unit,
    onCancel: () -> Unit,
    onReRegister: () -> Unit,
    onDelete: () -> Unit,
) {
    val isPast = registration.event?.isPast == true

    Surface(color = SurfaceWhite, shadowElevation = 8.dp) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp)
        ) {
            when {
                isPast -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SecondaryButton(
                            text = "Hapus Riwayat",
                            onClick = onDelete,
                            enabled = !isProcessing,
                            icon = Icons.Outlined.DeleteOutline,
                            containerColor = DangerBg,
                            contentColor = DangerText,
                            modifier = Modifier.weight(1f)
                        )
                        if (registration.status == RegistrationStatus.CONFIRMED) {
                            PrimaryButton("Beri Ulasan", onViewReviews, Modifier.weight(1f), icon = Icons.Outlined.RateReview)
                        } else {
                            PrimaryButton("Lihat Event", onViewEvent, Modifier.weight(1f), icon = Icons.Outlined.Event)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                registration.isActive -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SecondaryButton(
                            text = "Edit Data",
                            onClick = onEdit,
                            enabled = !isProcessing,
                            icon = Icons.Outlined.Edit,
                            modifier = Modifier.weight(1f)
                        )
                        PrimaryButton(
                            text = "Lihat Event",
                            onClick = onViewEvent,
                            loading = isProcessing,
                            icon = Icons.Outlined.Event,
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                    TextButton(onClick = onCancel, enabled = !isProcessing, modifier = Modifier.fillMaxWidth()) {
                        Text("Batalkan Pendaftaran", color = DangerText, style = MaterialTheme.typography.labelLarge)
                    }
                }

                else -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SecondaryButton(
                            text = "Hapus",
                            onClick = onDelete,
                            enabled = !isProcessing,
                            icon = Icons.Outlined.DeleteOutline,
                            containerColor = DangerBg,
                            contentColor = DangerText,
                            modifier = Modifier.weight(1f)
                        )
                        PrimaryButton(
                            text = "Daftar Ulang",
                            onClick = onReRegister,
                            loading = isProcessing,
                            icon = Icons.Outlined.Refresh,
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun RegistrationDetailContentPreview() {
    TugasAkhirPAMTheme {
        RegistrationDetailContent(
            state = RegistrationDetailUiState(isLoading = false, registration = SampleData.registration),
            onBack = {},
            onRetry = {},
            onEdit = {},
            onViewEvent = {},
            onViewReviews = {},
            onCancel = {},
            onReRegister = {},
            onDelete = {},
        )
    }
}
