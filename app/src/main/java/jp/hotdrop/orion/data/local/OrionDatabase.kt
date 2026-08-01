package jp.hotdrop.orion.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import jp.hotdrop.orion.data.settings.SettingsDao
import jp.hotdrop.orion.data.settings.SettingsEntity

@Database(
    entities = [SettingsEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class OrionDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao

    companion object {
        const val Name = "orion.db"
    }
}
