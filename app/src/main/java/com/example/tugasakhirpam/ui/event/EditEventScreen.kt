package com.example.tugasakhirpam.ui.event

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tugasakhirpam.ui.components.EventImage
import com.example.tugasakhirpam.ui.components.ScreenTopBar
import com.example.tugasakhirpam.ui.theme.AppBackground
import com.example.tugasakhirpam.ui.theme.BorderInput
import com.example.tugasakhirpam.ui.theme.BrandBlue
import com.example.tugasakhirpam.ui.theme.BrandBlueSoft
import com.example.tugasakhirpam.ui.theme.SurfaceWhite
import com.example.tugasakhirpam.ui.theme.TextMuted
import com.example.tugasakhirpam.ui.theme.TextPrimary
import com.example.tugasakhirpam.ui.theme.TextSecondary
import com.example.tugasakhirpam.ui.theme.TugasAkhirPAMTheme

data class EditEventUiModel(
    val posterUrl: String? = null,
    val title: String = "",
    val category: String = "",
    val description: String = "",
    val date: String = "",
    val time: String = "",
    val location: String = "",
    val quota: String = "",
)

@Composable
fun EditEventScreen(
    initialState: EditEventUiModel = EditEventUiModel(),
    onBack: () -> Unit = {},
    onChangePoster: () -> Unit = {},
    onSelectCategory: () -> Unit = {},
    onSelectDate: () -> Unit = {},
    onSelectTime: () -> Unit = {},
    onSaveEvent: (EditEventUiModel) -> Unit = {},
    onCancel: () -> Unit = {},
) {
    var title by rememberSaveable { mutableStateOf(initialState.title) }
    var category by rememberSaveable { mutableStateOf(initialState.category) }
    var description by rememberSaveable { mutableStateOf(initialState.description) }
    var date by rememberSaveable { mutableStateOf(initialState.date) }
    var time by rememberSaveable { mutableStateOf(initialState.time) }
    var location by rememberSaveable { mutableStateOf(initialState.location) }
    var quota by rememberSaveable { mutableStateOf(initialState.quota) }

    Scaffold(
        containerColor = AppBackground,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            ScreenTopBar(
                title = "Edit Event",
                onBack = onBack,
            )

            Spacer(Modifier.height(8.dp))

            // Poster Card with "Ganti poster" button
            EditEventPosterCard(
                posterUrl = initialState.posterUrl,
                onChangePoster = onChangePoster,
            )

            Spacer(Modifier.height(18.dp))

            // Nama event
            EditEventTextField(
                label = "Nama event",
                value = title,
                onValueChange = { title = it },
                placeholder = "Masukkan nama event",
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
            )

            Spacer(Modifier.height(14.dp))

            // Kategori
            EditEventPickerField(
                label = "Kategori",
                value = category.ifEmpty { "Pilih kategori" },
                isPlaceholder = false,
                leadingIcon = Icons.Outlined.GridView,
                trailingIcon = Icons.Outlined.ChevronRight,
                onClick = onSelectCategory,
            )

            Spacer(Modifier.height(14.dp))

            // Deskripsi
            EditEventTextField(
                label = "Deskripsi",
                value = description,
                onValueChange = { description = it },
                placeholder = "Jelaskan eventmu...",
                singleLine = false,
                minHeightDp = 114,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                ),
            )

            Spacer(Modifier.height(14.dp))

            // Tanggal & Waktu side-by-side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EditEventPickerField(
                    label = "Tanggal",
                    value = date.ifEmpty { "dd/mm/yyyy" },
                    isPlaceholder = date.isEmpty(),
                    leadingIcon = Icons.Outlined.CalendarToday,
                    onClick = onSelectDate,
                    modifier = Modifier.weight(1.1f),
                )

                EditEventPickerField(
                    label = "Waktu",
                    value = time.ifEmpty { "09.00" },
                    isPlaceholder = time.isEmpty(),
                    leadingIcon = Icons.Outlined.Schedule,
                    onClick = onSelectTime,
                    modifier = Modifier.weight(0.9f),
                )
            }

            Spacer(Modifier.height(14.dp))

            // Lokasi
            EditEventTextField(
                label = "Lokasi",
                value = location,
                onValueChange = { location = it },
                placeholder = "Lokasi event",
                leadingIcon = Icons.Outlined.LocationOn,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
            )

            Spacer(Modifier.height(14.dp))

            // Kuota peserta
            EditEventTextField(
                label = "Kuota peserta",
                value = quota,
                onValueChange = { quota = it.filter(Char::isDigit) },
                placeholder = "Contoh: 150",
                leadingIcon = Icons.Outlined.Groups,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
            )

            Spacer(Modifier.height(22.dp))

            // Save Event Button
            Button(
                onClick = {
                    onSaveEvent(
                        EditEventUiModel(
                            posterUrl = initialState.posterUrl,
                            title = title,
                            category = category,
                            description = description,
                            date = date,
                            time = time,
                            location = location,
                            quota = quota,
                        )
                    )
                },
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
                    .fillMaxWidth()
                    .height(50.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(14.dp),
                        ambientColor = BrandBlue.copy(alpha = 0.45f),
                        spotColor = BrandBlue.copy(alpha = 0.45f),
                    )
            ) {
                Text(
                    text = "Save Event",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.height(8.dp))

            // Cancel Button
            TextButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Cancel",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EditEventPosterCard(
    posterUrl: String?,
    onChangePoster: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(185.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(BrandBlueSoft)
            .clickable(onClick = onChangePoster)
    ) {
        EventImage(
            url = posterUrl,
            contentDescription = "Poster event",
            modifier = Modifier.fillMaxSize()
        )

        Surface(
            onClick = onChangePoster,
            shape = RoundedCornerShape(10.dp),
            color = SurfaceWhite,
            shadowElevation = 3.dp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoCamera,
                    contentDescription = null,
                    tint = BrandBlue,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Ganti poster",
                    color = BrandBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun EditEventTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    singleLine: Boolean = true,
    minHeightDp: Int = 52,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                color = TextPrimary,
            ),
            placeholder = {
                Text(
                    text = placeholder,
                    fontSize = 13.sp,
                    color = TextMuted,
                )
            },
            leadingIcon = if (leadingIcon != null) {
                {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else null,
            keyboardOptions = keyboardOptions,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandBlue,
                unfocusedBorderColor = BorderInput,
                focusedContainerColor = SurfaceWhite,
                unfocusedContainerColor = SurfaceWhite,
                cursorColor = BrandBlue,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeightDp.dp)
        )
    }
}

@Composable
private fun EditEventPickerField(
    label: String,
    value: String,
    isPlaceholder: Boolean,
    leadingIcon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector? = null,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
        Spacer(Modifier.height(8.dp))
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            color = SurfaceWhite,
            border = BorderStroke(1.dp, BorderInput),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = value,
                    fontSize = 13.sp,
                    color = if (isPlaceholder) TextMuted else TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (trailingIcon != null) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 1020)
@Composable
private fun EditEventScreenPreview() {
    TugasAkhirPAMTheme {
        EditEventScreen()
    }
}
