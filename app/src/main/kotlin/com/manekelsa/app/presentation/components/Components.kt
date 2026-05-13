package com.manekelsa.app.presentation.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.manekelsa.app.domain.model.Worker
import com.manekelsa.app.presentation.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerCard(
    worker: Worker,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {

            // Avatar with initials
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(AppColors.Saffron.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = worker.name.take(2).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppColors.Saffron
                )
            }

            // Availability dot
            Box(
                modifier = Modifier
                    .offset(x = (-10).dp, y = 22.dp)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(if (worker.isAvailable) AppColors.Available else AppColors.Unavailable)
            )

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        worker.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (worker.identityVerified) {
                        Icon(
                            Icons.Filled.Verified, null,
                            tint = AppColors.Verified,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    worker.primarySkill(),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Saffron,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Filled.Star, null, tint = AppColors.StarYellow, modifier = Modifier.size(13.dp))
                    Text(worker.toDisplayRating(), style = MaterialTheme.typography.labelSmall)
                    Text("·", color = AppColors.MidGray, style = MaterialTheme.typography.labelSmall)
                    Icon(Icons.Outlined.LocationOn, null, tint = AppColors.MidGray, modifier = Modifier.size(12.dp))
                    Text(worker.city, style = MaterialTheme.typography.labelSmall, color = AppColors.MidGray)
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (worker.dailyRate > 0) "₹${worker.dailyRate.toInt()}/day"
                        else "Negotiable",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Saffron
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = (if (worker.isAvailable) AppColors.Available else AppColors.Unavailable).copy(alpha = 0.12f)
                    ) {
                        Text(
                            if (worker.isAvailable) "Available" else "Busy",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (worker.isAvailable) AppColors.Available else AppColors.Unavailable,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RatingBar(
    rating: Float,
    starSize: Int = 24,
    onRatingChange: ((Float) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        (1..5).forEach { star ->
            Icon(
                imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = if (star <= rating) AppColors.StarYellow else AppColors.LightGray,
                modifier = Modifier
                    .size(starSize.dp)
                    .let { m -> if (onRatingChange != null) m.clickable { onRatingChange(star.toFloat()) } else m }
            )
        }
    }
}

@Composable
fun SkillChip(skill: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            skill,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
