package com.fliker.shiftscheduler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.fliker.shiftscheduler.data.local.AppDatabase
import com.fliker.shiftscheduler.data.local.UserPreferencesRepository
import com.fliker.shiftscheduler.data.repository.ShiftRepositoryImpl
import com.fliker.shiftscheduler.ui.navigation.AppNavigation
import com.fliker.shiftscheduler.ui.theme.ShiftSchedulerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "shifts-db"
        ).build()
        val repository = ShiftRepositoryImpl(db.shiftDao())
        val userPreferencesRepository = UserPreferencesRepository(applicationContext)
        
        enableEdgeToEdge()
        setContent {
            AppNavigation(
                repository = repository,
                userPreferencesRepository = userPreferencesRepository
            )
        }
    }
}