package com.example.tugasakhirpam.ui.review

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
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
import com.example.tugasakhirpam.ui.components.PrimaryButton
import com.example.tugasakhirpam.ui.components.RatingInput
import com.example.tugasakhirpam.ui.components.ScreenTopBar
import com.example.tugasakhirpam.ui.components.ratingLabel
import com.example.tugasakhirpam.ui.theme.AppBackground
import com.example.tugasakhirpam.ui.theme.BorderSoft
import com.example.tugasakhirpam.ui.theme.BrandBlue
import com.example.tugasakhirpam.ui.theme.DangerText
import com.example.tugasakhirpam.ui.theme.SurfaceWhite
import com.example.tugasakhirpam.ui.theme.TextPrimary
import com.example.tugasakhirpam.ui.theme.TextSecondary
import com.example.tugasakhirpam.ui.theme.TugasAkhirPAMTheme
import com.example.tugasakhirpam.util.DateFormatter

@Composable
fun ReviewFormScreen(
    state: ReviewFormUiState,
    onBack: () -> Unit,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
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
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let(onSaved)
    }

    ReviewFormContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onRatingChange = onRatingChange,
        onCommentChange = onCommentChange,
        onSubmit = onSubmit,
    )
}

@Composable
fun ReviewFormContent(
    state: ReviewFormUiState,
    onBack: () -> Unit,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
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
            ScreenTopBar(if (state.isEditMode) "Edit Review" else "Write Review", onBack)
            Spacer(Modifier.height(8.dp))

            state.event?.let { ReviewEventCard(it) }
            Spacer(Modifier.height(28.dp))

            Text(
                "Bagaimana pengalamanmu?",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            RatingInput(
                rating = state.rating,
                onRatingChange = {
                    focusManager.clearFocus()
                    onRatingChange(it)
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                ratingLabel(state.rating),
                fontSize = 13.sp,
                fontWeight = if (state.rating > 0) FontWeight.SemiBold else FontWeight.Normal,
                color = if (state.rating > 0) BrandBlue else TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            state.ratingError?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, color = DangerText, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
            Spacer(Modifier.height(28.dp))

            FormTextField(
                label = "Ulasan",
                value = state.comment,
                onValueChange = onCommentChange,
                placeholder = "Ceritakan pengalamanmu: materi, pembicara, atau panitia...",
                singleLine = false,
                minHeight = 140,
                error = state.commentError,
                helperText = "${state.comment.length}/$MAX_REVIEW_LENGTH karakter",
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            Spacer(Modifier.height(4.dp))
            PrimaryButton(
                text = if (state.isEditMode) "Simpan Perubahan" else "Kirim Ulasan",
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
private fun ReviewEventCard(event: Event) {
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
                CategoryLabel("Mengulas event")
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

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun ReviewFormContentPreview() {
    TugasAkhirPAMTheme {
        ReviewFormContent(
            state = ReviewFormUiState(isLoadingInitial = false, event = SampleData.event, rating = 4),
            onBack = {},
            onRatingChange = {},
            onCommentChange = {},
            onSubmit = {},
        )
    }
}
