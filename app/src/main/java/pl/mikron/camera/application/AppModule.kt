package pl.mikron.camera.application

import android.content.Context
import android.content.res.Resources
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(value = [SingletonComponent::class])
object AppModule {

  @Provides
  fun provideResources(@ApplicationContext context: Context): Resources =
    context.resources
}
