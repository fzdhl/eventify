package com.example.tugasakhirpam.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tugasakhirpam.model.User
import com.example.tugasakhirpam.ui.components.FormTextField
import com.example.tugasakhirpam.ui.components.PrimaryButton
import com.example.tugasakhirpam.ui.components.ScreenTopBar
import com.example.tugasakhirpam.ui.theme.AppBackground
import com.example.tugasakhirpam.ui.theme.BrandBlue
import com.example.tugasakhirpam.ui.theme.TextMuted
import com.example.tugasakhirpam.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun EditProfileScreen(
    state: EditProfileUiState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onNimChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onStudyProgramChange: (String) -> Unit,
    onAvatarPicked: (uri: String, bytes: ByteArray, mimeType: String?) -> Unit,
    onAvatarReadFailed: () -> Unit,
    onSubmit: () -> Unit,
    onErrorDismissed: () -> Unit,
    onSaved: (String) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorDismissed()
        }
    }
    LaunchedEffect(state.successMessage) { state.successMessage?.let(onSaved) }

    // Photo Picker sistem: tidak butuh izin storage.
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val resolver = context.contentResolver
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        val mime = resolver.getType(uri)
                        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                        mime to bytes
                    }.getOrNull()
                }
                val bytes = result?.second
                if (bytes == null) onAvatarReadFailed() else onAvatarPicked(uri.toString(), bytes, result.first)
            }
        }
    }
    val openPicker = {
        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    Scaffold(
        containerColor = AppBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScreenTopBar(title = "Edit Profile", onBack = onBack, modifier = Modifier.padding(horizontal = 20.dp))

            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(Modifier.size(96.dp), contentAlignment = Alignment.TopStart) {
                    ProfileAvatar(
                        user = User(
                            id = "",
                            name = state.name,
                            nim = state.nim,
                            email = state.email,
                            phone = state.phone,
                            avatarUrl = state.currentAvatarUrl,
                        ),
                        size = 88.dp,
                        previewUri = state.pickedAvatarUri,
                    )
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(BrandBlue)
                            .border(2.dp, Color.White, CircleShape)
                            .clickable(enabled = !state.isSubmitting) { openPicker() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.PhotoCamera, "Ganti foto profil", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("Ketuk ikon untuk ganti foto", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                Spacer(Modifier.height(2.dp))
                Text("JPG atau PNG, maksimal 5 MB", fontSize = 10.sp, color = TextMuted)
                Spacer(Modifier.height(24.dp))

                FormTextField(
                    label = "Nama lengkap",
                    value = state.name,
                    onValueChange = onNameChange,
                    placeholder = "Masukkan nama lengkap",
                    leadingIcon = Icons.Outlined.Person,
                    error = state.nameError,
                )
                FormTextField(
                    label = "NIM",
                    value = state.nim,
                    onValueChange = onNimChange,
                    placeholder = "Contoh: 221051234",
                    leadingIcon = Icons.Outlined.Badge,
                    error = state.nimError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                // Email adalah identitas login di Supabase Auth. Mengubahnya butuh alur verifikasi
                // email baru, jadi di sini hanya ditampilkan.
                FormTextField(
                    label = "Email kampus",
                    value = state.email,
                    onValueChange = {},
                    placeholder = "nama@kampus.ac.id",
                    leadingIcon = Icons.Outlined.Email,
                    enabled = false,
                    helperText = "Email dipakai untuk masuk dan tidak dapat diubah.",
                )
                FormTextField(
                    label = "Nomor HP",
                    value = state.phone,
                    onValueChange = onPhoneChange,
                    placeholder = "+62 812 3456 7890",
                    leadingIcon = Icons.Outlined.Phone,
                    error = state.phoneError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                )
                FormTextField(
                    label = "Program studi",
                    value = state.studyProgram,
                    onValueChange = onStudyProgramChange,
                    placeholder = "Contoh: Sistem Informasi",
                    leadingIcon = Icons.Outlined.Apartment,
                )

                Spacer(Modifier.height(8.dp))
                PrimaryButton(
                    text = "Simpan Perubahan",
                    onClick = onSubmit,
                    loading = state.isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(
                    onClick = onBack,
                    enabled = !state.isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 50.dp),
                ) {
                    Text("Batal", fontSize = 14.sp, color = TextSecondary)
                }
            }
        }
    }
}
