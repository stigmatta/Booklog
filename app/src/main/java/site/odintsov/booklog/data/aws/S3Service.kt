package site.odintsov.booklog.data.aws

import android.util.Log
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class S3Service {
    // Ensure your bucket policy allows "s3:PutObject" for Principal "*"
    private val bucketName = "booklog-kotlin-bucket"
    // UPDATED: Correct region from error message is us-east-2
    private val region = "us-east-2" 

    fun uploadFile(inputStream: InputStream, contentType: String): String {
        val fileName = "profile_images/${UUID.randomUUID()}.jpg"
        // Updated URL structure to use the specific region endpoint
        val urlString = "https://$bucketName.s3.$region.amazonaws.com/$fileName"
        
        Log.d("S3Service", "Starting upload to: $urlString")
        
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        
        try {
            connection.doOutput = true
            connection.requestMethod = "PUT"
            connection.setRequestProperty("Content-Type", contentType)
            
            connection.outputStream.use { output ->
                inputStream.copyTo(output)
            }
            
            val responseCode = connection.responseCode
            Log.d("S3Service", "Response Code: $responseCode")
            
            if (responseCode !in 200..299) {
                // Read error stream for debugging
                val errorStream = connection.errorStream ?: connection.inputStream
                val error = errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown Error"
                Log.e("S3Service", "Upload failed: $error")
                throw Exception("S3 Upload failed with code $responseCode: $error")
            }
            
            return urlString
        } catch (e: Exception) {
            Log.e("S3Service", "Exception during upload", e)
            throw e
        } finally {
            connection.disconnect()
        }
    }
}
