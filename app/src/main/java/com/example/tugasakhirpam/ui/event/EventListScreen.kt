package com.example.tugasakhirpam.ui.event

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tugasakhirpam.model.Event
import com.example.tugasakhirpam.model.EventCategory
import com.example.tugasakhirpam.model.SampleData
import com.example.tugasakhirpam.ui.components.AppBottomBar
import com.example.tugasakhirpam.ui.components.BottomNavItem
import com.example.tugasakhirpam.ui.components.CategoryFilterRow
import com.example.tugasakhirpam.ui.components.ErrorState
import com.example.tugasakhirpam.ui.components.EventCard
import com.example.tugasakhirpam.ui.components.FavoriteToggle
import com.example.tugasakhirpam.ui.components.InfoBadge
import com.example.tugasakhirpam.ui.components.LoadingState
import com.example.tugasakhirpam.ui.components.MessageState
import com.example.tugasakhirpam.ui.components.PageHeader
import com.example.tugasakhirpam.ui.components.ScreenTopBar
import com.example.tugasakhirpam.ui.theme.AppBackground
import com.example.tugasakhirpam.ui.theme.DangerBg
import com.example.tugasakhirpam.ui.theme.DangerText
import com.example.tugasakhirpam.ui.theme.SegmentTrack
import com.example.tugasakhirpam.ui.theme.TextSecondary
import com.example.tugasakhirpam.ui.theme.TugasAkhirPAMTheme

@Composable
fun EventListScreen(
    state: EventListUiState,
    showAsTab: Boolean,
    onBack: () -> Unit,
    onEventClick: (Long) -> Unit,
    onBottomNavClick: (BottomNavItem) -> Unit,
    onCategorySelect: (EventCategory?) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onSnackbarDismissed: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            onSnackbarDismissed()
        }
    }

    Scaffold(
        containerColor = AppBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showAsTab) AppBottomBar(BottomNavItem.CATEGORIES, onBottomNavClick)
        },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showAsTab) {
                PageHeader("Kategori", "Semua Event", "Temukan event sesuai minatmu")
            } else {
                ScreenTopBar(
                    state.selectedCategory?.label ?: "Semua Event",
                    onBack,
                    Modifier.padding(horizontal = 20.dp)
                )
            }
            CategoryFilterRow(
                selected = state.selectedCategory,
                onSelect = onCategorySelect,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            EventListBody(
                isLoading = state.isLoading,
                isRefreshing = state.isRefreshing,
                errorMessage = state.errorMessage,
                events = state.events,
                onRefresh = onRefresh,
                onRetry = onRetry,
                onEventClick = onEventClick,
                emptyContent = {
                    MessageState(
                        icon = Icons.Outlined.EventBusy,
                        title = "Belum ada event",
                        subtitle = "Coba pilih kategori lain atau tarik ke bawah untuk memuat ulang."
                    )
                },
                cardTrailing = { event -> FavoriteToggle(event.isFavorite, { onToggleFavorite(event.id) }) },
                cardFooter = { event ->
                    when {
                        event.isPast -> InfoBadge("Selesai", background = SegmentTrack, contentColor = TextSecondary)
                        event.isFull -> InfoBadge("Kuota penuh", background = DangerBg, contentColor = DangerText)
                        else -> InfoBadge("${event.remainingQuota} kursi tersisa")
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListBody(
    isLoading: Boolean,
    isRefreshing: Boolean,
    errorMessage: String?,
    events: List<Event>,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onEventClick: (Long) -> Unit,
    emptyContent: @Composable () -> Unit,
    cardTrailing: @Composable (Event) -> Unit = {},
    cardFooter: @Composable (Event) -> Unit = {},
) {
    when {
        isLoading -> LoadingState()
        errorMessage != null && events.isEmpty() -> ErrorState(errorMessage, onRetry)
        else -> PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            if (events.isEmpty()) {
                emptyContent()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
                ) {
                    items(events, key = { it.id }) { event ->
                        EventCard(
                            event = event,
                            onClick = { onEventClick(event.id) },
                            trailing = { cardTrailing(event) },
                            footer = { cardFooter(event) },
                            modifier = Modifier
                                .animateItem()
                                .padding(vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun EventListScreenPreview() {
    TugasAkhirPAMTheme {
        EventListScreen(
            state = EventListUiState(isLoading = false, events = SampleData.events),
            showAsTab = true,
            onBack = {},
            onEventClick = {},
            onBottomNavClick = {},
            onCategorySelect = {},
            onToggleFavorite = {},
            onRefresh = {},
            onRetry = {},
            onSnackbarDismissed = {},
        )
    }
}
