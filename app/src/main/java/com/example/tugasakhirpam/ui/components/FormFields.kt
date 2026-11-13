package com.example.tugasakhirpam.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tugasakhirpam.ui.theme.BorderInput
import com.example.tugasakhirpam.ui.theme.BorderSoft
import com.example.tugasakhirpam.ui.theme.BrandBlue
import com.example.tugasakhirpam.ui.theme.DangerText
import com.example.tugasakhirpam.ui.theme.SegmentTrack
import com.example.tugasakhirpam.ui.theme.SurfaceWhite
import com.example.tugasakhirpam.ui.theme.TextMuted
import com.example.tugasakhirpam.ui.theme.TextPrimary
import com.example.tugasakhirpam.ui.theme.TextSecondary

@Composable
fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    error: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minHeight: Int = 0,
    helperText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val supporting: String? = error ?: helperText
    Column(modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = singleLine,
            isError = error != null,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = if (enabled) TextPrimary else TextSecondary),
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = TextMuted) },
            leadingIcon = if (leadingIcon != null) {
                { Icon(leadingIcon, null, modifier = Modifier.size(18.dp)) }
            } else null,
            supportingText = if (supporting != null) {
                { Text(supporting, fontSize = 11.sp, color = if (error != null) DangerText else TextMuted) }
            } else null,
            keyboardOptions = keyboardOptions,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandBlue,
                unfocusedBorderColor = BorderInput,
                disabledBorderColor = BorderSoft,
                errorBorderColor = DangerText,
                focusedContainerColor = SurfaceWhite,
                unfocusedContainerColor = SurfaceWhite,
                disabledContainerColor = SegmentTrack,
                errorContainerColor = SurfaceWhite,
                focusedLeadingIconColor = BrandBlue,
                unfocusedLeadingIconColor = TextMuted,
                disabledLeadingIconColor = TextMuted,
                errorLeadingIconColor = DangerText,
                cursorColor = BrandBlue,
                errorSupportingTextColor = DangerText,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight.dp)
        )
        Spacer(Modifier.height(if (error == null && helperText == null) 16.dp else 8.dp))
    }
}

@Composable
fun PickerField(
    label: String,
    value: String,
    placeholder: String,
    leadingIcon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
    isError: Boolean = error != null,
) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Surface(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
            color = if (enabled) SurfaceWhite else SegmentTrack,
            border = BorderStroke(1.dp, if (isError) DangerText else BorderInput),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Row(Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(leadingIcon, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    text = value.ifEmpty { placeholder },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (value.isEmpty()) TextMuted else TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (trailingIcon != null) {
                    Icon(trailingIcon, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                }
            }
        }
        if (error != null) {
            Text(error, color = DangerText, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
        }
        Spacer(Modifier.height(if (error == null) 16.dp else 12.dp))
    }
}
