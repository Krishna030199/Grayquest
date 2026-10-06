package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.GQDatabase
import com.example.data.repository.GQRepository
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate50
import com.example.ui.viewmodel.GQViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var database: GQDatabase
    private lateinit var repository: GQRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = GQDatabase.getDatabase(this, lifecycleScope)
        val firestoreManager = com.example.data.firebase.GQFirestoreManager(this, lifecycleScope)
        repository = GQRepository(
            leadDao = database.leadDao(),
            teamMemberDao = database.teamMemberDao(),
            institutionDao = database.institutionDao(),
            activityDao = database.leadActivityDao(),
            visitDao = database.campusVisitDao(),
            firestoreManager = firestoreManager
        )

        // Ensure database has sample data ready immediately for instant interaction
        lifecycleScope.launch(Dispatchers.IO) {
            val existingMembers = database.teamMemberDao().getMemberById(1)
            if (existingMembers == null) {
                GQDatabase.populateInitialData(database)
            }
        }

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Slate50
                ) {
                    val viewModel: GQViewModel = viewModel(
                        factory = GQViewModel.provideFactory(repository)
                    )
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}
