package com.example.tugasakhirpam.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tugasakhirpam.ui.components.PrimaryButton
import com.example.tugasakhirpam.ui.theme.BrandBlue
import com.example.tugasakhirpam.ui.theme.DangerText
import com.example.tugasakhirpam.ui.theme.TextSecondary

@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onNameChange: (String) -> Unit,
    onNimChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onTogglePasswordVisible: () -> Unit,
    onToggleConfirmVisible: () -> Unit,
    onTermsChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onLoginClick: () -> Unit,
    onMessageShown: () -> Unit,
    onConfirmationRequired: (String) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val moveDown = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            onMessageShown()
        }
    }
    LaunchedEffect(state.confirmationMessage) { state.confirmationMessage?.let(onConfirmationRequired) }

    AuthScaffold(
        eyebrow = "Akun baru",
        title = "Mulai perjalananmu",
        subtitle = "Daftar dan temukan pengalaman kampus yang lebih seru.",
        snackbarHostState = snackbarHostState,
    ) {
        AuthTextField(
            label = "Nama lengkap",
            value = state.name,
            onValueChange = onNameChange,
            placeholder = "Masukkan nama lengkap",
            leadingIcon = Icons.Outlined.Person,
            error = state.nameError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = moveDown,
        )
        AuthTextField(
            label = "NIM",
            value = state.nim,
            onValueChange = onNimChange,
            placeholder = "Contoh: 221051234",
            leadingIcon = Icons.Outlined.Badge,
            error = state.nimError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            keyboardActions = moveDown,
        )
        AuthTextField(
            label = "Email kampus",
            value = state.email,
            onValueChange = onEmailChange,
            placeholder = "nama@kampus.ac.id",
            leadingIcon = Icons.Outlined.Email,
            error = state.emailError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = moveDown,
        )
        AuthPasswordField(
            label = "Password",
            value = state.password,
            onValueChange = onPasswordChange,
            placeholder = "Minimal 8 karakter",
            leadingIcon = Icons.Outlined.Lock,
            visible = state.passwordVisible,
            onToggleVisible = onTogglePasswordVisible,
            error = state.passwordError,
            keyboardActions = moveDown,
        )
        AuthPasswordField(
            label = "Konfirmasi password",
            value = state.confirmPassword,
            onValueChange = onConfirmChange,
            placeholder = "Ulangi password",
            leadingIcon = Icons.Outlined.VerifiedUser,
            visible = state.confirmVisible,
            onToggleVisible = onToggleConfirmVisible,
            error = state.confirmError,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        )

        Column(Modifier.fillMaxWidth()) {
            Row(
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onTermsChange(!state.acceptTerms) }
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = state.acceptTerms,
                    onCheckedChange = null,
                    colors = CheckboxDefaults.colors(checkedColor = BrandBlue),
                    modifier = Modifier.padding(end = 4.dp),
                )
                Text(
                    buildAnnotatedString {
                        append("Saya menyetujui ")
                        withStyle(SpanStyle(color = BrandBlue, fontWeight = FontWeight.Bold)) {
                            append("Syarat & Ketentuan")
                        }
                        append(" Eventify.")
                    },
                    fontSize = 11.sp,
                    color = TextSecondary,
                )
            }
            if (state.termsError != null) {
                Text(state.termsError, fontSize = 11.sp, color = DangerText, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
            }
        }

        Spacer(Modifier.height(16.dp))
        PrimaryButton(
            text = "Buat akun saya",
            onClick = onSubmit,
            loading = state.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(18.dp))
        AuthSwitchRow(prompt = "Sudah punya akun?", action = "Masuk di sini", onClick = onLoginClick)
        Spacer(Modifier.height(12.dp))
        AuthSecurityNote()
    }
}
