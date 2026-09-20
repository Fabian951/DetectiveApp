package com.example.detectiveapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.detectiveapp.model.AppDatabase
import com.example.detectiveapp.model.CaseRepository
import com.example.detectiveapp.view.CaseListScreen
import com.example.detectiveapp.view.theme.DetectiveAppTheme
import com.example.detectiveapp.viewmodel.CaseViewModel
import com.example.detectiveapp.viewmodel.CaseViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(this)
        val repository = CaseRepository(
            caseDao = database.caseDao(),
            findingDao = database.findingDao(),
            evidenceDao = database.evidenceDao()
        )
        val viewModel: CaseViewModel by viewModels {
            CaseViewModelFactory(repository)
        }

        setContent {
            DetectiveAppTheme {
                CaseListScreen(viewModel = viewModel)
            }
        }
    }
}