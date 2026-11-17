package com.example.tugasakhirpam.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.tugasakhirpam.data.AppContainer
import com.example.tugasakhirpam.data.AuthState
import com.example.tugasakhirpam.data.SupabaseProvider
import com.example.tugasakhirpam.ui.components.LoadingState
import com.example.tugasakhirpam.ui.theme.AppBackground
import com.example.tugasakhirpam.ui.theme.TextPrimary
import com.example.tugasakhirpam.ui.theme.TextSecondary

/**
 * Pintu masuk aplikasi. Menentukan layar berdasarkan status sesi Supabase:
 * belum dipulihkan -> loading, belum login -> Login/Register, sudah login -> AppNavHost.
 * Logout dari mana pun otomatis kembali ke Login karena status berubah menjadi SignedOut.
 */
@Composable
fun AuthGate() {
    if (!SupabaseProvider.isConfigured) {
        MissingConfigScreen()
        return
    }

    val authState by AppContainer.authRepository.state.collectAsStateWithLifecycle()

    when (authState) {
        AuthState.Loading -> LoadingState(Modifier.background(AppBackground))
        AuthState.SignedOut -> AuthNavHost()
        is AuthState.SignedIn -> {
            val navController = rememberNavController()
            AppNavHost(navController = navController)
        }
    }
}

@Composable
private fun MissingConfigScreen() {
    Box(
        Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Supabase belum dikonfigurasi", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            Text(
                "Tambahkan supabase.url dan supabase.anon.key ke file local.properties, lalu build ulang. " +
                    "Langkahnya ada di README.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
