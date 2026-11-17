package com.example.tugasakhirpam.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tugasakhirpam.model.Event
import com.example.tugasakhirpam.model.Category
import com.example.tugasakhirpam.model.SampleData
import com.example.tugasakhirpam.model.User
import com.example.tugasakhirpam.ui.components.AppBottomBar
import com.example.tugasakhirpam.ui.components.BottomNavItem
import com.example.tugasakhirpam.ui.components.CategoryTile
import com.example.tugasakhirpam.ui.components.ErrorState
import com.example.tugasakhirpam.ui.components.EventCard
import com.example.tugasakhirpam.ui.components.EventImage
import com.example.tugasakhirpam.ui.components.LoadingState
import com.example.tugasakhirpam.ui.components.MessageState
import com.example.tugasakhirpam.ui.components.SectionHeader
import com.example.tugasakhirpam.ui.profile.ProfileAvatar
import com.example.tugasakhirpam.ui.theme.AppBackground
import com.example.tugasakhirpam.ui.theme.BorderInput
import com.example.tugasakhirpam.ui.theme.BrandBlue
import com.example.tugasakhirpam.ui.theme.SurfaceWhite
import com.example.tugasakhirpam.ui.theme.TextMuted
import com.example.tugasakhirpam.ui.theme.TextPrimary
import com.example.tugasakhirpam.ui.theme.TextSecondary
import com.example.tugasakhirpam.ui.theme.TugasAkhirPAMTheme
import com.example.tugasakhirpam.util.DateFormatter

@Composable
fun HomeScreen(
    state: HomeUiState,
    resultMessage: String?,
    onResultConsumed: () -> Unit,
    onEventClick: (Long) -> Unit,
    onSeeAllEvents: () -> Unit,
    onCategoryClick: (Category) -> Unit,
    onBottomNavClick: (BottomNavItem) -> Unit,
    onRetry: () -> Unit,
    onProfileClick: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var query by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(resultMessage) {
        resultMessage?.let {
            snackbarHostState.showSnackbar(it)
            onResultConsumed()
        }
    }

    HomeContent(
        state = state,
        query = query,
        snackbarHostState = snackbarHostState,
        onQueryChange = { query = it },
        onEventClick = onEventClick,
        onSeeAllEvents = onSeeAllEvents,
        onCategoryClick = onCategoryClick,
        onBottomNavClick = onBottomNavClick,
        onRetry = onRetry,
        onProfileClick = onProfileClick,
    )
}

@Composable
fun HomeContent(
    state: HomeUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onEventClick: (Long) -> Unit,
    onSeeAllEvents: () -> Unit,
    onCategoryClick: (Category) -> Unit,
    onBottomNavClick: (BottomNavItem) -> Unit,
    onRetry: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onProfileClick: () -> Unit = {},
) {
    Scaffold(
        containerColor = AppBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { AppBottomBar(BottomNavItem.HOME, onBottomNavClick) },
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingState(Modifier.padding(innerPadding))

            state.errorMessage != null && state.events.isEmpty() ->
                ErrorState(state.errorMessage, onRetry, Modifier.padding(innerPadding))

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item(key = "header") { HomeHeader(state.user, onProfileClick) }
                item(key = "search") { SearchField(query, onQueryChange) }

                if (query.isNotBlank()) {
                    val results = state.search(query)
                    item(key = "result-title") {
                        SectionHeader("Hasil pencarian (${results.size})", Modifier.padding(top = 6.dp))
                    }
                    if (results.isEmpty()) {
                        item(key = "result-empty") {
                            MessageState(
                                icon = Icons.Outlined.SearchOff,
                                title = "Event tidak ditemukan",
                                subtitle = "Coba kata kunci lain, misalnya nama kategori atau lokasi."
                            )
                        }
                    }
                    items(results, key = { "result-${it.id}" }) { event ->
                        EventCard(event = event, onClick = { onEventClick(event.id) })
                    }
                } else {
                    state.featuredEvent?.let { featured ->
                        item(key = "featured") {
                            FeaturedEventCard(featured, onClick = { onEventClick(featured.id) })
                        }
                    }
                    item(key = "category-title") {
                        SectionHeader("Kategori", Modifier.padding(top = 6.dp), "Lihat semua", onSeeAllEvents)
                    }
                    item(key = "categories") {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            state.categories.take(4).forEach { category ->
                                CategoryTile(
                                    category = category,
                                    onClick = { onCategoryClick(category) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    item(key = "events-title") {
                        SectionHeader("Event untukmu", Modifier.padding(top = 6.dp), "Lihat semua", onSeeAllEvents)
                    }
                    if (state.recommended.isEmpty()) {
                        item(key = "events-empty") {
                            MessageState(
                                icon = Icons.Outlined.EventBusy,
                                title = "Belum ada event baru",
                                subtitle = "Event mendatang akan muncul di sini."
                            )
                        }
                    }
                    items(state.recommended, key = { it.id }) { event ->
                        EventCard(event = event, onClick = { onEventClick(event.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(user: User, onProfileClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(DateFormatter.greeting(), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(Modifier.height(2.dp))
            Text("Hai, ${user.firstName}!", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
        }
        ProfileAvatar(
            user = user,
            size = 46.dp,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onProfileClick)
        )
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        placeholder = {
            Text("Cari event, kategori, atau lokasi...", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
        },
        leadingIcon = { Icon(Icons.Outlined.Search, null, tint = TextMuted) },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Outlined.Close, "Hapus pencarian", tint = TextMuted)
                }
            }
        } else null,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        shape = RoundedCornerShape(14.dp),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BrandBlue,
            unfocusedBorderColor = BorderInput,
            focusedContainerColor = SurfaceWhite,
            unfocusedContainerColor = SurfaceWhite,
            cursorColor = BrandBlue,
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun FeaturedEventCard(event: Event, onClick: () -> Unit) {
    val tagline = event.description.split(". ").first().trimEnd('.') + "."

    Box(
        Modifier
            .fillMaxWidth()
            .height(196.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        EventImage(event.imageUrl, Modifier.fillMaxSize(), event.title)
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xE60B1B3F), Color(0xA60B1B3F), Color(0x330B1B3F))
                    )
                )
        )
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "FEATURED EVENT",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                event.title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                tagline,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(0.85f)
            )
            Spacer(Modifier.height(14.dp))
            Surface(onClick = onClick, shape = RoundedCornerShape(10.dp), color = Color.White) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Lihat Event", color = BrandBlue, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Outlined.ChevronRight, null, tint = BrandBlue, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun HomeContentPreview() {
    TugasAkhirPAMTheme {
        HomeContent(
            state = HomeUiState(isLoading = false, user = SampleData.user, events = SampleData.events),
            query = "",
            onQueryChange = {},
            onEventClick = {},
            onSeeAllEvents = {},
            onCategoryClick = {},
            onBottomNavClick = {},
            onRetry = {},
        )
    }
}
