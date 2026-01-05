package site.odintsov.booklog.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val author: String,
    val pages: Int,
    val isRead: Boolean = false,
    val imageUrl: String? = null,
    val description: String?
)