package com.deepak.suno.di

import android.content.Context
import androidx.room.Room
import com.deepak.suno.data.local.SunoDatabase
import com.deepak.suno.data.local.dao.FavoriteDao
import com.deepak.suno.data.local.dao.PlaylistDao
import com.deepak.suno.data.local.dao.SongStatsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSunoDatabase(
        @ApplicationContext context: Context
    ): SunoDatabase {
        return Room.databaseBuilder(
            context,
            SunoDatabase::class.java,
            "suno_music.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideFavoriteDao(database: SunoDatabase): FavoriteDao {
        return database.favoriteDao()
    }

    @Provides
    fun providePlaylistDao(database: SunoDatabase): PlaylistDao {
        return database.playlistDao()
    }

    @Provides
    fun provideSongStatsDao(database: SunoDatabase): SongStatsDao {
        return database.songStatsDao()
    }
}
