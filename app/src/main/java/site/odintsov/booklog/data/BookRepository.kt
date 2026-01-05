package site.odintsov.booklog.data

import GoogleBooksApi
import androidx.lifecycle.LiveData
import site.odintsov.booklog.data.local.BookDao

class BookRepository(private val bookDao: BookDao) {
    val allBooks: LiveData<List<Book>> = bookDao.getAllBooks()
    private val api = GoogleBooksApi.create()

    suspend fun fetchPopularBooks(query: String) {
        try {
            bookDao.deleteAllBooks()

            val response = api.searchBooks(query = query, orderBy = "relevance")

            response.items?.forEach { item ->
                val book = Book(
                    title = item.volumeInfo.title,
                    author = item.volumeInfo.authors.first(),
                    pages = item.volumeInfo.pageCount ?: 0,
                    imageUrl = item.volumeInfo.imageLinks?.thumbnail?.replace("http:", "https:"),
                    description = item.volumeInfo.description
                )
                bookDao.insertBook(book)
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
}