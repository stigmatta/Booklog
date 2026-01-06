package site.odintsov.booklog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import site.odintsov.booklog.data.BookRepository
import site.odintsov.booklog.data.local.AppDatabase
import site.odintsov.booklog.ui.BookViewModel
import site.odintsov.booklog.ui.screens.BookDetailScreen
import site.odintsov.booklog.ui.screens.BookScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val repository = BookRepository(database.bookDao())
        val app = application

        val viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BookViewModel(app, repository) as T
            }
        })[BookViewModel::class.java]

        viewModel.getPopularBooks()

        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemDark) }

            val colors = if (isDarkTheme) darkColorScheme() else lightColorScheme()
            MaterialTheme(colorScheme = colors) {
                val navController = rememberNavController()

                Surface(color = MaterialTheme.colorScheme.background) {
                    NavHost(navController = navController, startDestination = "book_list") {
                        composable("book_list") {
                            BookScreen(
                                viewModel = viewModel,
                                onBookClick = { book ->
                                    navController.navigate("book_details/${book.id}")
                                },
                                isDarkTheme = isDarkTheme,
                                onThemeToggle = { isDarkTheme = it },
                            )
                        }

                        composable(
                            route = "book_details/{bookId}",
                            arguments = listOf(navArgument("bookId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val bookId = backStackEntry.arguments?.getInt("bookId")
                            val book =
                                viewModel.allBooks.observeAsState().value?.find { it.id == bookId }

                            BookDetailScreen(
                                book = book,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}