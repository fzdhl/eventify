package com.example.tugasakhirpam.data

import com.example.tugasakhirpam.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Satu baris tabel public.profiles. Email tidak disimpan di sini, email milik auth.users. */
@Serializable
internal data class ProfileDto(
    val id: String,
    @SerialName("full_name") val fullName: String = "",
    val nim: String = "",
    val phone: String? = null,
    @SerialName("study_program") val studyProgram: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
) {
    fun toUser(email: String) = User(
        id = id,
        name = fullName,
        nim = nim,
        email = email,
        phone = phone.orEmpty(),
        studyProgram = studyProgram.orEmpty(),
        avatarUrl = avatarUrl,
        updatedAt = updatedAt,
    )
}
