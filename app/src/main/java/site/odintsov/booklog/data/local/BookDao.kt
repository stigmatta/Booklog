package site.odintsov.booklog.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import site.odintsov.booklog.data.Book

@Dao
interface BookDao {
    @Query("SELECT * FROM books WHERE isInLibrary = 0")
    fun getDiscoverBooks(): LiveData<List<Book>>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    suspend fun getBookSync(id: String): Book?

    @Query("SELECT * FROM books WHERE id = :id")
    fun getBookById(id: String): LiveData<Book?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBook(book: Book)

    @Upsert
    suspend fun upsertBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)

    @Query("DELETE FROM books WHERE isInLibrary = 0")
    suspend fun clearSearchCache()

    @Query("UPDATE books SET isInLibrary = 0, status = 0, readingProgress = 0.0, rating = 0")
    suspend fun resetAllBooks()

    @Query("SELECT * FROM books WHERE isInLibrary = 1")
    fun getLibraryBooks(): LiveData<List<Book>>
}
