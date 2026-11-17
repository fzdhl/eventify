package com.example.tugasakhirpam.data

import android.content.Context
import androidx.core.content.edit

/** Menyimpan pilihan "Ingat saya" di layar login. Hanya email yang diingat, password tidak pernah disimpan. */
class LoginPrefs(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

    /** Email yang diingat, atau null jika user tidak mencentang "Ingat saya". */
    fun rememberedEmail(): String? =
        prefs.getString(KEY_EMAIL, null)?.takeIf { prefs.getBoolean(KEY_REMEMBER, false) }

    fun save(remember: Boolean, email: String) {
        prefs.edit {
            putBoolean(KEY_REMEMBER, remember)
            if (remember) putString(KEY_EMAIL, email) else remove(KEY_EMAIL)
        }
    }

    private companion object {
        const val KEY_REMEMBER = "remember"
        const val KEY_EMAIL = "email"
    }
}
