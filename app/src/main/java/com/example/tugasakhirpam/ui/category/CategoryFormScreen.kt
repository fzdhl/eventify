package com.example.tugasakhirpam.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tugasakhirpam.ui.components.FormTextField
import com.example.tugasakhirpam.ui.components.LoadingState
import com.example.tugasakhirpam.ui.components.PrimaryButton
import com.example.tugasakhirpam.ui.components.ScreenTopBar
import com.example.tugasakhirpam.ui.theme.AppBackground
import com.example.tugasakhirpam.ui.theme.BrandBlue
import com.example.tugasakhirpam.ui.theme.BrandBlueSoft
import com.example.tugasakhirpam.ui.theme.TextPrimary
import com.example.tugasakhirpam.ui.theme.TextSecondary

@Composable
fun CategoryFormScreen(
    state: CategoryFormUiState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onErrorDismissed: () -> Unit,
    onSaved: (String) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorDismissed()
        }
    }
    LaunchedEffect(state.successMessage) { state.successMessage?.let(onSaved) }

    Scaffold(
        containerColor = AppBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScreenTopBar(
                title = if (state.isEditMode) "Edit Category" else "Add Category",
                onBack = onBack,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            if (state.isLoadingInitial) {
                LoadingState()
            } else {
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier
                            .size(55.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(BrandBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Apps, null, tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.height(13.dp))
                    Text(
                        if (state.isEditMode) "Ubah kategori" else "Buat kategori baru",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Kelompokkan event agar lebih mudah ditemukan.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(28.dp))

                    FormTextField(
                        label = "Nama kategori",
                        value = state.name,
                        onValueChange = onNameChange,
                        placeholder = "Contoh: Technology",
                        error = state.nameError,
                    )
                    FormTextField(
                        label = "Deskripsi",
                        value = state.description,
                        onValueChange = onDescriptionChange,
                        placeholder = "Jelaskan kategori ini secara singkat...",
                        singleLine = false,
                        minHeight = 118,
                    )

                    Text(
                        "Nama kategori harus unik dan belum pernah digunakan.",
                        fontSize = 10.sp,
                        color = BrandBlue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(9.dp))
                            .background(BrandBlueSoft)
                            .padding(10.dp)
                    )
                    Spacer(Modifier.height(18.dp))

                    PrimaryButton(
                        text = "Save Category",
                        onClick = onSubmit,
                        loading = state.isSubmitting,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    TextButton(
                        onClick = onBack,
                        enabled = !state.isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 50.dp)
                    ) {
                        Text("Cancel", fontSize = 14.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}
