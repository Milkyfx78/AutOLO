package com.damsel.app.di

import android.content.Context
import androidx.room.Room
import com.damsel.app.data.DamselDatabase
import com.damsel.app.data.PdfTextExtractor
import com.damsel.app.data.SecureKeyStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DamselDatabase =
        Room.databaseBuilder(context, DamselDatabase::class.java, "damsel.db")
            // Pre-release schema; destructive fallback is an accepted trade-off until this
            // app has real users with data worth writing a migration path for.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideBookDao(database: DamselDatabase) = database.bookDao()

    @Provides
    @Singleton
    fun provideSecureKeyStore(@ApplicationContext context: Context): SecureKeyStore =
        SecureKeyStore(context)

    @Provides
    @Singleton
    fun providePdfTextExtractor(@ApplicationContext context: Context): PdfTextExtractor =
        PdfTextExtractor(context)

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
}
