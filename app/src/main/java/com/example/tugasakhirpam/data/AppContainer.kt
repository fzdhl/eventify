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
                id = real.id,
                name = real.name.ifBlank { fake.name },
                nim = real.nim.ifBlank { fake.nim },
                email = real.email.ifBlank { fake.email },
                phone = real.phone,
                studyProgram = real.studyProgram,
                avatarUrl = real.avatarUrl,
                updatedAt = real.updatedAt,
            )
        }

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
    val reviewRepository: ReviewRepository by lazy {
        SupabaseReviewRepository(
            client = SupabaseProvider.client
        )
    }
    val categoryRepository: CategoryRepository by lazy {
        SupabaseCategoryRepository(
            client = SupabaseProvider.client,
            eventRepository = eventRepository,
        )
    }
}
