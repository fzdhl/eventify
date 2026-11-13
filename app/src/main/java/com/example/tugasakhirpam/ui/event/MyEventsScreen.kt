package com.example.tugasakhirpam.ui.event

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tugasakhirpam.model.SampleData
import com.example.tugasakhirpam.ui.components.AppBottomBar
import com.example.tugasakhirpam.ui.components.BottomNavItem
import com.example.tugasakhirpam.ui.components.InfoBadge
import com.example.tugasakhirpam.ui.components.MessageState
import com.example.tugasakhirpam.ui.components.PageHeader
import com.example.tugasakhirpam.ui.theme.AppBackground
import com.example.tugasakhirpam.ui.theme.BrandBlue
import com.example.tugasakhirpam.ui.theme.SegmentTrack
import com.example.tugasakhirpam.ui.theme.TextSecondary
import com.example.tugasakhirpam.ui.theme.TugasAkhirPAMTheme

@Composable
fun MyEventsScreen(
    resultMessage: String?,
    onResultConsumed: () -> Unit,
    onEventClick: (Long) -> Unit,
    onCreateEvent: () -> Unit,
    onBottomNavClick: (BottomNavItem) -> Unit,
    viewModel: EventListViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.start(EventListSource.MY_EVENTS) }

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
        onRefresh = viewModel::refresh,
        onRetry = { viewModel.start(EventListSource.MY_EVENTS) },
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateEvent,
                containerColor = BrandBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(14.dp),
                icon = { Icon(Icons.Outlined.Add, null) },
                text = { Text("Buat Event", style = MaterialTheme.typography.labelLarge) }
            )
        },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PageHeader("Penyelenggara", "My Events", "Kelola event yang kamu buat")
            EventListBody(
                isLoading = state.isLoading,
                isRefreshing = state.isRefreshing,
                errorMessage = state.errorMessage,
                events = state.events,
                onRefresh = onRefresh,
                onRetry = onRetry,
                onEventClick = onEventClick,
                bottomPadding = 96.dp,
                emptyContent = {
                    MessageState(
                        icon = Icons.AutoMirrored.Outlined.EventNote,
                        title = "Belum ada event",
                        subtitle = "Buat event pertamamu dan kelola pendaftarnya di sini.",
                        actionText = "Buat Event",
                        onAction = onCreateEvent
                    )
                },
                cardFooter = { event ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        InfoBadge("${event.registeredCount}/${event.quota} peserta")
                        if (event.isPast) InfoBadge("Selesai", background = SegmentTrack, contentColor = TextSecondary)
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
            state = EventListUiState(isLoading = false, events = SampleData.events.take(2)),
            onEventClick = {},
            onCreateEvent = {},
            onBottomNavClick = {},
            onRefresh = {},
            onRetry = {},
        )
    }
}
