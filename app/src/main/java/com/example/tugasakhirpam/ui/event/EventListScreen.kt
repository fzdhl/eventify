package com.example.tugasakhirpam.ui.event

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.tugasakhirpam.ui.theme.TugasAkhirPAMTheme

@Composable
fun EventListScreen(
    initialCategory: EventCategory?,
    showAsTab: Boolean,
    onBack: () -> Unit,
    onEventClick: (Long) -> Unit,
    onBottomNavClick: (BottomNavItem) -> Unit,
    viewModel: EventListViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.start(EventListSource.ALL, initialCategory) }
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.snackbarShown()
        }
    }

    EventListContent(
        state = state,
        showAsTab = showAsTab,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onEventClick = onEventClick,
        onBottomNavClick = onBottomNavClick,
        onCategorySelect = viewModel::selectCategory,
        onToggleFavorite = viewModel::toggleFavorite,
        onRefresh = viewModel::refresh,
        onRetry = { viewModel.start(EventListSource.ALL) },
    )
}

@Composable
fun EventListContent(
    state: EventListUiState,
    showAsTab: Boolean,
    onBack: () -> Unit,
    onEventClick: (Long) -> Unit,
    onBottomNavClick: (BottomNavItem) -> Unit,
    onCategorySelect: (EventCategory?) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        containerColor = AppBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { if (showAsTab) AppBottomBar(BottomNavItem.CATEGORIES, onBottomNavClick) },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showAsTab) {
                PageHeader("Jelajahi", "Categories", "Temukan event sesuai minatmu")
            } else {
                ScreenTopBar("Semua Event", onBack, Modifier.padding(horizontal = 20.dp))
                Spacer(Modifier.height(8.dp))
            }

            CategoryFilterRow(selected = state.selectedCategory, onSelect = onCategorySelect)
            Spacer(Modifier.height(8.dp))

            EventListBody(
                isLoading = state.isLoading,
                isRefreshing = state.isRefreshing,
                errorMessage = state.errorMessage,
                events = state.filtered,
                onRefresh = onRefresh,
                onRetry = onRetry,
                onEventClick = onEventClick,
                emptyContent = {
                    MessageState(
                        icon = Icons.Outlined.EventBusy,
                        title = "Belum ada event",
                        subtitle = "Belum ada event mendatang di kategori ini."
                    )
                },
                cardTrailing = { event -> FavoriteToggle(event.isFavorite, { onToggleFavorite(event.id) }) },
                cardFooter = { event ->
                    if (event.isFull) {
                        InfoBadge("Kuota penuh", background = DangerBg, contentColor = DangerText)
                    } else {
                        InfoBadge("${event.remainingQuota} kursi tersisa")
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EventListBody(
    isLoading: Boolean,
    isRefreshing: Boolean,
    errorMessage: String?,
    events: List<Event>,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onEventClick: (Long) -> Unit,
    emptyContent: @Composable () -> Unit,
    cardTrailing: (@Composable (Event) -> Unit)? = null,
    cardFooter: (@Composable (Event) -> Unit)? = null,
    bottomPadding: Dp = 24.dp,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            isLoading -> LoadingState()

            errorMessage != null && events.isEmpty() -> ErrorState(errorMessage, onRetry)

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = bottomPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (events.isEmpty()) {
                    item(key = "empty") {
                        Box(Modifier.fillParentMaxHeight(0.75f), contentAlignment = Alignment.Center) {
                            emptyContent()
                        }
                    }
                } else {
                    items(events, key = { it.id }) { event ->
                        EventCard(
                            event = event,
                            onClick = { onEventClick(event.id) },
                            modifier = Modifier.animateItem(),
                            trailing = if (cardTrailing != null) {
                                { cardTrailing(event) }
                            } else null,
                            footer = if (cardFooter != null) {
                                { cardFooter(event) }
                            } else null,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun EventListContentPreview() {
    TugasAkhirPAMTheme {
        EventListContent(
            state = EventListUiState(isLoading = false, events = SampleData.events),
            showAsTab = true,
            onBack = {},
            onEventClick = {},
            onBottomNavClick = {},
            onCategorySelect = {},
            onToggleFavorite = {},
            onRefresh = {},
            onRetry = {},
        )
    }
}
