package com.example.tugasakhirpam.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tugasakhirpam.ui.auth.LoginScreen
import com.example.tugasakhirpam.ui.auth.LoginViewModel
import com.example.tugasakhirpam.ui.auth.RegisterScreen
import com.example.tugasakhirpam.ui.auth.RegisterViewModel

/**
 * Graf navigasi untuk user yang belum masuk: Login dan Register.
 * Dipisah dari AppNavHost supaya halaman utama (tab, event, dll) tidak pernah bisa dibuka tanpa sesi.
 * Setelah login berhasil, AuthGate mengganti seluruh graf ini dengan AppNavHost.
 */
@Composable
fun AuthNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Login, modifier = modifier) {

        composable<Login> { entry ->
            val viewModel: LoginViewModel = viewModel(entry)
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            // Pesan dari layar Register (mis. "cek email untuk verifikasi").
            val result by entry.savedStateHandle
                .getStateFlow<String?>(RESULT_MESSAGE_KEY, null)
                .collectAsStateWithLifecycle()

            LoginScreen(
                state = state,
                resultMessage = result,
                onResultConsumed = { entry.savedStateHandle.set<String?>(RESULT_MESSAGE_KEY, null) },
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onTogglePasswordVisible = viewModel::togglePasswordVisible,
                onRememberChange = viewModel::onRememberChange,
                onSubmit = viewModel::submit,
                onForgotPassword = viewModel::forgotPassword,
                onRegisterClick = { navController.navigate(Register) },
                onMessageShown = viewModel::messageShown,
            )
        }

        composable<Register> { entry ->
            val viewModel: RegisterViewModel = viewModel(entry)
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            RegisterScreen(
                state = state,
                onNameChange = viewModel::onNameChange,
                onNimChange = viewModel::onNimChange,
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onConfirmChange = viewModel::onConfirmChange,
                onTogglePasswordVisible = viewModel::togglePasswordVisible,
                onToggleConfirmVisible = viewModel::toggleConfirmVisible,
                onTermsChange = viewModel::onTermsChange,
                onSubmit = viewModel::submit,
                onLoginClick = dropUnlessResumed { navController.popBackStack() },
                onMessageShown = viewModel::messageShown,
                onConfirmationRequired = { message ->
                    viewModel.confirmationHandled()
                    navController.popWithResult(message)
                },
            )
        }
    }
}
