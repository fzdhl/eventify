package com.example.tugasakhirpam.data

import com.example.tugasakhirpam.model.Category
import com.example.tugasakhirpam.model.CategoryInUseException
import com.example.tugasakhirpam.model.CategoryInput
import com.example.tugasakhirpam.model.DuplicateCategoryNameException
import com.example.tugasakhirpam.model.Event

interface CategoryRepository {
    suspend fun getCategories(): List<Category>
    suspend fun getCategory(id: Long): Category
    suspend fun getEventsByCategory(categoryId: Long): List<Event>
    suspend fun createCategory(input: CategoryInput): Long
    suspend fun updateCategory(id: Long, input: CategoryInput)
    suspend fun deleteCategory(id: Long)
}

class FakeCategoryRepository : CategoryRepository {

    private val db = FakeDataSource

    private fun withCount(c: Category): Category =
        c.copy(eventCount = db.events.count { it.categoryId == c.id })

    private fun find(id: Long): Category =
        db.categories.firstOrNull { it.id == id } ?: throw NoSuchElementException("Kategori tidak ditemukan.")

    private fun ensureUnique(name: String, excludeId: Long?) {
        val taken = db.categories.any { it.id != excludeId && it.name.trim().equals(name.trim(), ignoreCase = true) }
        if (taken) throw DuplicateCategoryNameException()
    }

    override suspend fun getCategories(): List<Category> = db.transaction {
        db.categories.sortedBy { it.id }.map(::withCount)
    }

    override suspend fun getCategory(id: Long): Category = db.transaction { withCount(find(id)) }

    override suspend fun getEventsByCategory(categoryId: Long): List<Event> = db.transaction {
        find(categoryId)
        db.events.filter { it.categoryId == categoryId }.map(db::withState).sortedBy { it.eventDate }
    }

    override suspend fun createCategory(input: CategoryInput): Long = db.transaction {
        ensureUnique(input.name, excludeId = null)
        val id = db.nextCategoryId++
        db.categories += Category(id = id, name = input.name.trim(), description = input.description.trim())
        id
    }

    override suspend fun updateCategory(id: Long, input: CategoryInput) = db.transaction {
        val old = find(id)
        ensureUnique(input.name, excludeId = id)
        db.categories[db.categories.indexOf(old)] =
            old.copy(name = input.name.trim(), description = input.description.trim())
        Unit
    }

    override suspend fun deleteCategory(id: Long) = db.transaction {
        val old = find(id)
        val used = db.events.count { it.categoryId == id }
        if (used > 0) throw CategoryInUseException(used)
        db.categories.remove(old)
        Unit
    }
}
