package com.example.tugasakhirpam.ui.event

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tugasakhirpam.model.Category
import com.example.tugasakhirpam.ui.components.EventImage
import com.example.tugasakhirpam.ui.components.FormTextField
import com.example.tugasakhirpam.ui.components.LoadingState
import com.example.tugasakhirpam.ui.components.PickerField
import com.example.tugasakhirpam.ui.components.PrimaryButton
import com.example.tugasakhirpam.ui.components.ScreenTopBar
import com.example.tugasakhirpam.ui.components.iconForCategory
import com.example.tugasakhirpam.ui.theme.AppBackground
import com.example.tugasakhirpam.ui.theme.BrandBlue
import com.example.tugasakhirpam.ui.theme.BrandBlueSoft
import com.example.tugasakhirpam.ui.theme.DangerText
import com.example.tugasakhirpam.ui.theme.SegmentTrack
import com.example.tugasakhirpam.ui.theme.SurfaceWhite
import com.example.tugasakhirpam.ui.theme.TextPrimary
import com.example.tugasakhirpam.ui.theme.TextSecondary
import com.example.tugasakhirpam.ui.theme.TugasAkhirPAMTheme
import com.example.tugasakhirpam.util.DateFormatter
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

private enum class TimeField { START, END }

@Composable
fun EventFormScreen(
    state: EventFormUiState,
    onBack: () -> Unit,
    onPosterSelected: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onCategorySelected: (Long) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDateSelected: (String) -> Unit,
    onStartTimeSelected: (String) -> Unit,
    onEndTimeSelected: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onQuotaChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onErrorDismissed: () -> Unit,
    onSaved: (String) -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showCategorySheet by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var timeField by rememberSaveable { mutableStateOf<TimeField?>(null) }

    val posterPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            onPosterSelected(uri.toString())
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorDismissed()
        }
    }
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let(onSaved)
    }

    EventFormContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onPickPoster = {
            posterPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onTitleChange = onTitleChange,
        onCategoryClick = { showCategorySheet = true },
        onDescriptionChange = onDescriptionChange,
        onDateClick = { showDatePicker = true },
        onStartTimeClick = { timeField = TimeField.START },
        onEndTimeClick = { timeField = TimeField.END },
        onLocationChange = onLocationChange,
        onQuotaChange = onQuotaChange,
        onSubmit = onSubmit,
    )

    if (showCategorySheet) {
        CategoryPickerSheet(
            categories = state.categories,
            selectedId = state.categoryId,
            onSelect = {
                onCategorySelected(it)
                showCategorySheet = false
            },
            onDismiss = { showCategorySheet = false }
        )
    }

    if (showDatePicker) {
        EventDatePickerDialog(
            initialDate = state.eventDate,
            onConfirm = {
                onDateSelected(it)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    timeField?.let { field ->
        EventTimePickerDialog(
            title = if (field == TimeField.START) "Waktu mulai" else "Waktu selesai",
            initialTime = if (field == TimeField.START) state.startTime else state.endTime,
            onConfirm = { time ->
                if (field == TimeField.START) onStartTimeSelected(time) else onEndTimeSelected(time)
                timeField = null
            },
            onDismiss = { timeField = null }
        )
    }
}

@Composable
fun EventFormContent(
    state: EventFormUiState,
    onBack: () -> Unit,
    onPickPoster: () -> Unit,
    onTitleChange: (String) -> Unit,
    onCategoryClick: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDateClick: () -> Unit,
    onStartTimeClick: () -> Unit,
    onEndTimeClick: () -> Unit,
    onLocationChange: (String) -> Unit,
    onQuotaChange: (String) -> Unit,
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
            ScreenTopBar(if (state.isEditMode) "Edit Event" else "Add Event", onBack)
            Spacer(Modifier.height(8.dp))

            PosterPicker(posterUri = state.posterUri, onPick = onPickPoster)
            Spacer(Modifier.height(20.dp))

            FormTextField(
                label = "Nama event",
                value = state.title,
                onValueChange = onTitleChange,
                placeholder = "Masukkan nama event",
                error = state.titleError,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )
            PickerField(
                label = "Kategori",
                value = state.categories.firstOrNull { it.id == state.categoryId }?.name.orEmpty(),
                placeholder = "Pilih kategori",
                leadingIcon = Icons.Outlined.GridView,
                trailingIcon = Icons.Outlined.ChevronRight,
                onClick = {
                    focusManager.clearFocus()
                    onCategoryClick()
                },
                error = state.categoryError,
            )
            FormTextField(
                label = "Deskripsi",
                value = state.description,
                onValueChange = onDescriptionChange,
                placeholder = "Jelaskan eventmu...",
                singleLine = false,
                minHeight = 120,
                error = state.descriptionError,
                helperText = "${state.description.length}/1000 karakter",
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
            PickerField(
                label = "Tanggal",
                value = DateFormatter.input(state.eventDate),
                placeholder = "dd/mm/yyyy",
                leadingIcon = Icons.Outlined.CalendarToday,
                onClick = {
                    focusManager.clearFocus()
                    onDateClick()
                },
                error = state.dateError,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PickerField(
                    label = "Waktu mulai",
                    value = DateFormatter.time(state.startTime),
                    placeholder = "09.00",
                    leadingIcon = Icons.Outlined.Schedule,
                    onClick = {
                        focusManager.clearFocus()
                        onStartTimeClick()
                    },
                    isError = state.timeError != null,
                    modifier = Modifier.weight(1f),
                )
                PickerField(
                    label = "Waktu selesai",
                    value = DateFormatter.time(state.endTime),
                    placeholder = "15.00",
                    leadingIcon = Icons.Outlined.Schedule,
                    onClick = {
                        focusManager.clearFocus()
                        onEndTimeClick()
                    },
                    isError = state.timeError != null,
                    modifier = Modifier.weight(1f),
                )
            }
            state.timeError?.let {
                Text(
                    it,
                    color = DangerText,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .offset(y = (-10).dp)
                        .padding(start = 4.dp)
                )
            }

            FormTextField(
                label = "Lokasi",
                value = state.location,
                onValueChange = onLocationChange,
                placeholder = "Lokasi event",
                leadingIcon = Icons.Outlined.LocationOn,
                error = state.locationError,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )
            FormTextField(
                label = "Kuota peserta",
                value = state.quota,
                onValueChange = onQuotaChange,
                placeholder = "Contoh: 150",
                leadingIcon = Icons.Outlined.Groups,
                error = state.quotaError,
                helperText = if (state.isEditMode && state.registeredCount > 0) {
                    "Sudah ada ${state.registeredCount} peserta terdaftar"
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
            )

            Spacer(Modifier.height(4.dp))
            PrimaryButton(
                text = if (state.isEditMode) "Simpan Perubahan" else "Save Event",
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
private fun PosterPicker(posterUri: String?, onPick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(BrandBlueSoft)
            .clickable(onClick = onPick)
    ) {
        if (posterUri.isNullOrBlank()) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.AddPhotoAlternate, null, tint = BrandBlue, modifier = Modifier.size(36.dp))
                Spacer(Modifier.height(8.dp))
                Text("Tambahkan poster event", color = BrandBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("Opsional • rasio 16:9 disarankan", color = TextSecondary, fontSize = 11.sp)
            }
        } else {
            EventImage(url = posterUri, contentDescription = "Poster event", modifier = Modifier.fillMaxSize())
            Surface(
                onClick = onPick,
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            ) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.PhotoCamera, null, tint = BrandBlue, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Ganti poster", color = BrandBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryPickerSheet(
    categories: List<Category>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
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
            Text("Pilih kategori", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(Modifier.height(12.dp))
            if (categories.isEmpty()) {
                Text(
                    "Belum ada kategori. Buat kategori dulu di tab Categories.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            categories.forEach { category ->
                val isSelected = category.id == selectedId
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) BrandBlueSoft else SurfaceWhite)
                        .clickable {
                            scope.launch { sheetState.hide() }.invokeOnCompletion { onSelect(category.id) }
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) SurfaceWhite else BrandBlueSoft)
                    ) {
                        Icon(iconForCategory(category.iconKey), null, tint = BrandBlue, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        category.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) Icon(Icons.Outlined.Check, null, tint = BrandBlue, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventDatePickerDialog(
    initialDate: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val todayMillis = remember { DateFormatter.todayUtcMillis() }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = DateFormatter.toUtcMillis(initialDate)?.takeIf { it >= todayMillis },
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= todayMillis
            override fun isSelectableYear(year: Int): Boolean = year >= LocalDate.now().year
        },
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { datePickerState.selectedDateMillis?.let { onConfirm(DateFormatter.fromUtcMillis(it)) } },
                enabled = datePickerState.selectedDateMillis != null
            ) { Text("Pilih", color = BrandBlue) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = TextSecondary) }
        },
        colors = DatePickerDefaults.colors(containerColor = SurfaceWhite),
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = SurfaceWhite,
                selectedDayContainerColor = BrandBlue,
                todayDateBorderColor = BrandBlue,
                todayContentColor = BrandBlue,
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventTimePickerDialog(
    title: String,
    initialTime: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val initial = DateFormatter.parseTime(initialTime) ?: LocalTime.of(9, 0)
    val timePickerState = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = true,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        title = { Text(title, style = MaterialTheme.typography.titleMedium) },
        text = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = SegmentTrack,
                        selectorColor = BrandBlue,
                        timeSelectorSelectedContainerColor = BrandBlueSoft,
                        timeSelectorSelectedContentColor = BrandBlue,
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(String.format(Locale.ROOT, "%02d:%02d", timePickerState.hour, timePickerState.minute))
            }) { Text("Pilih", color = BrandBlue) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = TextSecondary) }
        }
    )
}

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun EventFormContentPreview() {
    TugasAkhirPAMTheme {
        EventFormContent(
            state = EventFormUiState(),
            onBack = {},
            onPickPoster = {},
            onTitleChange = {},
            onCategoryClick = {},
            onDescriptionChange = {},
            onDateClick = {},
            onStartTimeClick = {},
            onEndTimeClick = {},
            onLocationChange = {},
            onQuotaChange = {},
            onSubmit = {},
        )
    }
}
