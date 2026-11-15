package com.example.tugasakhirpam.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tugasakhirpam.model.Category
import com.example.tugasakhirpam.ui.components.CategoryLabel
import com.example.tugasakhirpam.ui.components.ConfirmDialog
import com.example.tugasakhirpam.ui.components.ErrorState
import com.example.tugasakhirpam.ui.components.iconForCategory
import com.example.tugasakhirpam.ui.components.EventCard
import com.example.tugasakhirpam.ui.components.LoadingState
import com.example.tugasakhirpam.ui.components.MessageState
import com.example.tugasakhirpam.ui.components.ScreenTopBar
import com.example.tugasakhirpam.ui.components.SectionHeader
import com.example.tugasakhirpam.ui.theme.AppBackground
import com.example.tugasakhirpam.ui.theme.BrandBlue
import com.example.tugasakhirpam.ui.theme.BrandBlueSoft
import com.example.tugasakhirpam.ui.theme.BrandBlueSoftBorder
import com.example.tugasakhirpam.ui.theme.TextPrimary
import com.example.tugasakhirpam.ui.theme.TextSecondary

@Composable
fun CategoryDetailScreen(
    state: CategoryDetailUiState,
    resultMessage: String?,
    onResultConsumed: () -> Unit,
    onBack: () -> Unit,
    onEditCategory: (Long) -> Unit,
    onEventClick: (Long) -> Unit,
    onDelete: () -> Unit,
    onSnackbarDismissed: () -> Unit,
    onDeleted: (String) -> Unit,
    onRetry: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(resultMessage) {
        resultMessage?.let {
            snackbarHostState.showSnackbar(it)
            onResultConsumed()
        }
    }
    LaunchedEffect(state.errorMessage) {
        // Error saat memuat ditampilkan lewat ErrorState; snackbar hanya untuk error aksi (misalnya gagal hapus)
        if (state.category != null) {
            state.errorMessage?.let {
                snackbarHostState.showSnackbar(it)
                onSnackbarDismissed()
            }
        }
    }
    LaunchedEffect(state.deletedMessage) { state.deletedMessage?.let(onDeleted) }

    Scaffold(
        containerColor = AppBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScreenTopBar("Category Detail", onBack, Modifier.padding(horizontal = 20.dp))

            val category = state.category
            when {
                state.isLoading && category == null -> LoadingState()

                category == null -> ErrorState(
                    message = state.errorMessage ?: "Kategori tidak ditemukan.",
                    onRetry = onRetry
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "hero") { CategoryHero(category) }
                    item(key = "actions") {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            ActionButton(
                                icon = Icons.Outlined.Edit,
                                label = "Edit Category",
                                enabled = !state.isProcessing,
                                modifier = Modifier.weight(1f),
                                onClick = { onEditCategory(category.id) }
                            )
                            ActionButton(
                                icon = Icons.Outlined.Delete,
                                label = "Delete",
                                enabled = !state.isProcessing,
                                modifier = Modifier.weight(1f),
                                onClick = { showDeleteDialog = true }
                            )
                        }
                    }
                    item(key = "section") {
                        SectionHeader("${state.events.size} Event tersedia", Modifier.padding(top = 8.dp))
                    }
                    if (state.events.isEmpty()) {
                        item(key = "empty") {
                            MessageState(
                                icon = Icons.Outlined.EventBusy,
                                title = "Belum ada event",
                                subtitle = "Belum ada event di kategori ini."
                            )
                        }
                    } else {
                        items(state.events, key = { it.id }) { event ->
                            EventCard(event = event, onClick = { onEventClick(event.id) })
                        }
                    }
                }
            }
        }
    }

    val current = state.category
    if (showDeleteDialog && current != null) {
        ConfirmDialog(
            title = "Hapus kategori?",
            message = "Kategori \"${current.name}\" akan dihapus permanen. Kategori yang masih dipakai event tidak bisa dihapus.",
            confirmText = "Hapus",
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

@Composable
private fun CategoryHero(category: Category) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(listOf(BrandBlueSoft, BrandBlueSoftBorder)))
            .border(1.dp, BrandBlueSoftBorder, shape)
            .padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Box(
            Modifier
                .size(55.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(BrandBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(iconForCategory(category.iconKey), null, tint = Color.White, modifier = Modifier.size(30.dp))
        }
        Column(Modifier.weight(1f)) {
            CategoryLabel("Kategori")
            Spacer(Modifier.height(5.dp))
            Text(category.name, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            if (category.description.isNotBlank()) {
                Spacer(Modifier.height(5.dp))
                Text(category.description, fontSize = 11.sp, lineHeight = 16.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = BrandBlue),
        modifier = modifier
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(5.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
