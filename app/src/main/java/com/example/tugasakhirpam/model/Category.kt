package com.example.tugasakhirpam.model

data class Category(
    val id: Long,
    val name: String,
    val description: String = "",
    val iconKey: String = "apps",
    val eventCount: Int = 0,
)

data class CategoryInput(
    val name: String,
    val description: String,
)

/** Dilempar repository saat nama kategori sudah dipakai. Nanti dipetakan dari unique constraint Supabase. */
class DuplicateCategoryNameException : Exception("Nama kategori sudah digunakan")

/** Dilempar saat kategori masih dipakai event. Setara FK ON DELETE RESTRICT di Supabase. */
class CategoryInUseException(val eventCount: Int) :
    Exception("Kategori masih dipakai $eventCount event. Pindahkan atau hapus event tersebut terlebih dahulu.")
