package site.odintsov.booklog.ui

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import site.odintsov.booklog.R
import site.odintsov.booklog.data.Book
import site.odintsov.booklog.data.BookRepository

class BookViewModel(application: Application, private val repository: BookRepository) :
    AndroidViewModel(application) {

    val genres: List<String> = application.resources.getStringArray(R.array.book_genres).toList()
    val languages = listOf("All" to null, "English" to "en", "Ukrainian" to "uk")

    val allBooks: LiveData<List<Book>> = repository.allBooks
    val libraryBooks: LiveData<List<Book>> = repository.libraryBooks

    val totalBooks = repository.libraryBooks.map { it.size }
    val readBooksCount = repository.libraryBooks.map { list -> list.count { it.status == 2 } }
    val wishlistCount = repository.libraryBooks.map { list -> list.count { it.status == 1 } }
    val readingCount = repository.libraryBooks.map { list -> list.count { it.status == 3 } }


    val averageRating: LiveData<Float> = repository.libraryBooks.map { list ->
        val readBooks = list.filter { it.rating > 0 }
        if (readBooks.isEmpty()) 0f
        else readBooks.map { it.rating }.average().toFloat()
    }

    val yearlyGoal = mutableIntStateOf(20)

    val goalProgress: LiveData<Float> = readBooksCount.map { count ->
        if (yearlyGoal.intValue > 0) count.toFloat() / yearlyGoal.intValue else 0f
    }

    private val _selectedGenre = mutableStateOf(genres.first())
    val selectedGenre: State<String> = _selectedGenre

    private val _selectedLanguage = mutableStateOf(languages.first())
    private val _isLoading = mutableStateOf(false)

    val isLoading: State<Boolean> = _isLoading
    
    // Reviews
    private val _reviews = mutableStateListOf<Map<String, Any>>()
    val reviews: List<Map<String, Any>> = _reviews
    
    // User Reviews (Profile Page)
    private val _userReviews = mutableStateListOf<Map<String, Any>>()
    val userReviews: List<Map<String, Any>> = _userReviews
    
    // Combined Ratings State
    private val _combinedRatings = mutableStateOf<Map<String, Pair<Double, Int>>>(emptyMap())

    fun onGenreSelected(genre: String) {
        _selectedGenre.value = genre
        getPopularBooks(genre)
    }

    fun getPopularBooks(genre: String? = null) {
        val genreToUse = genre ?: _selectedGenre.value
        val mappedGenre = mapGenreToApiQuery(genreToUse)
        val finalQuery = "subject:${mappedGenre.lowercase()}"

        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.fetchPopularBooks(finalQuery)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getLibraryBooks() = viewModelScope.launch {
        repository.getLibraryBooks()
    }

    fun updateBookInLibrary(book: Book) {
        viewModelScope.launch {
            repository.saveBookToLibrary(book)
        }
    }
    
    fun syncLibrary() {
        viewModelScope.launch {
            repository.syncLibraryFromFirestore()
        }
    }
    
    fun loadReviews(book: Book) {
        viewModelScope.launch {
            val fetchedReviews = repository.getReviews(book.id)
            _reviews.clear()
            _reviews.addAll(fetchedReviews)
            
            // Calculate and cache combined rating
            val appRatings = fetchedReviews.mapNotNull { (it["rating"] as? Number)?.toDouble() }
            val appCount = appRatings.size
            
            if (appCount > 0) {
                val appSum = appRatings.sum()
                val externalSum = book.averageRating * book.ratingsCount
                val totalCount = book.ratingsCount + appCount
                val newAverage = (externalSum + appSum) / totalCount
                
                // Update map
                val currentMap = _combinedRatings.value.toMutableMap()
                currentMap[book.id] = newAverage to totalCount
                _combinedRatings.value = currentMap
            }
        }
    }
    
    fun loadUserReviews(userId: String) {
        viewModelScope.launch {
            val fetched = repository.getUserReviews(userId)
            _userReviews.clear()
            _userReviews.addAll(fetched)
        }
    }
    
    fun getCombinedRating(bookId: String): Pair<Double, Int>? {
        return _combinedRatings.value[bookId]
    }
    
    fun submitReview(book: Book, rating: Int, comment: String) {
        viewModelScope.launch {
            repository.addReview(book, rating, comment)
            loadReviews(book)
        }
    }
    
    fun deleteReview(review: Map<String, Any>) {
        val id = review["id"] as? String ?: return

        viewModelScope.launch {
            repository.deleteReview(id)
            
            // Update reviews list for current book if we are on details screen
            // Since we don't have the full Book object easily available here,
            // we rely on subsequent loadReviews call or just remove from list.
            // For correct combined ratings update, the UI should ideally trigger refresh.
            // Here we just remove from local lists for immediate feedback.
            _reviews.removeIf { it["id"] == id }
            _userReviews.removeIf { it["id"] == id }

        }
    }

    private fun mapGenreToApiQuery(localizedGenre: String): String {
        return when (localizedGenre) {
            "Художня література", "Художественная литература", "Fiction" -> "Fiction"
            "Фентезі", "Фэнтези", "Fantasy" -> "Fantasy"
            "Детектив", "Mystery" -> "Mystery"
            "Історія", "История", "History" -> "History"
            "Наука", "Science" -> "Science"
            "Класика", "Классика", "Classic" -> "Classics"
            "Комп’ютери", "Компьютеры", "Computers" -> "Computers"
            "Психологія", "Психология", "Psychology" -> "Psychology"
            "Бізнес", "Бизнес", "Business" -> "Business"
            "Філософія", "Философия", "Philosophy" -> "Philosophy"
            "Мистецтво", "Искусство", "Art" -> "Art"
            "Подорожі", "Путешествия", "Travel" -> "Travel"
            "Біографія", "Биография", "Biography" -> "Biography"
            "Кулінарія", "Кулинария", "Cooking" -> "Cooking"

            else -> localizedGenre
        }
    }
}
