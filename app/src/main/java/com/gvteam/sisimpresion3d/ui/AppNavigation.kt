package com.gvteam.sisimpresion3d.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gvteam.sisimpresion3d.data.SupabaseClient
import com.gvteam.sisimpresion3d.data.repository.MaterialRepository
import com.gvteam.sisimpresion3d.model.UserProfile
import com.gvteam.sisimpresion3d.ui.screens.LoginScreen
import com.gvteam.sisimpresion3d.ui.screens.MainScreen
import com.gvteam.sisimpresion3d.ui.screens.OnboardingScreen
import com.gvteam.sisimpresion3d.ui.screens.ProfileScreen
import com.gvteam.sisimpresion3d.viewmodel.MaterialViewModel
import com.gvteam.sisimpresion3d.viewmodel.PrinterViewModel
import com.gvteam.sisimpresion3d.viewmodel.UserViewModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    var isCheckingSession by remember { mutableStateOf(true) }
    var startDestination by remember { mutableStateOf("login") }
    val scope = rememberCoroutineScope()

    val viewModelFactory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when {
                modelClass.isAssignableFrom(PrinterViewModel::class.java) -> PrinterViewModel() as T
                modelClass.isAssignableFrom(UserViewModel::class.java) -> UserViewModel() as T
                modelClass.isAssignableFrom(MaterialViewModel::class.java) -> {
                    val repository = MaterialRepository(SupabaseClient.client)
                    MaterialViewModel(repository) as T
                }
                else -> throw IllegalArgumentException("ViewModel desconocido")
            }
        }
    }

    val userViewModel: UserViewModel = viewModel(factory = viewModelFactory)

    LaunchedEffect(Unit) {
        val sessionRestored = try {
            SupabaseClient.client.auth.loadFromStorage()
        } catch (e: Exception) { false }

        if (sessionRestored) {
            try {
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: ""
                val profile = SupabaseClient.client.from("profiles")
                    .select { filter { eq("id", userId) } }
                    .decodeSingleOrNull<UserProfile>()

                if (profile != null && (profile.isExternal || !profile.career.isNullOrBlank())) {
                    startDestination = "main"
                    userViewModel.fetchUserProfile()
                } else {
                    startDestination = "onboarding"
                }
            } catch (e: Exception) {
                startDestination = "login"
            }
        } else {
            startDestination = "login"
        }
        isCheckingSession = false
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (isCheckingSession) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            SharedTransitionLayout {
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    enterTransition = { fadeIn(animationSpec = tween(400)) },
                    exitTransition = { fadeOut(animationSpec = tween(400)) },
                    popEnterTransition = { fadeIn(animationSpec = tween(400)) },
                    popExitTransition = { fadeOut(animationSpec = tween(400)) }
                ) {
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = {
                                userViewModel.fetchUserProfile()
                                scope.launch {
                                    try {
                                        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: ""
                                        val profile = SupabaseClient.client.from("profiles")
                                            .select { filter { eq("id", userId) } }
                                            .decodeSingleOrNull<UserProfile>()

                                        if (profile != null && (profile.isExternal || !profile.career.isNullOrBlank())) {
                                            navController.navigate("main") { popUpTo("login") { inclusive = true } }
                                        } else {
                                            navController.navigate("onboarding") { popUpTo("login") { inclusive = true } }
                                        }
                                    } catch (e: Exception) {
                                    }
                                }
                            }
                        )
                    }

                    composable("onboarding") {
                        OnboardingScreen(
                            onFinished = {
                                userViewModel.fetchUserProfile()
                                navController.navigate("main") { popUpTo("onboarding") { inclusive = true } }
                            }
                        )
                    }

                    composable("main") {
                        val printerViewModel: PrinterViewModel = viewModel(factory = viewModelFactory)
                        val materialViewModel: MaterialViewModel = viewModel(factory = viewModelFactory)

                        MainScreen(
                            printerViewModel = printerViewModel,
                            materialViewModel = materialViewModel,
                            userViewModel = userViewModel,
                            onNavigateToProfile = {
                                navController.navigate("profile")
                            },
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this@composable
                        )
                    }

                    composable("profile") {
                        ProfileScreen(
                            userViewModel = userViewModel,
                            onBack = { navController.popBackStack() },
                            onLogout = {
                                scope.launch {
                                    try {
                                        SupabaseClient.client.auth.signOut()
                                        navController.navigate("login") {
                                            popUpTo("main") { inclusive = true }
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error al salir", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this@composable
                        )
                    }
                }
            }
        }
    }
}