package com.example.tugasakhirpam.ui.event

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.tugasakhirpam.model.SampleData
import com.example.tugasakhirpam.ui.components.AppBottomBar
import com.example.tugasakhirpam.ui.components.BottomNavItem
import com.example.tugasakhirpam.ui.components.InfoBadge
import com.example.tugasakhirpam.ui.components.MessageState
import com.example.tugasakhirpam.ui.components.PageHeader
import com.example.tugasakhirpam.ui.theme.AppBackground
import com.example.tugasakhirpam.ui.theme.SegmentTrack
import com.example.tugasakhirpam.ui.theme.TextSecondary
import com.example.tugasakhirpam.ui.theme.TugasAkhirPAMTheme

@Composable
fun MyEventsScreen(
    state: EventListUiState,
    resultMessage: String?,
    onResultConsumed: () -> Unit,
    onEventClick: (Long) -> Unit,
    onCreateEvent: () -> Unit,
    onBottomNavClick: (BottomNavItem) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(resultMessage) {
        resultMessage?.let {
            snackbarHostState.showSnackbar(it)
            onResultConsumed()
        }
    }

    MyEventsContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onEventClick = onEventClick,
        onCreateEvent = onCreateEvent,
        onBottomNavClick = onBottomNavClick,
        onRefresh = onRefresh,
        onRetry = onRetry,
    )
}

@Composable
fun MyEventsContent(
    state: EventListUiState,
    onEventClick: (Long) -> Unit,
    onCreateEvent: () -> Unit,
    onBottomNavClick: (BottomNavItem) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        containerColor = AppBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { AppBottomBar(BottomNavItem.MY_EVENTS, onBottomNavClick) },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PageHeader("Penyelenggara", "My Events", "Kelola event dan pendaftaran pesertamu")
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
                        title = "Belum membuat event",
                        subtitle = "Buat event pertamamu dan kelola peserta dengan mudah.",
                        actionText = "Buat Event",
                        onAction = onCreateEvent
                    )
                },
                cardTrailing = { event ->
                    InfoBadge(
                        text = "${event.registeredCount}/${event.quota}",
                    )
                },
                cardFooter = { event ->
                    if (event.isPast) {
                        InfoBadge("Selesai", background = SegmentTrack, contentColor = TextSecondary)
                    } else {
                        InfoBadge("Event kamu")
                    }
                },
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun MyEventsContentPreview() {
    TugasAkhirPAMTheme {
        MyEventsContent(
            state = EventListUiState(
                isLoading = false,
                events = SampleData.events.map { it.copy(createdBy = SampleData.user.id) }
            ),
            onEventClick = {},
            onCreateEvent = {},
            onBottomNavClick = {},
            onRefresh = {},
            onRetry = {},
        )
    }
}
