package site.odintsov.booklog.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import site.odintsov.booklog.R
import kotlin.math.roundToInt

@Composable
fun StatusSection(
    initialStatus: Int,
    initialProgress: Float,
    initialRating: Int,
    isInLibrary: Boolean,
    onSave: (Int, Float, Int) -> Unit,
    onDelete: () -> Unit
) {
    var statusIndex by remember { mutableIntStateOf(initialStatus) }
    var progress by remember { mutableFloatStateOf(initialProgress) }
    var rating by remember { mutableIntStateOf(initialRating) }

    val isRead = statusIndex == 2

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(50)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusSegment(
                    text = stringResource(R.string.status_wishlist),
                    icon = Icons.Default.FavoriteBorder,
                    isSelected = statusIndex == 1,
                    onClick = {
                        statusIndex = 1
                        progress = 0f
                    },
                    modifier = Modifier.weight(1f)
                )

                VerticalDivider(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight(0.6f),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                StatusSegment(
                    text = stringResource(R.string.status_isRead),
                    icon = Icons.Default.CheckCircle,
                    isSelected = statusIndex == 2,
                    onClick = {
                        statusIndex = 2
                        progress = 1f
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.your_rating),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(5) { index ->
                    val isSelected = index < rating
                    Icon(
                        imageVector = if (isSelected) Icons.Default.Star else Icons.Rounded.StarOutline,
                        contentDescription = null,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .clickable { rating = index + 1 },
                        tint = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(text = stringResource(R.string.reading_progress))
                Text(
                    text = if (isRead) "100%" else "${(progress * 100).roundToInt()}%",
                    color = if (isRead) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Slider(
                value = if (isRead) 1f else progress,
                onValueChange = {
                    if (!isRead) {
                        progress = it
                        statusIndex = if (it > 0f) 3 else 1
                    }
                },
                enabled = !isRead,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onSave(statusIndex, progress, rating) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(12.dp)
            ) {
                Text(text = stringResource(R.string.btn_add_to_library))
            }

            if (isInLibrary) {
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = stringResource(R.string.btn_remove_from_library))
                }
            }
        }
    }
}
