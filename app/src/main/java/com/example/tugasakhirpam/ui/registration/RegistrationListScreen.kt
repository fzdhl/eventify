package com.example.tugasakhirpam.ui.registration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
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
import com.example.tugasakhirpam.model.SampleData
import com.example.tugasakhirpam.ui.components.AppBottomBar
import com.example.tugasakhirpam.ui.components.BottomNavItem
import com.example.tugasakhirpam.ui.components.ErrorState
import com.example.tugasakhirpam.ui.components.LoadingState
import com.example.tugasakhirpam.ui.components.MessageState
import com.example.tugasakhirpam.ui.components.PageHeader
import com.example.tugasakhirpam.ui.components.RegistrationCard
import com.example.tugasakhirpam.ui.components.SegmentedTabs
import com.example.tugasakhirpam.ui.theme.AppBackground
import com.example.tugasakhirpam.ui.theme.TugasAkhirPAMTheme

@Composable
fun RegistrationListScreen(
    state: RegistrationListUiState,
    resultMessage: String?,
    onResultConsumed: () -> Unit,
    onTabSelect: (RegistrationTab) -> Unit,
    onRegistrationClick: (Long) -> Unit,
    onAddRegistrationClick: () -> Unit,
    onBottomNavClick: (BottomNavItem) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onErrorDismissed: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(resultMessage) {
        resultMessage?.let {
            snackbarHostState.showSnackbar(it)
            onResultConsumed()
        }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorDismissed()
        }
    }

    RegistrationListContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onTabSelect = onTabSelect,
        onRegistrationClick = onRegistrationClick,
        onAddRegistrationClick = onAddRegistrationClick,
        onBottomNavClick = onBottomNavClick,
        onRefresh = onRefresh,
        onRetry = onRetry,
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
    val tabs = remember { RegistrationTab.entries.map { it.label } }

    Scaffold(
        containerColor = AppBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { AppBottomBar(BottomNavItem.REGISTRATIONS, onBottomNavClick) },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PageHeader("Riwayat", "My Registrations", "Pantau status pendaftaran event kamu")
            SegmentedTabs(
                tabs = tabs,
                selectedIndex = state.selectedTab.ordinal,
                onSelect = { onTabSelect(RegistrationTab.entries[it]) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(12.dp))

            when {
                state.isLoading -> LoadingState()
                state.errorMessage != null && state.registrations.isEmpty() -> ErrorState(state.errorMessage, onRetry)
                else -> PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (state.filtered.isEmpty()) {
                        MessageState(
                            icon = Icons.Outlined.AssignmentTurnedIn,
                            title = if (state.selectedTab == RegistrationTab.UPCOMING) "Belum ada event mendatang"
                            else "Belum ada riwayat pendaftaran",
                            subtitle = if (state.selectedTab == RegistrationTab.UPCOMING) "Daftar event baru untuk mengisinya."
                            else "Pendaftaran yang sudah selesai atau dibatalkan akan muncul di sini.",
                            actionText = if (state.selectedTab == RegistrationTab.UPCOMING) "Daftar Event" else null,
                            onAction = if (state.selectedTab == RegistrationTab.UPCOMING) onAddRegistrationClick else null
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
                        ) {
                            items(state.filtered, key = { it.id }) { registration ->
                                RegistrationCard(
                                    registration = registration,
                                    onClick = { onRegistrationClick(registration.id) },
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
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun RegistrationListContentPreview() {
    TugasAkhirPAMTheme {
        RegistrationListContent(
            state = RegistrationListUiState(isLoading = false, registrations = listOf(SampleData.registration)),
            onTabSelect = {},
            onRegistrationClick = {},
            onAddRegistrationClick = {},
            onBottomNavClick = {},
            onRefresh = {},
            onRetry = {},
        )
    }
}
