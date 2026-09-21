package com.example.detectiveapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.detectiveapp.model.AppDatabase
import com.example.detectiveapp.model.CaseRepository
import com.example.detectiveapp.view.CaseListScreen
import com.example.detectiveapp.view.theme.DetectiveAppTheme
import com.example.detectiveapp.viewmodel.CaseViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(context = this)
        val repository = CaseRepository(
            caseDao = database.caseDao(),
            findingDao = database.findingDao(),
            evidenceDao = database.evidenceDao()
        )

        // Inyección directa usando la factoría local autodefinida
        val viewModel: CaseViewModel by viewModels {
            CaseViewModelLocalFactory(repository)
        }

        setContent {
            DetectiveAppTheme {
                CaseListScreen(viewModel = viewModel)
            }
        }
    }
}

// Factoría local autodefinida para romper el bloqueo de indexación del IDE
class CaseViewModelLocalFactory(private val repository: CaseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CaseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CaseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
