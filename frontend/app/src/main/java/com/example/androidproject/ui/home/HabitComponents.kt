package com.example.androidproject

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.androidproject.network.models.HabitResponse
import com.example.androidproject.network.models.ScheduleResponse
import com.example.androidproject.utils.DateTimeUtils

fun categoryColor(name: String?): Color = when (name?.lowercase()) {
    "exercise" -> Color(0xFFEF6C57)
    "reading" -> Color(0xFF6C8EF5)
    "study" -> Color(0xFF9C7BEA)
    "hydration" -> Color(0xFF3BB8D6)
    "writing" -> Color(0xFFE5A93C)
    "running" -> Color(0xFF4CAF7A)
    else -> Color(0xFF8E9AAF)
}

fun categoryEmoji(name: String?): String = when (name?.lowercase()) {
    "exercise" -> "🏋️"
    "reading" -> "📚"
    "study" -> "🎓"
    "hydration" -> "💧"
    "writing" -> "✍️"
    "running" -> "🏃"
    else -> "⭐"
}

@Composable
fun SectionHeader(title: String, count: Int? = null, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (count != null) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(count.toString(), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun StatusChip(status: String?) {
    val (label, color) = when (status?.lowercase()) {
        "completed", "done" -> "Done" to Color(0xFF4CAF7A)
        "missed", "skipped" -> "Missed" to Color(0xFFEF6C57)
        else -> "Planned" to Color(0xFF6C8EF5)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CategoryBadge(categoryName: String?, size: Int = 44) {
    val accent = categoryColor(categoryName)
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Text(categoryEmoji(categoryName), style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun ScheduleCard(schedule: ScheduleResponse, modifier: Modifier = Modifier) {
    val habit = schedule.habit
    val categoryName = habit?.category?.name ?: "Other"
    val accent = categoryColor(categoryName)
    val start = DateTimeUtils.formatTime(schedule.startTime).ifBlank { "--:--" }

    ElevatedCard(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.width(58.dp)) {
                Text(
                    start,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
                Text(
                    "${schedule.durationMinutes ?: 0} min",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            CategoryBadge(categoryName)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    habit?.name ?: "Unknown habit",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    categoryName,
                    style = MaterialTheme.typography.labelMedium,
                    color = accent
                )
                schedule.notes?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            StatusChip(schedule.status)
        }
    }
}

@Composable
fun HabitCard(habit: HabitResponse, modifier: Modifier = Modifier) {
    val accent = categoryColor(habit.category.name)
    ElevatedCard(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            CategoryBadge(habit.category.name, size = 48)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    habit.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(habit.category.name, style = MaterialTheme.typography.labelMedium, color = accent)
                habit.description?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "🎯 ${habit.goal}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun FormHeader(title: String, subtitle: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Column {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}