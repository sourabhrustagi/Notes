package notes.arch.practise

import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import notes.arch.practise.di.DaggerApplicationComponent
import timber.log.Timber

open class TodoApplication : DaggerApplication() {
    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerApplicationComponent.factory().create(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}