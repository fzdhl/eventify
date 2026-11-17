package com.example.tugasakhirpam.data

import com.example.tugasakhirpam.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

/**
 * Satu klien Supabase untuk seluruh aplikasi. Anggota tim lain cukup memakai
 * [SupabaseProvider.client] di repository masing-masing, tidak perlu membuat klien baru.
 */
object SupabaseProvider {

    /** False jika supabase.url / supabase.anon.key belum diisi di local.properties. */
    val isConfigured: Boolean
        get() = BuildConfig.SUPABASE_URL.startsWith("https://") && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
        }
    }
}
