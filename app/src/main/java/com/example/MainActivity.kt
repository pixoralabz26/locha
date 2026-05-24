package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.LochaDatabase
import com.example.data.LochaRepository
import com.example.ui.screens.LochaApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.LochaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize Room Database & abstract repository layer
        val database = LochaDatabase.getDatabase(applicationContext)
        val repository = LochaRepository(database.lochaDao())
        
        // Expose ViewModel constructed via standard factory provider
        val factory = LochaViewModel.Factory(repository)
        val lochaViewModel = ViewModelProvider(this, factory)[LochaViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LochaApp(
                        viewModel = lochaViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
