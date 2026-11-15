package com.example.tugasakhirpam.ui.registration

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Event
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
import com.example.tugasakhirpam.model.Event
import com.example.tugasakhirpam.model.SampleData
import com.example.tugasakhirpam.ui.components.CategoryLabel
import com.example.tugasakhirpam.ui.components.EventImage
import com.example.tugasakhirpam.ui.components.FormTextField
import com.example.tugasakhirpam.ui.components.LoadingState
import com.example.tugasakhirpam.ui.components.PickerField
import com.example.tugasakhirpam.ui.components.PrimaryButton
import com.example.tugasakhirpam.ui.components.ScreenTopBar
import com.example.tugasakhirpam.ui.theme.AppBackground
import com.example.tugasakhirpam.ui.theme.BorderSoft
import com.example.tugasakhirpam.ui.theme.BrandBlue
import com.example.tugasakhirpam.ui.theme.BrandBlueSoft
import com.example.tugasakhirpam.ui.theme.SurfaceWhite
import com.example.tugasakhirpam.ui.theme.TextPrimary
import com.example.tugasakhirpam.ui.theme.TextSecondary
import com.example.tugasakhirpam.ui.theme.TugasAkhirPAMTheme
import com.example.tugasakhirpam.util.DateFormatter
import kotlinx.coroutines.launch

@Composable
fun RegistrationFormScreen(
    state: RegistrationFormUiState,
    onBack: () -> Unit,
    onEventSelected: (EventOption) -> Unit,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onErrorDismissed: () -> Unit,
    onSaved: (String) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showEventSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorDismissed()
        }
    }
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let(onSaved)
    }

    RegistrationFormContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onEventClick = { showEventSheet = true },
        onFullNameChange = onFullNameChange,
        onEmailChange = onEmailChange,
        onPhoneChange = onPhoneChange,
        onSubmit = onSubmit,
    )

    if (showEventSheet) {
        EventPickerSheet(
            options = state.options,
            selected = state.selectedEvent,
            onSelect = {
                onEventSelected(it)
                showEventSheet = false
            },
            onDismiss = { showEventSheet = false }
        )
    }
}

@Composable
fun RegistrationFormContent(
    state: RegistrationFormUiState,
    onBack: () -> Unit,
    onEventClick: () -> Unit,
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
            ScreenTopBar(if (state.isEditMode) "Edit Pendaftaran" else "Event Registration", onBack)
            Spacer(Modifier.height(8.dp))

            val selectedEvent = state.selectedEvent
            if (state.isEditMode && selectedEvent != null) {
                EventSummaryCard(selectedEvent)
            } else {
                PickerField(
                    label = "Pilih event",
                    value = selectedEvent?.title.orEmpty(),
                    placeholder = "Tekan untuk memilih event",
                    leadingIcon = Icons.Outlined.Event,
                    trailingIcon = Icons.Outlined.ChevronRight,
                    onClick = {
                        focusManager.clearFocus()
                        onEventClick()
                    },
                    error = state.eventError,
                )
            }
            Spacer(Modifier.height(16.dp))

            FormTextField(
                label = "Nama lengkap",
                value = state.fullName,
                onValueChange = onFullNameChange,
                placeholder = "Nama lengkap sesuai KTM",
                leadingIcon = Icons.Outlined.Person,
                error = state.fullNameError,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )
            FormTextField(
                label = "NIM (Terkunci dari akun)",
                value = state.nim,
                onValueChange = {},
                placeholder = "NIM",
                leadingIcon = Icons.Outlined.Badge,
                helperText = "NIM diambil otomatis dari akun loginmu."
            )
            FormTextField(
                label = "Email",
                value = state.email,
                onValueChange = onEmailChange,
                placeholder = "nama@univ.ac.id",
                leadingIcon = Icons.Outlined.Email,
                error = state.emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )
            FormTextField(
                label = "Nomor WhatsApp",
                value = state.phone,
                onValueChange = onPhoneChange,
                placeholder = "081234567890",
                leadingIcon = Icons.Outlined.Phone,
                error = state.phoneError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done)
            )

            Spacer(Modifier.height(4.dp))
            PrimaryButton(
                text = if (state.isEditMode) "Simpan Perubahan" else "Register Now",
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
private fun EventSummaryCard(event: Event) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSoft),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            EventImage(
                url = event.imageUrl,
                modifier = Modifier
                    .size(width = 64.dp, height = 56.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                CategoryLabel("Mendaftar untuk")
                Spacer(Modifier.height(3.dp))
                Text(
                    event.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${DateFormatter.short(event.eventDate)} · ${event.location}",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventPickerSheet(
    options: List<EventOption>,
    selected: Event?,
    onSelect: (EventOption) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = SurfaceWhite) {
        Column(
            Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Text("Pilih event yang ingin diikuti", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(Modifier.height(12.dp))
            if (options.isEmpty()) {
                Text("Semua event terbuka sudah kamu ikuti.", fontSize = 12.sp, color = TextSecondary)
            } else {
                options.forEach { option ->
                    val event = option.event
                    val isSelected = event.id == selected?.id
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) BrandBlueSoft else SurfaceWhite)
                            .clickable {
                                scope.launch { sheetState.hide() }.invokeOnCompletion { onSelect(option) }
                            }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EventImage(
                            url = event.imageUrl,
                            modifier = Modifier
                                .size(width = 54.dp, height = 46.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                event.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                if (option.disabledReason != null) "${DateFormatter.short(event.eventDate)} · ${option.disabledReason}"
                                else "${DateFormatter.short(event.eventDate)} · Sisa kuota ${event.remainingQuota}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        if (isSelected) Icon(Icons.Outlined.Check, null, tint = BrandBlue, modifier = Modifier.size(18.dp))
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
                fullName = SampleData.user.name,
                nim = SampleData.user.nim,
                email = SampleData.user.email,
                phone = SampleData.user.phone,
                selectedEvent = SampleData.event
            ),
            onBack = {},
            onEventClick = {},
            onFullNameChange = {},
            onEmailChange = {},
            onPhoneChange = {},
            onSubmit = {},
        )
    }
}
