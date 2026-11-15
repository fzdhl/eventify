package com.example.tugasakhirpam.ui.review

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.RateReview
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tugasakhirpam.model.Event
import com.example.tugasakhirpam.model.Review
import com.example.tugasakhirpam.model.SampleData
import com.example.tugasakhirpam.ui.components.ConfirmDialog
import com.example.tugasakhirpam.ui.components.ErrorState
import com.example.tugasakhirpam.ui.components.InfoBadge
import com.example.tugasakhirpam.ui.components.LoadingState
import com.example.tugasakhirpam.ui.components.MessageState
import com.example.tugasakhirpam.ui.components.PrimaryButton
import com.example.tugasakhirpam.ui.components.RatingStars
import com.example.tugasakhirpam.ui.components.ScreenTopBar
import com.example.tugasakhirpam.ui.components.UserAvatar
import com.example.tugasakhirpam.ui.theme.AppBackground
import com.example.tugasakhirpam.ui.theme.BorderSoft
import com.example.tugasakhirpam.ui.theme.BrandBlue
import com.example.tugasakhirpam.ui.theme.BrandBlueLight
import com.example.tugasakhirpam.ui.theme.DangerText
import com.example.tugasakhirpam.ui.theme.SurfaceWhite
import com.example.tugasakhirpam.ui.theme.TextMuted
import com.example.tugasakhirpam.ui.theme.TextPrimary
import com.example.tugasakhirpam.ui.theme.TextSecondary
import com.example.tugasakhirpam.ui.theme.TugasAkhirPAMTheme
import com.example.tugasakhirpam.util.DateFormatter
import java.time.OffsetDateTime

@Composable
fun ReviewListScreen(
    state: ReviewListUiState,
    resultMessage: String?,
    onResultConsumed: () -> Unit,
    onBack: () -> Unit,
    onRatingSelect: (Int?) -> Unit,
    onWriteReview: () -> Unit,
    onEditReview: (Long) -> Unit,
    onDeleteReview: (Long) -> Unit,
    onSnackbarDismissed: () -> Unit,
    onRetry: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var reviewToDelete by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(resultMessage) {
        resultMessage?.let {
            snackbarHostState.showSnackbar(it)
            onResultConsumed()
        }
    }
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            onSnackbarDismissed()
        }
    }

    ReviewListContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onRetry = onRetry,
        onRatingSelect = onRatingSelect,
        onWriteReview = onWriteReview,
        onEditReview = onEditReview,
        onDeleteReview = { reviewToDelete = it },
    )

    reviewToDelete?.let { id ->
        ConfirmDialog(
            title = "Hapus ulasan?",
            message = "Ulasanmu akan dihapus permanen dan rating event akan dihitung ulang.",
            confirmText = "Hapus",
            onConfirm = {
                reviewToDelete = null
                onDeleteReview(id)
            },
            onDismiss = { reviewToDelete = null }
        )
    }
}

