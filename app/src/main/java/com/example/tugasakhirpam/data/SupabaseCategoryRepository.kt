package com.example.tugasakhirpam.data

import com.example.tugasakhirpam.model.Category
import com.example.tugasakhirpam.model.CategoryInUseException
import com.example.tugasakhirpam.model.CategoryInput
import com.example.tugasakhirpam.model.DuplicateCategoryNameException
import com.example.tugasakhirpam.model.Event
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SupabaseCategoryRepository(
    private val client: SupabaseClient,
    private val eventRepository: EventRepository,
) : CategoryRepository {

    override suspend fun getCategories(): List<Category> {
        val categories = client.from("categories")
            .select()
            .decodeList<CategoryDto>()

        // Ambil category_id dari events untuk menghitung jumlah event per kategori.
        val eventRefs = client.from("events")
            .select()
            .decodeList<EventCategoryRefDto>()

        val eventCounts = eventRefs
            .groupingBy { it.categoryId }
            .eachCount()

        return categories.map { dto ->
            dto.toCategory(eventCounts[dto.id] ?: 0)
        }.sortedBy { it.id }
    }

    override suspend fun getCategory(id: Long): Category {
        val dto = client.from("categories")
            .select {
                filter {
                    eq("id", id)
                }
            }
            .decodeSingleOrNull<CategoryDto>()
            ?: throw NoSuchElementException("Kategori tidak ditemukan.")

        val eventCount = getEventCount(id)
        return dto.toCategory(eventCount)
    }

    /**
     * Sementara menggunakan EventRepository yang sedang aktif.
     * Setelah EventRepository diganti ke Supabase, method ini otomatis
     * mengambil event dari Supabase juga.
     */
    override suspend fun getEventsByCategory(categoryId: Long): List<Event> {
        getCategory(categoryId)

        return eventRepository.getEvents()
            .filter { it.categoryId == categoryId }
            .sortedBy { it.eventDate }
    }

    override suspend fun createCategory(input: CategoryInput): Long {
        try {
            return client.from("categories")
                .insert(
                    CategoryInsertDto(
                        name = input.name.trim(),
                        description = input.description.trim(),
                    )
                ) {
                    select()
                }
                .decodeSingle<CategoryIdDto>()
                .id
        } catch (e: Exception) {
            if (e.isDuplicateKeyError()) {
                throw DuplicateCategoryNameException()
            }
            throw e
        }
    }

    override suspend fun updateCategory(id: Long, input: CategoryInput) {
        try {
            val updated = client.from("categories")
                .update({
                    set("name", input.name.trim())
                    set("description", input.description.trim())
                }) {
                    filter {
                        eq("id", id)
                    }
                    select()
                }
                .decodeSingleOrNull<CategoryIdDto>()

            if (updated == null) {
                throw NoSuchElementException("Kategori tidak ditemukan.")
            }
        } catch (e: Exception) {
            if (e.isDuplicateKeyError()) {
                throw DuplicateCategoryNameException()
            }
            throw e
        }
    }

    override suspend fun deleteCategory(id: Long) {
        // Periksa kategori terlebih dahulu agar pesan error tetap jelas.
        getCategory(id)

        val eventCount = getEventCount(id)
        if (eventCount > 0) {
            throw CategoryInUseException(eventCount)
        }

        try {
            val deleted = client.from("categories")
                .delete {
                    filter {
                        eq("id", id)
                    }
                    select()
                }
                .decodeList<CategoryIdDto>()

            if (deleted.isEmpty()) {
                throw NoSuchElementException("Kategori tidak ditemukan.")
            }
        } catch (e: Exception) {
            if (e.isForeignKeyError()) {
                val latestCount = getEventCount(id)
                throw CategoryInUseException(latestCount.coerceAtLeast(1))
            }
            throw e
        }
    }

    private suspend fun getEventCount(categoryId: Long): Int {
        return client.from("events")
            .select {
                filter {
                    eq("category_id", categoryId)
                }
            }
            .decodeList<EventCategoryRefDto>()
            .size
    }

    private fun Exception.isDuplicateKeyError(): Boolean {
        val detail = message.orEmpty()
        return detail.contains("23505") ||
                detail.contains("duplicate key", ignoreCase = true)
    }

    private fun Exception.isForeignKeyError(): Boolean {
        val detail = message.orEmpty()
        return detail.contains("23503") ||
                detail.contains("foreign key", ignoreCase = true)
    }
}

@Serializable
internal data class CategoryDto(
    val id: Long,
    val name: String,
    val description: String = "",
    @SerialName("icon_key")
    val iconKey: String = "apps",
)

@Serializable
internal data class CategoryInsertDto(
    val name: String,
    val description: String,
    @SerialName("icon_key")
    val iconKey: String = "apps",
)

@Serializable
internal data class CategoryIdDto(
    val id: Long,
)

@Serializable
internal data class EventCategoryRefDto(
    @SerialName("category_id")
    val categoryId: Long,
)

private fun CategoryDto.toCategory(eventCount: Int) = Category(
    id = id,
    name = name,
    description = description,
    iconKey = iconKey,
    eventCount = eventCount,
)
