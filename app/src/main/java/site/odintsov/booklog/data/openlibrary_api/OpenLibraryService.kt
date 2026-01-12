package site.odintsov.booklog.data.openlibrary_api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenLibraryApi {
    @GET("search.json")
    suspend fun searchByIsbn(
        @Query("isbn") isbn: String
    ): OpenLibrarySearchResponse

    companion object {
        private const val BASE_URL = "https://openlibrary.org/"

        fun create(): OpenLibraryApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OpenLibraryApi::class.java)
        }
    }
}

data class OpenLibrarySearchResponse(
    val docs: List<OpenLibraryDoc>?
)

data class OpenLibraryDoc(
    val ratings_average: Double?,
    val ratings_count: Int?
)
