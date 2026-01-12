package site.odintsov.booklog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.LaunchedEffect
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
import site.odintsov.booklog.data.AuthRepository
import site.odintsov.booklog.data.BookRepository
import site.odintsov.booklog.data.local.AppDatabase
import site.odintsov.booklog.ui.AuthViewModel
import site.odintsov.booklog.ui.BookViewModel
import site.odintsov.booklog.ui.screens.BookDetailScreen
import site.odintsov.booklog.ui.screens.BookScreen
import site.odintsov.booklog.ui.screens.LibraryScreen
import site.odintsov.booklog.ui.screens.LoginScreen
import site.odintsov.booklog.ui.screens.ProfileScreen
import site.odintsov.booklog.ui.screens.SignUpScreen

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val repository = BookRepository(database.bookDao())
        val authRepository = AuthRepository(applicationContext)
        val app = application

        val viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BookViewModel(app, repository) as T
            }
        })[BookViewModel::class.java]

        val authViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(authRepository) as T
            }
        })[AuthViewModel::class.java]

        viewModel.getPopularBooks()

        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemDark) }

            val colors = if (isDarkTheme) darkColorScheme() else lightColorScheme()
            MaterialTheme(colorScheme = colors) {
                val navController = rememberNavController()
                val isLoggedIn = authRepository.currentUser != null
                val startDest = if (isLoggedIn) "book_list" else "login"

                LaunchedEffect(Unit) {
                    if (isLoggedIn) {
                        viewModel.syncLibrary()
                    }
                }

                Surface(color = MaterialTheme.colorScheme.background) {
                    SharedTransitionLayout {
                        NavHost(navController = navController, startDestination = startDest) {
                            composable("login") {
                                LoginScreen(
                                    viewModel = authViewModel,
                                    onLoginSuccess = {
                                        viewModel.syncLibrary()
                                        navController.navigate("book_list") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    },
                                    onNavigateToSignup = {
                                        navController.navigate("signup")
                                    }
                                )
                            }

                            composable("signup") {
                                SignUpScreen(
                                    viewModel = authViewModel,
                                    onSignupSuccess = {
                                        viewModel.syncLibrary()
                                        navController.navigate("book_list") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    },
                                    onNavigateToLogin = {
                                        navController.navigate("login") {
                                            popUpTo("signup") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable("book_list") {
                                BookScreen(
                                    viewModel = viewModel,
                                    onBookClick = { book ->
                                        navController.navigate("book_details/${book.id}")
                                    },
                                    isDarkTheme = isDarkTheme,
                                    onThemeToggle = { isDarkTheme = it },
                                    onProfileClick = {
                                        navController.navigate("profile")
                                    },
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = this@composable
                                )
                            }

                            composable(
                                route = "book_details/{bookId}",
                                arguments = listOf(navArgument("bookId") {
                                    type = NavType.StringType
                                })
                            ) { backStackEntry ->
                                val bookId = backStackEntry.arguments?.getString("bookId")

                                val discoverBooks by viewModel.allBooks.observeAsState(initial = emptyList())
                                val libraryBooks by viewModel.libraryBooks.observeAsState(initial = emptyList())

                                val book = libraryBooks.find { it.id == bookId }
                                    ?: discoverBooks.find { it.id == bookId }

                                BookDetailScreen(
                                    book = book,
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() },
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = this@composable
                                )
                            }

                            composable("library") {
                                LibraryScreen(
                                    viewModel = viewModel,
                                    onBookClick = { book -> navController.navigate("book_details/${book.id}") },
                                    onBack = { navController.popBackStack() },
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = this@composable
                                )
                            }

                            composable("profile") {
                                ProfileScreen(
                                    viewModel = viewModel,
                                    onNavigateToLibrary = {
                                        navController.navigate("library")
                                    },
                                    onBack = { navController.popBackStack() },
                                    authViewModel = authViewModel,
                                    onLogout = {
                                        authViewModel.logout()
                                        navController.navigate("login") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    },
                                    onBookClick = { bookId ->
                                        navController.navigate("book_details/$bookId")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
