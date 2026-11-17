package com.example.tugasakhirpam.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tugasakhirpam.ui.theme.BorderInput
import com.example.tugasakhirpam.ui.theme.BorderSoft
import com.example.tugasakhirpam.ui.theme.BrandBlue
import com.example.tugasakhirpam.ui.theme.BrandBlueDark
import com.example.tugasakhirpam.ui.theme.BrandBlueLight
import com.example.tugasakhirpam.ui.theme.BrandBlueSoftBorder
import com.example.tugasakhirpam.ui.theme.DangerText
import com.example.tugasakhirpam.ui.theme.SurfaceWhite
import com.example.tugasakhirpam.ui.theme.TextMuted
import com.example.tugasakhirpam.ui.theme.TextPrimary
import com.example.tugasakhirpam.ui.theme.TextSecondary

/** Kerangka bersama layar Login dan Register: latar gradasi, logo Eventify, dan kartu putih berisi form. */
@Composable
fun AuthScaffold(
    eyebrow: String,
    title: String,
    subtitle: String,
    snackbarHostState: SnackbarHostState,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFE8F0FE), SurfaceWhite)))
    ) {
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = 70.dp, y = (-50).dp)
                .size(190.dp)
                .clip(CircleShape)
                .background(BrandBlueSoftBorder.copy(alpha = 0.7f))
        )

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AuthBrand()
                Spacer(Modifier.height(24.dp))

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = SurfaceWhite,
                    border = BorderStroke(1.dp, BorderSoft),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Text(eyebrow.uppercase(), style = MaterialTheme.typography.labelSmall, color = BrandBlue)
                        Spacer(Modifier.height(6.dp))
                        Text(title, style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp), color = TextPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Spacer(Modifier.height(20.dp))
                        content()
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthBrand() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(68.dp)
                .shadow(12.dp, RoundedCornerShape(20.dp), ambientColor = BrandBlue, spotColor = BrandBlue)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(BrandBlueLight, BrandBlue, BrandBlueDark)))
        ) {
            Icon(Icons.Outlined.CalendarMonth, null, tint = Color.White, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text("Eventify", fontSize = 32.sp, fontWeight = FontWeight.Normal, color = BrandBlue)
        Spacer(Modifier.height(4.dp))
        Text("Satu tempat untuk semua momen kampusmu.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

@Composable
fun AuthTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    error: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    Column(modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            isError = error != null,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = TextMuted) },
            leadingIcon = { Icon(leadingIcon, null, modifier = Modifier.size(18.dp)) },
            trailingIcon = trailingIcon,
            supportingText = if (error != null) {
                { Text(error, fontSize = 11.sp, color = DangerText) }
            } else null,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandBlue,
                unfocusedBorderColor = BorderInput,
                errorBorderColor = DangerText,
                focusedContainerColor = SurfaceWhite,
                unfocusedContainerColor = SurfaceWhite,
                errorContainerColor = SurfaceWhite,
                focusedLeadingIconColor = BrandBlue,
                unfocusedLeadingIconColor = TextMuted,
                errorLeadingIconColor = DangerText,
                cursorColor = BrandBlue,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(if (error == null) 16.dp else 8.dp))
    }
}

@Composable
fun AuthPasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    visible: Boolean,
    onToggleVisible: () -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    AuthTextField(
        label = label,
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        modifier = modifier,
        error = error,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = keyboardActions,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggleVisible) {
                Icon(
                    if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = if (visible) "Sembunyikan password" else "Tampilkan password",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp),
                )
            }
        },
    )
}

/** Baris "Belum punya akun? Daftar sekarang" dan sebaliknya. */
@Composable
fun AuthSwitchRow(prompt: String, action: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(prompt, fontSize = 13.sp, color = TextSecondary)
        Spacer(Modifier.width(4.dp))
        Text(
            action,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BrandBlue,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onClick)
                .padding(2.dp),
        )
    }
}

@Composable
fun AuthSecurityNote(modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.VerifiedUser, null, tint = TextMuted, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text("Data akunmu tersimpan dengan aman", fontSize = 11.sp, color = TextMuted)
    }
}
