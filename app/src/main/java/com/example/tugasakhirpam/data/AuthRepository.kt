package com.example.tugasakhirpam.data

import com.example.tugasakhirpam.model.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put

sealed interface AuthState {
    /** Sesi sedang dipulihkan dari penyimpanan lokal. */
    data object Loading : AuthState
    data object SignedOut : AuthState
    data class SignedIn(val user: User) : AuthState
}

data class RegisterInput(
    val name: String,
    val nim: String,
    val email: String,
    val password: String,
)

enum class SignUpResult {
    /** Konfirmasi email dimatikan di Supabase, user langsung masuk. */
    SIGNED_IN,

    /** Konfirmasi email aktif, user harus membuka email verifikasi dulu. */
    EMAIL_CONFIRMATION_REQUIRED,
}

interface AuthRepository {
    val state: StateFlow<AuthState>
    suspend fun signIn(email: String, password: String)
    suspend fun signUp(input: RegisterInput): SignUpResult
    suspend fun signOut()
    suspend fun sendPasswordReset(email: String)

    /** Dipanggil ProfileRepository setelah profil berubah supaya semua layar ikut diperbarui. */
    fun publishUser(user: User)
}

class SupabaseAuthRepository(
    private val client: SupabaseClient,
    scope: CoroutineScope,
) : AuthRepository {

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    override val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        scope.launch {
            client.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Initializing -> {
                        if (_state.value !is AuthState.SignedIn) _state.value = AuthState.Loading
                    }
                    is SessionStatus.Authenticated -> onAuthenticated(status.session.user)
                    is SessionStatus.NotAuthenticated -> _state.value = AuthState.SignedOut
                    is SessionStatus.RefreshFailure -> {
                        // Offline saat token kedaluwarsa. Pertahankan status yang ada,
                        // kecuali masih Loading supaya layar tidak menggantung.
                        if (_state.value is AuthState.Loading) _state.value = AuthState.SignedOut
                    }
                }
            }
        }
    }

    private suspend fun onAuthenticated(info: UserInfo?) {
        if (info == null) {
            _state.value = AuthState.SignedOut
            return
        }
        _state.value = AuthState.SignedIn(loadUser(info))
    }

    /** Baca profil dari tabel; jika gagal (offline atau baris belum ada) pakai metadata akun. */
    private suspend fun loadUser(info: UserInfo): User {
        val email = info.email.orEmpty()
        val metaName = (info.userMetadata?.get("full_name") as? JsonPrimitive)?.contentOrNull.orEmpty()
        val metaNim = (info.userMetadata?.get("nim") as? JsonPrimitive)?.contentOrNull.orEmpty()
        val fallback = User(
            id = info.id,
            name = metaName.ifBlank { email.substringBefore("@") },
            nim = metaNim,
            email = email,
            phone = "",
        )
        return try {
            client.from("profiles")
                .select { filter { eq("id", info.id) } }
                .decodeSingleOrNull<ProfileDto>()
                ?.toUser(email)
                ?: fallback
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            fallback
        }
    }

    override suspend fun signIn(email: String, password: String) {
        client.auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }
    }

    override suspend fun signUp(input: RegisterInput): SignUpResult {
        client.auth.signUpWith(Email) {
            email = input.email.trim()
            password = input.password
            // Dibaca trigger handle_new_user (lihat supabase/auth_profile_setup.sql) untuk membuat baris profiles.
            data = buildJsonObject {
                put("full_name", input.name.trim())
                put("nim", input.nim.trim())
            }
        }
        return if (client.auth.currentSessionOrNull() != null) {
            SignUpResult.SIGNED_IN
        } else {
            SignUpResult.EMAIL_CONFIRMATION_REQUIRED
        }
    }

    override suspend fun signOut() {
        client.auth.signOut()
    }

    override suspend fun sendPasswordReset(email: String) {
        client.auth.resetPasswordForEmail(email.trim())
    }

    override fun publishUser(user: User) {
        if (_state.value is AuthState.SignedIn) _state.value = AuthState.SignedIn(user)
    }
}
