package site.odintsov.booklog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import site.odintsov.booklog.R
import site.odintsov.booklog.data.Book

@Composable
fun BookReviews(
    book: Book,
    reviews: List<Map<String, Any>>,
    onAddReview: (Int, String) -> Unit
) {
    var newRating by remember { mutableIntStateOf(0) }
    var newComment by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = stringResource(R.string.rating_summary),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (book.ratingsCount > 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = String.format("%.1f", book.averageRating),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    StaticRatingBar(rating = book.averageRating.toFloat())
                    Text(
                        text = stringResource(R.string.based_on_ratings, book.ratingsCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Text(
                text = stringResource(R.string.no_reviews),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Write a Review",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Row {
            repeat(5) { index ->
                val isSelected = index < newRating
                androidx.compose.material3.IconButton(onClick = { newRating = index + 1 }) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        tint = if (isSelected) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        OutlinedTextField(
            value = newComment,
            onValueChange = { newComment = it },
            label = { Text("Your comment") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = {
                if (newRating > 0 && newComment.isNotBlank()) {
                    onAddReview(newRating, newComment)
                    newRating = 0
                    newComment = ""
                }
            },
            modifier = Modifier.align(Alignment.End),
            enabled = newRating > 0 && newComment.isNotBlank()
        ) {
            Text("Submit")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (reviews.isNotEmpty()) {
            Text(
                text = stringResource(R.string.user_reviews),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            reviews.forEach { review ->
                ReviewItem(review)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ReviewItem(review: Map<String, Any>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            val photoUrl = review["userPhotoUrl"] as? String
            
            if (!photoUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = review["userName"] as? String ?: "Anonymous",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                
                // Use Number to safely handle Int/Long types from Map
                StaticRatingBar(rating = (review["rating"] as? Number)?.toFloat() ?: 0f)
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = review["comment"] as? String ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun StaticRatingBar(rating: Float) {
    Row {
        repeat(5) { index ->
            val isSelected = index < rating.toInt()
            val isHalf = index == rating.toInt() && rating % 1 >= 0.5
            
            val icon = when {
                isSelected -> Icons.Filled.Star
                isHalf -> Icons.Filled.StarHalf
                else -> Icons.Outlined.Star
            }
            
            // Only tint filled or half stars with Gold. Empty stars use a lighter color.
            val tint = if (isSelected || isHalf) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
