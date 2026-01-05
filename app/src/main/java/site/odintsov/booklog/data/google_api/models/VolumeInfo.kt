package site.odintsov.booklog.data.google_api.models

data class VolumeInfo(
    val title: String,
    val authors: List<String>,
    val pageCount: Int?,
    val imageLinks: ImageLinks?,
    val description: String
)

data class ImageLinks(val thumbnail: String)
