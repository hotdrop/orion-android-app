package jp.hotdrop.orion.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import jp.hotdrop.orion.data.local.OrionDatabase
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
        ).build()

    @Provides
    @Singleton
    fun provideGoogleDriveRemoteDataSource(): GoogleDriveRemoteDataSource = HttpGoogleDriveRemoteDataSource()
}
