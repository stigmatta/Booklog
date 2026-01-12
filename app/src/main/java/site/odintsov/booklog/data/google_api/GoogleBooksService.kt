package site.odintsov.booklog.data.google_api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import site.odintsov.booklog.data.google_api.models.GoogleBooksResponse


interface GoogleBooksApi {
    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("orderBy") orderBy: String = "relevance",
        @Query("maxResults") maxResults: Int = 20,
        @Query("printType") printType: String = "books",
        @Query("langRestrict") langRestrict: String? = null
    ): GoogleBooksResponse

    companion object {
        private const val BASE_URL = "https://www.googleapis.com/books/v1/"

        fun create(): GoogleBooksApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GoogleBooksApi::class.java)
        }
    }
}