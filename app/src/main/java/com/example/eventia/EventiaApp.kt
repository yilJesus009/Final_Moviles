package com.example.eventia

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// @HiltAndroidApp: punto de partida de Hilt, crea el contenedor de dependencias de toda la app
@HiltAndroidApp
class EventiaApp : Application()
