package site.odintsov.booklog.data

import GoogleBooksApi
import androidx.lifecycle.LiveData
import site.odintsov.booklog.data.local.BookDao

class BookRepository(private val bookDao: BookDao) {
    val allBooks: LiveData<List<Book>> = bookDao.getDiscoverBooks()
    val libraryBooks: LiveData<List<Book>> = bookDao.getLibraryBooks()
    private val api = GoogleBooksApi.create()

    suspend fun resetLibrary() {
        bookDao.resetAllBooks()
    }

    suspend fun fetchPopularBooks(query: String) {
        try {
            resetLibrary()
            bookDao.clearSearchCache()
            val response = api.searchBooks(query = query, orderBy = "relevance")

            response.items?.forEach { item ->
                val existingBook = bookDao.getBookSync(item.id)

                if (existingBook == null) {
                    val book = Book(
                        id = item.id,
                        title = item.volumeInfo.title,
                        author = item.volumeInfo.authors.firstOrNull() ?: "Unknown",
                        pages = item.volumeInfo.pageCount ?: 0,
                        imageUrl = item.volumeInfo.imageLinks?.thumbnail?.replace(
                            "http:",
                            "https:"
                        ),
                        description = item.volumeInfo.description,
                        status = 0,
                        isInLibrary = false
                    )
                    bookDao.insertBook(book)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun insert(book: Book) {
        bookDao.insertBook(book)
    }

    suspend fun delete(book: Book) {
        bookDao.deleteBook(book)
    }

    fun getLibraryBooks() {
        bookDao.getLibraryBooks()
    }

    fun getBookById(id: String): LiveData<Book?> = bookDao.getBookById(id)
    suspend fun saveBookToLibrary(book: Book) {
        bookDao.upsertBook(book)
    }


}