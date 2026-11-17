package com.example.tugasakhirpam.data

import com.example.tugasakhirpam.model.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType

data class ProfileInput(
    val name: String,
    val nim: String,
    val phone: String,
    val studyProgram: String,
)

/** Foto profil yang dipilih user dan belum diunggah. */
class AvatarUpload(val bytes: ByteArray, val mimeType: String)

interface ProfileRepository {
    suspend fun getProfile(): User

    /** Simpan perubahan profil. Jika [avatar] ada, foto diunggah ke Supabase Storage lebih dulu. */
    suspend fun updateProfile(input: ProfileInput, avatar: AvatarUpload?): User
}

class SupabaseProfileRepository(
    private val client: SupabaseClient,
    private val authRepository: AuthRepository,
) : ProfileRepository {

    private fun requireUser() =
        client.auth.currentUserOrNull() ?: error("Sesi berakhir. Silakan masuk lagi.")

    override suspend fun getProfile(): User {
        val info = requireUser()
        val dto = client.from("profiles")
            .select { filter { eq("id", info.id) } }
            .decodeSingleOrNull<ProfileDto>()
        return dto?.toUser(info.email.orEmpty()) ?: error("Profil tidak ditemukan.")
    }

    override suspend fun updateProfile(input: ProfileInput, avatar: AvatarUpload?): User {
        val info = requireUser()

        var avatarUrl: String? = null
        if (avatar != null) {
            val bucket = client.storage.from(AVATAR_BUCKET)
            val path = "${info.id}/avatar"
            bucket.upload(path, avatar.bytes) {
                upsert = true
                contentType = ContentType.parse(avatar.mimeType)
            }
            // Parameter v mencegah Coil menampilkan foto lama dari cache setelah diganti.
            avatarUrl = bucket.publicUrl(path) + "?v=" + System.currentTimeMillis()
        }

        val updated = client.from("profiles")
            .update({
                set("full_name", input.name.trim())
                set("nim", input.nim.trim())
                set("phone", input.phone.trim().ifBlank { null })
                set("study_program", input.studyProgram.trim().ifBlank { null })
                if (avatarUrl != null) set("avatar_url", avatarUrl)
            }) {
                filter { eq("id", info.id) }
                select()
            }
            .decodeSingle<ProfileDto>()
            .toUser(info.email.orEmpty())

        authRepository.publishUser(updated)
        return updated
    }

    private companion object {
        const val AVATAR_BUCKET = "avatars"
    }
}
