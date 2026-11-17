package com.example.tugasakhirpam.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tugasakhirpam.ui.components.ConfirmDialog
import com.example.tugasakhirpam.ui.components.PrimaryButton
import com.example.tugasakhirpam.ui.components.ScreenTopBar
import com.example.tugasakhirpam.ui.components.SecondaryButton
import com.example.tugasakhirpam.ui.theme.AppBackground
import com.example.tugasakhirpam.ui.theme.BorderSoft
import com.example.tugasakhirpam.ui.theme.BrandBlue
import com.example.tugasakhirpam.ui.theme.BrandBlueSoft
import com.example.tugasakhirpam.ui.theme.DangerBg
import com.example.tugasakhirpam.ui.theme.DangerText
import com.example.tugasakhirpam.ui.theme.SuccessBg
import com.example.tugasakhirpam.ui.theme.SuccessText
import com.example.tugasakhirpam.ui.theme.SurfaceWhite
import com.example.tugasakhirpam.ui.theme.TextMuted
import com.example.tugasakhirpam.ui.theme.TextPrimary
import com.example.tugasakhirpam.ui.theme.TextSecondary
import com.example.tugasakhirpam.util.DateFormatter

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    resultMessage: String?,
    onResultConsumed: () -> Unit,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onLogout: () -> Unit,
    onErrorShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val user = state.user

    LaunchedEffect(resultMessage) {
        resultMessage?.let {
            snackbarHostState.showSnackbar(it)
            onResultConsumed()
        }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorShown()
        }
    }

    if (showLogoutDialog) {
        ConfirmDialog(
            title = "Keluar dari akun?",
            message = "Kamu perlu masuk lagi untuk melihat event dan pendaftaranmu.",
            confirmText = "Keluar",
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            },
            onDismiss = { showLogoutDialog = false },
        )
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
            ScreenTopBar(title = "Profile", onBack = onBack, modifier = Modifier.padding(horizontal = 20.dp))

            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ProfileAvatar(user = user, size = 88.dp)
                Spacer(Modifier.height(12.dp))
                ActiveBadge()
                Spacer(Modifier.height(10.dp))
                Text(
                    user?.name.orEmpty().ifBlank { "Pengguna Eventify" },
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                    color = TextPrimary,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    studentLabel(user?.studyProgram.orEmpty()),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(24.dp))

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "INFORMASI PRIBADI",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandBlue,
                        modifier = Modifier.weight(1f),
                    )
                    val updatedAt = user?.updatedAt
                    if (updatedAt != null) {
                        Text("Diperbarui ${DateFormatter.relative(updatedAt)}", fontSize = 10.sp, color = TextMuted)
                    }
                }
                Spacer(Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = SurfaceWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSoft),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        InfoRow(Icons.Outlined.Email, "Email", user?.email.orEmpty())
                        HorizontalDivider(color = BorderSoft)
                        InfoRow(Icons.Outlined.Badge, "NIM", user?.nim.orEmpty())
                        HorizontalDivider(color = BorderSoft)
                        InfoRow(Icons.Outlined.Phone, "Nomor HP", user?.phone.orEmpty())
                        HorizontalDivider(color = BorderSoft)
                        InfoRow(Icons.Outlined.Apartment, "Program studi", user?.studyProgram.orEmpty())
                    }
                }

                Spacer(Modifier.height(24.dp))
                PrimaryButton(
                    text = "Edit Profile",
                    onClick = onEditClick,
                    icon = Icons.Outlined.Edit,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                SecondaryButton(
                    text = if (state.isLoggingOut) "Keluar..." else "Logout",
                    onClick = { showLogoutDialog = true },
                    enabled = !state.isLoggingOut,
                    icon = Icons.AutoMirrored.Outlined.Logout,
                    containerColor = DangerBg,
                    contentColor = DangerText,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private fun studentLabel(studyProgram: String) =
    if (studyProgram.isBlank()) "Mahasiswa" else "Mahasiswa $studyProgram"

@Composable
private fun ActiveBadge() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(SuccessBg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(SuccessText))
        Text("Profil aktif", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SuccessText)
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(BrandBlueSoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = BrandBlue, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = TextSecondary)
            Text(
                value.ifBlank { "-" },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (value.isBlank()) TextMuted else TextPrimary,
            )
        }
    }
}
