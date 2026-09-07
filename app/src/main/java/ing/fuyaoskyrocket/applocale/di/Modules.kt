package ing.fuyaoskyrocket.applocale.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ing.fuyaoskyrocket.applocale.BuildConfig
import ing.fuyaoskyrocket.applocale.data.local.PinnedLocaleStore
import ing.fuyaoskyrocket.applocale.data.repository.ConfigurationEditEnvironment
import ing.fuyaoskyrocket.applocale.data.repository.LocaleRepository
import ing.fuyaoskyrocket.applocale.data.repository.RealConfigurationEditEnvironment
import ing.fuyaoskyrocket.applocale.data.system.AppIconLoader
import ing.fuyaoskyrocket.applocale.data.system.PrivilegedLocaleDataSource
import ing.fuyaoskyrocket.applocale.service.PrivilegedServiceClient
import javax.inject.Singleton
import dagger.Binds

@InstallIn(SingletonComponent::class)
@Module
object Modules {

    @Singleton
    @Provides
    fun provideSharedPreferences(app: Application): SharedPreferences {
        return app.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
    }
}

/** Binds the live-edit environment seam to its production implementation. */
@InstallIn(SingletonComponent::class)
@Module
abstract class ConfigurationEditModule {
    @Binds
    abstract fun bindConfigurationEditEnvironment(
        impl: RealConfigurationEditEnvironment,
    ): ConfigurationEditEnvironment
}

/**
 * Entry point for classes that cannot use @AndroidEntryPoint (e.g. LocaleQuickSettingsTile).
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppEntryPoint {
    fun privilegedServiceClient(): PrivilegedServiceClient
    fun privilegedLocaleDataSource(): PrivilegedLocaleDataSource
    fun localeRepository(): LocaleRepository
    fun pinnedLocaleStore(): PinnedLocaleStore
    fun appIconLoader(): AppIconLoader
}
