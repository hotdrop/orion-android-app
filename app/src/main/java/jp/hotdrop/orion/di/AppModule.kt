package jp.hotdrop.orion.di

import jp.hotdrop.orion.data.local.dao.IncomingPersonalDao
import jp.hotdrop.orion.data.local.MIGRATION_1_2
import jp.hotdrop.orion.data.local.MIGRATION_2_3
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import jp.hotdrop.orion.data.local.OrionDatabase
import jp.hotdrop.orion.data.local.dao.IncomingIntelligenceDao
import jp.hotdrop.orion.data.local.dao.SettingsDao
import jp.hotdrop.orion.data.remote.GoogleDriveRemoteDataSource
import jp.hotdrop.orion.data.remote.HttpGoogleDriveRemoteDataSource

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): OrionDatabase =
        Room.databaseBuilder(
            context,
            OrionDatabase::class.java,
            OrionDatabase.DATABASE_NAME,
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()

    @Provides
    @Singleton
    fun provideSettingsDao(database: OrionDatabase): SettingsDao = database.settingsDao()

    @Provides
    @Singleton
    fun provideIncomingIntelligenceDao(database: OrionDatabase): IncomingIntelligenceDao = database.incomingIntelligenceDao()

    @Provides
    @Singleton
    fun provideIncomingPersonalDao(database: OrionDatabase): IncomingPersonalDao = database.incomingPersonalDao()

    @Provides
    @Singleton
    fun provideGoogleDriveRemoteDataSource(): GoogleDriveRemoteDataSource = HttpGoogleDriveRemoteDataSource()
}
