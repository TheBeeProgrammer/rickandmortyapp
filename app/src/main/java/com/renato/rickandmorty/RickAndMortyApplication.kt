package com.renato.rickandmorty

import android.app.Application
import com.renato.logger.Logger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RickAndMortyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Logger.init()
    }
}
