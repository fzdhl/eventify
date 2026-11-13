package com.example.tugasakhirpam.ui.event

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tugasakhirpam.model.SampleData
import com.example.tugasakhirpam.ui.components.AppBottomBar
import com.example.tugasakhirpam.ui.components.BottomNavItem
import com.example.tugasakhirpam.ui.components.FavoriteToggle
import com.example.tugasakhirpam.ui.components.InfoBadge
import com.example.tugasakhirpam.ui.components.MessageState
import com.example.tugasakhirpam.ui.components.PageHeader
import com.example.tugasakhirpam.ui.theme.AppBackground
import com.example.tugasakhirpam.ui.theme.DangerBg
import com.example.tugasakhirpam.ui.theme.DangerText
import com.example.tugasakhirpam.ui.theme.SegmentTrack
import com.example.tugasakhirpam.ui.theme.TextSecondary
import com.example.tugasakhirpam.ui.theme.TugasAkhirPAMTheme

@Composable
fun FavoritesScreen(
    onEventClick: (Long) -> Unit,
    onExploreClick: () -> Unit,
    onBottomNavClick: (BottomNavItem) -> Unit,
    viewModel: EventListViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.start(EventListSource.FAVORITES) }
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.snackbarShown()
        }
    }

    FavoritesContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onEventClick = onEventClick,
        onExploreClick = onExploreClick,
        onBottomNavClick = onBottomNavClick,
        onToggleFavorite = viewModel::toggleFavorite,
        onRefresh = viewModel::refresh,
        onRetry = { viewModel.start(EventListSource.FAVORITES) },
    )
}

@Composable
fun FavoritesContent(
    state: EventListUiState,
    onEventClick: (Long) -> Unit,
    onExploreClick: () -> Unit,
    onBottomNavClick: (BottomNavItem) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        containerColor = AppBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { AppBottomBar(BottomNavItem.FAVORITES, onBottomNavClick) },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PageHeader("Disimpan", "Favorites", "Event yang kamu tandai untuk dilihat lagi")
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
                        icon = Icons.Outlined.FavoriteBorder,
                        title = "Belum ada favorit",
                        subtitle = "Tekan ikon hati pada event untuk menyimpannya di sini.",
                        actionText = "Jelajahi Event",
                        onAction = onExploreClick
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

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun FavoritesContentPreview() {
    TugasAkhirPAMTheme {
        FavoritesContent(
            state = EventListUiState(isLoading = false, events = SampleData.events.map { it.copy(isFavorite = true) }),
            onEventClick = {},
            onExploreClick = {},
            onBottomNavClick = {},
            onToggleFavorite = {},
            onRefresh = {},
            onRetry = {},
        )
    }
}
