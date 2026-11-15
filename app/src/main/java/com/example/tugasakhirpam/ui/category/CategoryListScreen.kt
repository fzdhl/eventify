package com.example.tugasakhirpam.ui.category

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tugasakhirpam.model.Category
import com.example.tugasakhirpam.ui.components.AppBottomBar
import com.example.tugasakhirpam.ui.components.BottomNavItem
import com.example.tugasakhirpam.ui.components.CategoryLabel
import com.example.tugasakhirpam.ui.components.CircleIconButton
import com.example.tugasakhirpam.ui.components.ErrorState
import com.example.tugasakhirpam.ui.components.iconForCategory
import com.example.tugasakhirpam.ui.components.LoadingState
import com.example.tugasakhirpam.ui.components.MessageState
import com.example.tugasakhirpam.ui.theme.AppBackground
import com.example.tugasakhirpam.ui.theme.BorderSoft
import com.example.tugasakhirpam.ui.theme.BrandBlue
import com.example.tugasakhirpam.ui.theme.BrandBlueSoft
import com.example.tugasakhirpam.ui.theme.SurfaceWhite
import com.example.tugasakhirpam.ui.theme.TextPrimary
import com.example.tugasakhirpam.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    state: CategoryListUiState,
    resultMessage: String?,
    onResultConsumed: () -> Unit,
    onCategoryClick: (Long) -> Unit,
    onAddCategoryClick: () -> Unit,
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

    Scaffold(
        containerColor = AppBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { AppBottomBar(BottomNavItem.CATEGORIES, onBottomNavClick) },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    CategoryLabel("Jelajahi")
                    Spacer(Modifier.height(4.dp))
                    Text("Categories", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Temukan event berdasarkan minatmu",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                CircleIconButton(
                    icon = Icons.Outlined.Add,
                    contentDescription = "Tambah kategori",
                    onClick = onAddCategoryClick,
                    bordered = true,
                )
            }

            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                val error = state.errorMessage
                val items = state.categories
                when {
                    state.isLoading -> LoadingState()

                    error != null && items.isEmpty() -> ErrorState(error, onRetry)

                    items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        MessageState(
                            icon = Icons.Outlined.Apps,
                            title = "Belum ada kategori",
                            subtitle = "Buat kategori pertama agar event mudah ditemukan.",
                            actionText = "Tambah Kategori",
                            onAction = onAddCategoryClick,
                        )
                    }

                    else -> LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        itemsIndexed(
                            items = items,
                            key = { _, category -> category.id },
                            // Kartu terakhir melebar penuh bila jumlahnya ganjil, seperti kartu Technology di desain
                            span = { index, _ ->
                                if (index == items.lastIndex && items.size % 2 == 1) GridItemSpan(maxLineSpan)
                                else GridItemSpan(1)
                            }
                        ) { index, category ->
                            CategoryCard(
                                category = category,
                                fullWidth = index == items.lastIndex && items.size % 2 == 1,
                                onClick = { onCategoryClick(category.id) },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    fullWidth: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSoft),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = if (fullWidth) 145.dp else 170.dp)
    ) {
        Column(Modifier.padding(17.dp)) {
            Box(
                Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BrandBlueSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconForCategory(category.iconKey), null, tint = BrandBlue, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(13.dp))
            Text(
                category.name,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (category.description.isNotBlank()) {
                Spacer(Modifier.height(5.dp))
                Text(
                    category.description,
                    fontSize = 10.sp,
                    lineHeight = 15.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${category.eventCount} events",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue
                )
                Icon(Icons.Outlined.ChevronRight, null, tint = BrandBlue, modifier = Modifier.size(14.dp))
            }
        }
    }
}
