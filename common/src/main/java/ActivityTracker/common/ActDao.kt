package ActivityTracker.common

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ActDao {

    @Query("SELECT * FROM activities ORDER BY timestamp DESC")
    suspend fun getAllActs(): List<Act>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(act: Act)
}