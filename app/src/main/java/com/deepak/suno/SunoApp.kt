package com.deepak.suno

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * SunoApp is the entry point application class.
 * @HiltAndroidApp triggers Hilt's code generation, including a base class for your
 * application that serves as the application-level dependency container.
 */
@HiltAndroidApp
class SunoApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
