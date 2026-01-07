package site.odintsov.booklog.ui

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
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


    private val _isLoading = mutableStateOf(false)

    val isLoading: State<Boolean> = _isLoading

    fun onGenreSelected(genre: String) {
        _selectedGenre.value = genre
        getPopularBooks("subject:${genre.lowercase()}")
    }

    fun getPopularBooks(query: String = "subject:${_selectedGenre.value.lowercase()}") =
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.fetchPopularBooks(query)
            } finally {
                _isLoading.value = false
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
}