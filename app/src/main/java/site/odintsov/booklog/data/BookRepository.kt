package site.odintsov.booklog.data

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import site.odintsov.booklog.data.google_api.GoogleBooksApi
import site.odintsov.booklog.data.local.BookDao
import site.odintsov.booklog.data.openlibrary_api.OpenLibraryApi
import java.util.Date

class BookRepository(private val bookDao: BookDao) {
    val allBooks: LiveData<List<Book>> = bookDao.getDiscoverBooks()
    val libraryBooks: LiveData<List<Book>> = bookDao.getLibraryBooks()
    private val googleApi = GoogleBooksApi.create()
    private val openLibraryApi = OpenLibraryApi.create()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getSystemLanguage(): String {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        if (!appLocales.isEmpty) {
            return appLocales[0]?.language ?: "en"
        }

        return "en"
    }

    suspend fun fetchPopularBooks(query: String) {

        val currentLang = getSystemLanguage()
        withContext(Dispatchers.IO) {
            try {
                bookDao.invalidateSearchResults()
                
                val response = googleApi.searchBooks(
                    query = query,
                    orderBy = "relevance",
                    lang = currentLang,
                    interfaceLang = currentLang
                )

                val items = response.items ?: emptyList()
                val itemIds = items.map { it.id }

                bookDao.markAsSearchResults(itemIds)

                val initialBooks = items.map { item ->
                    val googleRating = item.volumeInfo.averageRating
                    val googleCount = item.volumeInfo.ratingsCount ?: 0
                    
                    val isbn = item.volumeInfo.industryIdentifiers?.firstOrNull { 
                        it.type == "ISBN_13" 
                    }?.identifier ?: item.volumeInfo.industryIdentifiers?.firstOrNull { 
                        it.type == "ISBN_10" 
                    }?.identifier

                    Book(
                        id = item.id,
                        title = item.volumeInfo.title,
                        author = item.volumeInfo.authors?.firstOrNull() ?: "Unknown",
                        pages = item.volumeInfo.pageCount ?: 0,
                        imageUrl = item.volumeInfo.imageLinks?.thumbnail?.replace(
                            "http:",
                            "https:"
                        ),
                        description = item.volumeInfo.description,
                        status = 0,
                        isInLibrary = false,
                        averageRating = googleRating ?: 0.0,
                        ratingsCount = googleCount,
                        isbn = isbn,
                        isSearchResult = true
                    )
                }

                bookDao.insertBooks(initialBooks)
                bookDao.cleanupOldSearchResults()

                val enrichmentJobs = initialBooks.map { book ->
                    async {
                        if (book.ratingsCount < 10 && book.isbn != null) {
                            try {
                                val olResponse = openLibraryApi.searchByIsbn(book.isbn)
                                val doc = olResponse.docs?.firstOrNull()
                                
                                if (doc != null && doc.ratings_count != null && doc.ratings_count > 0) {
                                    val currentBook = bookDao.getBookSync(book.id) ?: book
                                    val updatedBook = currentBook.copy(
                                        averageRating = doc.ratings_average ?: 0.0,
                                        ratingsCount = doc.ratings_count
                                    )
                                    bookDao.upsertBook(updatedBook)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
                
                enrichmentJobs.awaitAll()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun getLibraryBooks() {
        bookDao.getLibraryBooks()
    }

    suspend fun saveBookToLibrary(book: Book) {
        bookDao.upsertBook(book)
        val userId = auth.currentUser?.uid
        if (userId != null) {
            try {
                firestore.collection("users")
                    .document(userId)
                    .collection("books")
                    .document(book.id)
                    .set(book, SetOptions.merge())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun syncLibraryFromFirestore() {
        val userId = auth.currentUser?.uid ?: return
        withContext(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("users")
                    .document(userId)
                    .collection("books")
                    .get()
                    .await()
                
                val books = snapshot.toObjects(Book::class.java)
                books.forEach { book ->
                    val validBook = book.copy(isInLibrary = true)
                    bookDao.upsertBook(validBook)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Reviews Section
    
    suspend fun addReview(book: Book, rating: Int, comment: String) {
        val user = auth.currentUser ?: return
        
        // Ensure we have the latest profile data (photoUrl)
        try {
            user.reload().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        val review = hashMapOf(
            "bookId" to book.id,
            "bookTitle" to book.title,
            "bookAuthor" to book.author,
            "bookImage" to (book.imageUrl ?: ""),
            "userId" to user.uid,
            "userName" to (user.displayName ?: "Anonymous"),
            "userPhotoUrl" to (user.photoUrl?.toString() ?: ""),
            "rating" to rating,
            "comment" to comment,
            "timestamp" to Date()
        )
        
        try {
            firestore.collection("reviews").add(review).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun deleteReview(reviewId: String) {
        try {
            firestore.collection("reviews").document(reviewId).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun getReviews(bookId: String): List<Map<String, Any>> {
        return try {
            val snapshot = firestore.collection("reviews")
                .whereEqualTo("bookId", bookId)
                .get()
                .await()
            
            snapshot.documents
                .map { doc ->
                    val data = doc.data ?: mutableMapOf()
                    data["id"] = doc.id
                    data
                }
                .sortedByDescending { (it["timestamp"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(0) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getUserReviews(userId: String): List<Map<String, Any>> {
        return try {
            val snapshot = firestore.collection("reviews")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            snapshot.documents
                .map { doc ->
                    val data = doc.data ?: mutableMapOf()
                    data["id"] = doc.id
                    data
                }
                .sortedByDescending { (it["timestamp"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(0) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