@Composable
fun ReviewListContent(
    state: ReviewListUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onRatingSelect: (Int?) -> Unit,
    onWriteReview: () -> Unit,
    onEditReview: (Long) -> Unit,
    onDeleteReview: (Long) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val event = state.event

    Scaffold(
        containerColor = AppBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!state.isLoading && event != null) {
                ReviewBottomBar(
                    action = state.action,
                    isProcessing = state.isProcessing,
                    onWriteReview = onWriteReview,
                    onEditMyReview = { state.myReview?.let { onEditReview(it.id) } },
                )
            }
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScreenTopBar("Reviews", onBack, Modifier.padding(horizontal = 20.dp))

            when {
                state.isLoading -> LoadingState()
                event == null -> ErrorState(state.errorMessage ?: "Event tidak ditemukan", onRetry)
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "summary") {
                        RatingSummaryCard(event = event, average = state.average, count = state.reviews.size)
                    }
                    item(key = "filter") {
                        RatingFilterRow(selected = state.selectedRating, onSelect = onRatingSelect)
                    }

                    if (state.filtered.isEmpty()) {
                        item(key = "empty") {
                            MessageState(
                                icon = Icons.Outlined.RateReview,
                                title = if (state.reviews.isEmpty()) "Belum ada ulasan"
                                else "Belum ada ulasan ${state.selectedRating} bintang",
                                subtitle = if (state.reviews.isEmpty()) "Ulasan dari peserta akan muncul di sini setelah event selesai."
                                else "Coba pilih filter rating lainnya.",
                            )
                        }
                    }

                    items(state.filtered, key = { it.id }) { review ->
                        ReviewCard(
                            review = review,
                            isMine = review.userId == state.currentUserId,
                            enabled = !state.isProcessing,
                            onEdit = { onEditReview(review.id) },
                            onDelete = { onDeleteReview(review.id) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingSummaryCard(event: Event, average: Double, count: Int) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(BrandBlue, BrandBlueLight)))
            .padding(vertical = 20.dp, horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(110.dp)
            ) {
                Text(
                    if (count == 0) "–" else DateFormatter.rating(average),
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 44.sp
                )
                RatingStars(rating = average, starSize = 14.dp, emptyColor = Color.White.copy(alpha = 0.35f))
                Spacer(Modifier.height(6.dp))
                Text("$count ulasan", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
            }
            Box(
                Modifier
                    .padding(horizontal = 12.dp)
                    .width(1.dp)
                    .height(76.dp)
                    .background(Color.White.copy(alpha = 0.3f))
            )
            Column(Modifier.weight(1f)) {
                Text(
                    event.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text("Pengalaman peserta event", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun RatingFilterRow(selected: Int?, onSelect: (Int?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item { RatingChip(label = "Semua", showStar = false, selected = selected == null) { onSelect(null) } }
        items(listOf(5, 4, 3, 2, 1)) { star ->
            RatingChip(label = "$star", showStar = true, selected = selected == star) { onSelect(star) }
        }
    }
}

@Composable
private fun RatingChip(label: String, showStar: Boolean, selected: Boolean, onClick: () -> Unit) {
    val contentColor = if (selected) Color.White else TextSecondary
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) BrandBlue else SurfaceWhite,
        border = if (selected) null else BorderStroke(1.dp, BorderSoft),
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = contentColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            if (showStar) {
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Filled.Star, null, tint = contentColor, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
private fun ReviewCard(
    review: Review,
    isMine: Boolean,
    enabled: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSoft),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(review.initials, size = 40.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            review.userName,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isMine) {
                            Spacer(Modifier.width(6.dp))
                            InfoBadge("Ulasanmu")
                        }
                    }
                    Spacer(Modifier.height(3.dp))
                    RatingStars(rating = review.rating.toDouble(), starSize = 13.dp)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    DateFormatter.relative(review.updatedAt ?: review.createdAt) +
                        if (review.isEdited) " · diedit" else "",
                    fontSize = 10.sp,
                    color = TextMuted,
                    textAlign = TextAlign.End
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(review.comment, fontSize = 13.sp, color = TextSecondary, lineHeight = 19.sp)

            if (isMine) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SmallAction(Icons.Outlined.Edit, "Edit", BrandBlue, enabled, onEdit)
                    SmallAction(Icons.Outlined.DeleteOutline, "Delete", DangerText, enabled, onDelete)
                }
            }
        }
    }
}

@Composable
private fun SmallAction(icon: ImageVector, label: String, color: Color, enabled: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ReviewBottomBar(
    action: ReviewAction,
    isProcessing: Boolean,
    onWriteReview: () -> Unit,
    onEditMyReview: () -> Unit,
) {
    val hint = when (action) {
        ReviewAction.EVENT_NOT_FINISHED -> "Ulasan bisa ditulis setelah event selesai."
        ReviewAction.NOT_ATTENDED -> "Hanya peserta yang hadir (status Confirmed) yang bisa menulis ulasan."
        ReviewAction.OWNER -> "Kamu penyelenggara event ini, jadi tidak bisa mengulas."
        else -> null
    }

    Surface(color = SurfaceWhite, shadowElevation = 8.dp) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            if (hint != null) {
                Text(
                    hint,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                if (action != ReviewAction.OWNER) Spacer(Modifier.height(8.dp))
            }
            when (action) {
                ReviewAction.WRITE -> PrimaryButton(
                    "Write Review", onWriteReview, Modifier.fillMaxWidth(),
                    loading = isProcessing, icon = Icons.Outlined.Add
                )
                ReviewAction.EDIT -> PrimaryButton(
                    "Edit Ulasanmu", onEditMyReview, Modifier.fillMaxWidth(),
                    loading = isProcessing, icon = Icons.Outlined.Edit
                )
                ReviewAction.EVENT_NOT_FINISHED, ReviewAction.NOT_ATTENDED -> PrimaryButton(
                    "Write Review", {}, Modifier.fillMaxWidth(),
                    enabled = false, icon = Icons.Outlined.Add
                )
                ReviewAction.OWNER -> Unit
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ReviewListContentPreview() {
    val now = OffsetDateTime.now().minusDays(2).toString()
    TugasAkhirPAMTheme {
        ReviewListContent(
            state = ReviewListUiState(
                isLoading = false,
                event = SampleData.event,
                reviews = listOf(
                    Review(1, 1, "u-201", "Nadia Putri", 5, "Materinya relevan dan pembicaranya sangat inspiratif.", now),
                    Review(2, 1, "u-202", "Rizky Ananda", 4, "Event terorganisir dengan baik, antrean cukup panjang.", now),
                    Review(3, 1, SampleData.user.id, SampleData.user.name, 5, "Banyak insight baru tentang AI.", now),
                ),
                action = ReviewAction.EDIT,
                currentUserId = SampleData.user.id,
            ),
            onBack = {},
            onRetry = {},
            onRatingSelect = {},
            onWriteReview = {},
            onEditReview = {},
            onDeleteReview = {},
        )
    }
}
