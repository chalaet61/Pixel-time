package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.data.database.ClockDatabase
import com.example.data.repository.ClockRepository
import com.example.ui.ClockAppUi
import com.example.ui.ClockViewModel
import com.example.ui.ClockViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Initialize Room Database, Repository, and ViewModel
    val db = Room.databaseBuilder(
      applicationContext,
      ClockDatabase::class.java,
      "digital_clock_db"
    ).fallbackToDestructiveMigration().build()
    
    val repository = ClockRepository(
      db.alarmDao(),
      db.worldCityDao(),
      db.userSettingsDao()
    )
    
    val viewModel = ViewModelProvider(
      this,
      ClockViewModelFactory(repository)
    )[ClockViewModel::class.java]

    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          ClockAppUi(
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize()
          )
        }
      }
    }
  }
}
