package site.odintsov.booklog.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val pages: Int,
    val imageUrl: String? = null,
    val description: String?,
    val status: Int = 0, // 0: Not Read, 1: Wishlist, 2: Reading, 3: Read
    val readingProgress: Float = 0f,
    val rating: Int = 0,
    val isInLibrary: Boolean = false // Only true for statuses 1, 2, and 3
)