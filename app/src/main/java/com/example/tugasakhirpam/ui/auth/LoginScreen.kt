package com.example.tugasakhirpam.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tugasakhirpam.ui.components.PrimaryButton
import com.example.tugasakhirpam.ui.theme.BrandBlue
import com.example.tugasakhirpam.ui.theme.TextSecondary

@Composable
fun LoginScreen(
    state: LoginUiState,
    resultMessage: String?,
    onResultConsumed: () -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisible: () -> Unit,
    onRememberChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onForgotPassword: () -> Unit,
    onRegisterClick: () -> Unit,
    onMessageShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            onMessageShown()
        }
    }
    LaunchedEffect(resultMessage) {
        resultMessage?.let {
            snackbarHostState.showSnackbar(it)
            onResultConsumed()
        }
    }

    AuthScaffold(
        eyebrow = "Masuk akun",
        title = "Selamat datang kembali",
        subtitle = "Masuk untuk melihat event dan pendaftaranmu.",
        snackbarHostState = snackbarHostState,
    ) {
        AuthTextField(
            label = "Email kampus",
            value = state.email,
            onValueChange = onEmailChange,
            placeholder = "nama@kampus.ac.id",
            leadingIcon = Icons.Outlined.Email,
            error = state.emailError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
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
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                onSubmit()
            }),
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onRememberChange(!state.rememberMe) }
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = state.rememberMe,
                    onCheckedChange = null,
                    colors = CheckboxDefaults.colors(checkedColor = BrandBlue),
                    modifier = Modifier.padding(end = 4.dp),
                )
                Text("Ingat saya", fontSize = 12.sp, color = TextSecondary)
            }
            Text(
                "Lupa password?",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = BrandBlue,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onForgotPassword)
                    .padding(4.dp),
            )
        }

        Spacer(Modifier.height(16.dp))
        PrimaryButton(
            text = "Masuk ke Eventify",
            onClick = onSubmit,
            loading = state.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(18.dp))
        AuthSwitchRow(prompt = "Belum punya akun?", action = "Daftar sekarang", onClick = onRegisterClick)
        Spacer(Modifier.height(12.dp))
        AuthSecurityNote()
    }
}
