package site.odintsov.booklog.data.google_api.models

data class VolumeInfo(
    val title: String,
    val authors: List<String>?,
    val pageCount: Int?,
    val imageLinks: ImageLinks?,
    val description: String?,
    val averageRating: Double?,
    val ratingsCount: Int?,
    val industryIdentifiers: List<IndustryIdentifier>?
)

data class ImageLinks(val thumbnail: String)

data class IndustryIdentifier(
    val type: String,
    val identifier: String
)
