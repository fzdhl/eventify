package com.example.tugasakhirpam.ui.registration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tugasakhirpam.model.SampleData
import com.example.tugasakhirpam.ui.components.AppBottomBar
import com.example.tugasakhirpam.ui.components.BottomNavItem
import com.example.tugasakhirpam.ui.components.ErrorState
import com.example.tugasakhirpam.ui.components.EventCard
import com.example.tugasakhirpam.ui.components.InfoBadge
import com.example.tugasakhirpam.ui.components.LoadingState
import com.example.tugasakhirpam.ui.components.MessageState
import com.example.tugasakhirpam.ui.components.PageHeader
import com.example.tugasakhirpam.ui.components.SegmentedTabs
import com.example.tugasakhirpam.ui.components.StatusBadge
import com.example.tugasakhirpam.ui.theme.AppBackground
import com.example.tugasakhirpam.ui.theme.BrandBlue
import com.example.tugasakhirpam.ui.theme.SegmentTrack
import com.example.tugasakhirpam.ui.theme.TextSecondary
import com.example.tugasakhirpam.ui.theme.TugasAkhirPAMTheme

@Composable
fun RegistrationListScreen(
    resultMessage: String?,
    onResultConsumed: () -> Unit,
    onRegistrationClick: (Long) -> Unit,
    onAddRegistrationClick: () -> Unit,
    onBottomNavClick: (BottomNavItem) -> Unit,
    viewModel: RegistrationListViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.loadRegistrations() }

    LaunchedEffect(resultMessage) {
        resultMessage?.let {
            snackbarHostState.showSnackbar(it)
            onResultConsumed()
        }
    }
    LaunchedEffect(state.errorMessage) {
        val msg = state.errorMessage
        if (msg != null && state.registrations.isNotEmpty()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.errorShown()
        }
    }

    RegistrationListContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onTabSelect = viewModel::selectTab,
        onRegistrationClick = onRegistrationClick,
        onAddRegistrationClick = onAddRegistrationClick,
        onBottomNavClick = onBottomNavClick,
        onRefresh = viewModel::refresh,
        onRetry = viewModel::loadRegistrations,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationListContent(
    state: RegistrationListUiState,
    onTabSelect: (RegistrationTab) -> Unit,
    onRegistrationClick: (Long) -> Unit,
    onAddRegistrationClick: () -> Unit,
    onBottomNavClick: (BottomNavItem) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        containerColor = AppBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { AppBottomBar(BottomNavItem.REGISTRATIONS, onBottomNavClick) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddRegistrationClick,
                containerColor = BrandBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(14.dp),
                icon = { Icon(Icons.Outlined.Add, null) },
                text = { Text("Daftar Event", style = MaterialTheme.typography.labelLarge) }
            )
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PageHeader("Event Kamu", "My Registrations", "Pantau semua pendaftaran eventmu")

            SegmentedTabs(
                tabs = RegistrationTab.entries.map {
                    if (state.isLoading) it.label else "${it.label} (${state.countFor(it)})"
                },
                selectedIndex = state.selectedTab.ordinal,
                onSelect = { onTabSelect(RegistrationTab.entries[it]) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(4.dp))

            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    state.isLoading -> LoadingState()

                    state.errorMessage != null && state.registrations.isEmpty() ->
                        ErrorState(state.errorMessage, onRetry)

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (state.filtered.isEmpty()) {
                            item(key = "empty") {
                                val cancelledTab = state.selectedTab == RegistrationTab.CANCELLED
                                Box(Modifier.fillParentMaxHeight(0.75f), contentAlignment = Alignment.Center) {
                                    MessageState(
                                        icon = if (cancelledTab) Icons.Outlined.EventBusy else Icons.Outlined.ConfirmationNumber,
                                        title = if (cancelledTab) "Tidak ada pendaftaran dibatalkan" else "Belum ada pendaftaran",
                                        subtitle = if (cancelledTab) "Semua pendaftaranmu masih aktif."
                                        else "Daftar ke event favoritmu dan pantau statusnya di sini.",
                                        actionText = if (cancelledTab) null else "Daftar Event",
                                        onAction = onAddRegistrationClick,
                                    )
                                }
                            }
                        } else {
                            items(state.filtered, key = { it.id }) { registration ->
                                val event = registration.event
                                if (event != null) {
                                    EventCard(
                                        event = event,
                                        onClick = { onRegistrationClick(registration.id) },
                                        modifier = Modifier.animateItem(),
                                        footer = {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                StatusBadge(registration.status)
                                                if (registration.isActive && event.isPast) {
                                                    InfoBadge("Selesai", background = SegmentTrack, contentColor = TextSecondary)
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun RegistrationListContentPreview() {
    TugasAkhirPAMTheme {
        RegistrationListContent(
            state = RegistrationListUiState(isLoading = false, registrations = SampleData.registrations),
            onTabSelect = {},
            onRegistrationClick = {},
            onAddRegistrationClick = {},
            onBottomNavClick = {},
            onRefresh = {},
            onRetry = {},
        )
    }
}
