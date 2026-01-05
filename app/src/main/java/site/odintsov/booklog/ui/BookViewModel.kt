package site.odintsov.booklog.ui

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import site.odintsov.booklog.R
import site.odintsov.booklog.data.Book
import site.odintsov.booklog.data.BookRepository

class BookViewModel(application: Application, private val repository: BookRepository) :
    AndroidViewModel(application) {

    val genres: List<String> = application.resources.getStringArray(R.array.book_genres).toList()
    val allBooks: LiveData<List<Book>> = repository.allBooks

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

    fun insert(book: Book) = viewModelScope.launch {
        repository.insert(book)
    }

    fun delete(book: Book) = viewModelScope.launch {
        repository.delete(book)
    }
}