package com.example.tugasakhirpam.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.tugasakhirpam.ui.theme.StarEmpty
import com.example.tugasakhirpam.ui.theme.StarYellow
import kotlin.math.roundToInt

@Composable
fun RatingStars(
    rating: Double,
    modifier: Modifier = Modifier,
    starSize: Dp = 14.dp,
    emptyColor: Color = StarEmpty,
) {
    val filled = rating.roundToInt()
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (index in 1..5) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = if (index <= filled) StarYellow else emptyColor,
                modifier = Modifier.size(starSize)
            )
        }
    }
}

@Composable
fun RatingInput(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    starSize: Dp = 40.dp,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (index in 1..5) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onRatingChange(index) }
                    .padding(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "$index bintang",
                    tint = if (index <= rating) StarYellow else StarEmpty,
                    modifier = Modifier.size(starSize)
                )
            }
        }
    }
}

fun ratingLabel(rating: Int): String = when (rating) {
    1 -> "Sangat buruk"
    2 -> "Kurang"
    3 -> "Cukup"
    4 -> "Bagus"
    5 -> "Sangat bagus!"
    else -> "Ketuk bintang untuk memberi nilai"
}
