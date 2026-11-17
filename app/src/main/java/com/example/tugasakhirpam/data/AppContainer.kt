package com.example.tugasakhirpam.data

import android.content.Context
import com.example.tugasakhirpam.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object AppContainer {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var appContext: Context

    /** Dipanggil sekali dari MainActivity.onCreate. */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    val loginPrefs: LoginPrefs by lazy { LoginPrefs(appContext) }

    // ====================== Auth + Profil (Supabase) ======================
    val authRepository: AuthRepository by lazy { SupabaseAuthRepository(SupabaseProvider.client, appScope) }
    val profileRepository: ProfileRepository by lazy { SupabaseProfileRepository(SupabaseProvider.client, authRepository) }

    /**
     * JEMBATAN SEMENTARA. Repository event, review, dan registrasi masih memakai FakeDataSource
     * yang mengenali pemilik data lewat id "u-001". Karena itu id tetap dari data palsu,
     * sedangkan nama, NIM, email, HP, prodi, dan foto diambil dari akun yang sedang login.
     * Hapus jembatan ini setelah semua repository pindah ke Supabase dan memakai id auth asli.
     */
//    val currentUser: User
//        get() {
//            val fake = FakeDataSource.currentUser
//            if (!SupabaseProvider.isConfigured) return fake
//            val real = (authRepository.state.value as? AuthState.SignedIn)?.user ?: return fake
//            return fake.copy(
//                name = real.name.ifBlank { fake.name },
//                nim = real.nim.ifBlank { fake.nim },
//                email = real.email.ifBlank { fake.email },
//                phone = real.phone,
//                studyProgram = real.studyProgram,
//                avatarUrl = real.avatarUrl,
//                updatedAt = real.updatedAt,
//            )
//        }

    val currentUser: User
        get() {
            val fake = FakeDataSource.currentUser

            if (!SupabaseProvider.isConfigured) return fake

            val real = (authRepository.state.value as? AuthState.SignedIn)?.user
                ?: return fake

            return fake.copy(
                id = real.id, // PENTING: gunakan UUID asli Supabase
                name = real.name.ifBlank { fake.name },
                nim = real.nim.ifBlank { fake.nim },
                email = real.email.ifBlank { fake.email },
                phone = real.phone,
                studyProgram = real.studyProgram,
                avatarUrl = real.avatarUrl,
                updatedAt = real.updatedAt,
            )
        }


    // ============== Fitur lain (masih data palsu, milik anggota masing-masing) ==============
    //val eventRepository: EventRepository by lazy { FakeEventRepository() }

    val eventRepository: EventRepository by lazy {
        SupabaseEventRepository(
            client = SupabaseProvider.client
        )
    }
    val registrationRepository: RegistrationRepository by lazy {
        SupabaseRegistrationRepository(
            client = SupabaseProvider.client,
            eventRepository = eventRepository
        )
    }
    val reviewRepository: ReviewRepository by lazy { FakeReviewRepository() }
    val categoryRepository: CategoryRepository by lazy {
        SupabaseCategoryRepository(
            client = SupabaseProvider.client,
            eventRepository = eventRepository,
        )
    }
}
