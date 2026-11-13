package com.example.tugasakhirpam.ui.registration

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tugasakhirpam.model.Event
import com.example.tugasakhirpam.model.SampleData
import com.example.tugasakhirpam.ui.components.CategoryLabel
import com.example.tugasakhirpam.ui.components.EventImage
import com.example.tugasakhirpam.ui.components.FormTextField
import com.example.tugasakhirpam.ui.components.InfoBadge
import com.example.tugasakhirpam.ui.components.LoadingState
import com.example.tugasakhirpam.ui.components.PrimaryButton
import com.example.tugasakhirpam.ui.components.ScreenTopBar
import com.example.tugasakhirpam.ui.theme.AppBackground
import com.example.tugasakhirpam.ui.theme.BorderSoft
import com.example.tugasakhirpam.ui.theme.BrandBlue
import com.example.tugasakhirpam.ui.theme.BrandBlueSoft
import com.example.tugasakhirpam.ui.theme.BrandBlueSoftBorder
import com.example.tugasakhirpam.ui.theme.DangerText
import com.example.tugasakhirpam.ui.theme.SegmentTrack
import com.example.tugasakhirpam.ui.theme.SurfaceWhite
import com.example.tugasakhirpam.ui.theme.TextMuted
import com.example.tugasakhirpam.ui.theme.TextPrimary
import com.example.tugasakhirpam.ui.theme.TextSecondary
import com.example.tugasakhirpam.ui.theme.TugasAkhirPAMTheme
import com.example.tugasakhirpam.util.DateFormatter
import kotlinx.coroutines.launch

@Composable
fun RegistrationFormScreen(
    eventId: Long?,
    registrationId: Long?,
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: RegistrationFormViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showEventPicker by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.init(eventId, registrationId) }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.errorShown()
        }
    }
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let(onSaved)
    }

    RegistrationFormContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onEventCardClick = { showEventPicker = true },
        onFullNameChange = viewModel::onFullNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPhoneChange = viewModel::onPhoneChange,
        onSubmit = viewModel::submit,
    )

    if (showEventPicker) {
        EventPickerSheet(
            options = state.options,
            selectedId = state.selectedEvent?.id,
            onSelect = {
                viewModel.onEventSelected(it)
                showEventPicker = false
            },
            onDismiss = { showEventPicker = false }
        )
    }
}

@Composable
fun RegistrationFormContent(
    state: RegistrationFormUiState,
    onBack: () -> Unit,
    onEventCardClick: () -> Unit,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSubmit: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        containerColor = AppBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (state.isLoadingInitial) {
            LoadingState(Modifier.padding(innerPadding))
            return@Scaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            ScreenTopBar(if (state.isEditMode) "Edit Registration" else "Event Registration", onBack)
            Spacer(Modifier.height(8.dp))

            EventSummaryCard(
                event = state.selectedEvent,
                selectable = !state.isEditMode,
                isError = state.eventError != null,
                onClick = {
                    focusManager.clearFocus()
                    onEventCardClick()
                }
            )
            state.eventError?.let {
                Text(it, color = DangerText, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 6.dp))
            }
            Spacer(Modifier.height(20.dp))

            FormTextField(
                label = "Nama lengkap",
                value = state.fullName,
                onValueChange = onFullNameChange,
                leadingIcon = Icons.Outlined.Person,
                placeholder = "Masukkan nama lengkap",
                error = state.fullNameError,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )
            FormTextField(
                label = "NIM",
                value = state.nim,
                onValueChange = {},
                enabled = false,
                leadingIcon = Icons.Outlined.Badge,
                placeholder = "NIM",
                helperText = "NIM diambil dari akunmu dan tidak bisa diubah",
            )
            FormTextField(
                label = "Email",
                value = state.email,
                onValueChange = onEmailChange,
                leadingIcon = Icons.Outlined.Email,
                placeholder = "nama@univ.ac.id",
                error = state.emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )
            FormTextField(
                label = "Nomor HP",
                value = state.phone,
                onValueChange = onPhoneChange,
                leadingIcon = Icons.Outlined.Phone,
                placeholder = "+62 8xx xxxx xxxx",
                error = state.phoneError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done)
            )

            Spacer(Modifier.height(4.dp))
            PrimaryButton(
                text = if (state.isEditMode) "Simpan Perubahan" else "Submit Registration",
                onClick = {
                    focusManager.clearFocus()
                    onSubmit()
                },
                loading = state.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onBack, enabled = !state.isSubmitting, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EventSummaryCard(
    event: Event?,
    selectable: Boolean,
    isError: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = selectable,
        shape = RoundedCornerShape(14.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, if (isError) DangerText else BorderSoft),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            EventImage(
                url = event?.imageUrl,
                modifier = Modifier
                    .size(width = 64.dp, height = 56.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                CategoryLabel(if (event != null) "Mendaftar untuk" else "Pilih event")
                Spacer(Modifier.height(3.dp))
                Text(
                    text = event?.title ?: "Belum ada event dipilih",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 13.sp,
                    color = if (event != null) TextPrimary else TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = event?.let { "${DateFormatter.dayMonth(it.eventDate)} · ${it.location}" }
                        ?: "Ketuk untuk memilih event",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (selectable) Icon(Icons.Outlined.KeyboardArrowDown, "Pilih event", tint = TextMuted)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventPickerSheet(
    options: List<EventOption>,
    selectedId: Long?,
    onSelect: (EventOption) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = SurfaceWhite) {
        Text(
            "Pilih Event",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Text(
            "Event yang sudah kamu ikuti, milikmu, atau penuh tidak dapat dipilih.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )
        if (options.isEmpty()) {
            Text(
                "Belum ada event mendatang.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.padding(20.dp)
            )
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(options, key = { it.event.id }) { option ->
                val event = option.event
                val selected = event.id == selectedId
                val enabled = option.disabledReason == null
                Surface(
                    onClick = { scope.launch { sheetState.hide() }.invokeOnCompletion { onSelect(option) } },
                    enabled = enabled,
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        selected -> BrandBlueSoft
                        enabled -> SurfaceWhite
                        else -> SegmentTrack
                    },
                    border = BorderStroke(1.dp, if (selected) BrandBlueSoftBorder else BorderSoft),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        EventImage(
                            url = event.imageUrl,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            CategoryLabel(event.category.label)
                            Text(
                                event.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 13.sp,
                                color = if (enabled) TextPrimary else TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${DateFormatter.short(event.eventDate)} · ${event.remainingQuota} kursi tersisa",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Normal
                            )
                        }
                        when {
                            option.disabledReason != null -> InfoBadge(option.disabledReason)
                            selected -> Icon(Icons.Outlined.CheckCircle, null, tint = BrandBlue, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun RegistrationFormContentPreview() {
    TugasAkhirPAMTheme {
        RegistrationFormContent(
            state = RegistrationFormUiState(
                isLoadingInitial = false,
                selectedEvent = SampleData.event,
                fullName = SampleData.user.name,
                nim = SampleData.user.nim,
                email = SampleData.user.email,
            ),
            onBack = {},
            onEventCardClick = {},
            onFullNameChange = {},
            onEmailChange = {},
            onPhoneChange = {},
            onSubmit = {},
        )
    }
}
