package com.enigmaticdevs.wallhaven.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.preference.PreferenceManager
import com.enigmaticdevs.wallhaven.data.download.DownloadService
import com.enigmaticdevs.wallhaven.data.remote.InterfaceAPI
import com.enigmaticdevs.wallhaven.data.remote.MyInterceptor
import com.enigmaticdevs.wallhaven.domain.billing.BillingRepository
import com.enigmaticdevs.wallhaven.domain.repository.DataStoreRepository
import com.enigmaticdevs.wallhaven.domain.repository.MainRepository
import com.enigmaticdevs.wallhaven.util.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


private const val BASE_URL = "https://wallhaven.cc/api/v1/"
private const val USER_PREFERENCES = "com.enigmaticdevs.wallhaven_dataStore"

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Singleton
    @Provides
    fun provideApi(dataStoreRepository: DataStoreRepository): InterfaceAPI {
        //custom header request
        val client = OkHttpClient.Builder().apply {
            addInterceptor(MyInterceptor(dataStoreRepository))
        }.build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(InterfaceAPI::class.java)

    }

    @Singleton
    @Provides
    fun provideDataStoreRepository(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            migrations = listOf(SharedPreferencesMigration(context, USER_PREFERENCES)),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { context.preferencesDataStoreFile(USER_PREFERENCES) }
        )
    }

    @Singleton
    @Provides
    fun provideDefaultPreferences(@ApplicationContext context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }


    @Singleton
    @Provides
    fun provideMainRepository(api: InterfaceAPI): MainRepository = MainRepository(api)

    @Singleton
    @Provides
    fun provideBillingRepository(@ApplicationContext context : Context): BillingRepository = BillingRepository(context as Application)


    @Singleton
    @Provides
    fun provideDownloadService(dataStoreRepository: DataStoreRepository)  : DownloadService{
        val client = OkHttpClient.Builder().apply {
            addInterceptor(MyInterceptor(dataStoreRepository))
        }.build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(DownloadService::class.java)
    }

    @Singleton
    @Provides
    fun provideDispatchers(): DispatcherProvider = object : DispatcherProvider {
        override val main: CoroutineDispatcher
            get() = Dispatchers.Main
        override val io: CoroutineDispatcher
            get() = Dispatchers.IO
        override val default: CoroutineDispatcher
            get() = Dispatchers.Default
        override val unconfined: CoroutineDispatcher
            get() = Dispatchers.Unconfined
    }
}

