package ActivityTracker.common

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Entity(tableName = "activities")
data class Act (
    val label: String,
    val timestamp: Long,
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
) {
    override fun toString(): String {
        return "Activity('$label': ${timeString()})"
    }

    fun timeString(): String {
        val instant = Instant.ofEpochMilli(timestamp)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss MM/dd/yy")
        return dateTime.format(formatter)
    }
}
