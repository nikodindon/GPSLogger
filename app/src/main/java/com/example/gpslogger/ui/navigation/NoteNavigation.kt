package com.example.gpslogger.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gpslogger.ui.notes.NoteFragment

@Composable
fun NoteNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "note_screen"
    ) {
        composable("note_screen") {
            NoteFragment()
        }
        // Add more note-related screens here
    }
}