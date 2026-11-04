package com.example.transipay

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.transipay.screen.auth.SignInScreen
import com.example.transipay.screen.company.CreateCompanyScreen
import com.example.transipay.ui.theme.TransipayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TransipayTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "create_company") {

                    composable("create_company") {
                        CreateCompanyScreen(
                            onCompanyCreated = { name ->
                                navController.navigate("signin?company=${Uri.encode(name)}") {
                                    // Remove the form so Back doesn't return to it
                                    popUpTo("create_company") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(
                        route = "signin?company={company}",
                        arguments = listOf(
                            navArgument("company") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        )
                    ) { entry ->
                        val company = entry.arguments?.getString("company")
                        SignInScreen(
                            companyName = company ?: "Your Company",
                            onSignedIn = {
                                // TODO: navigate to your home/dashboard screen
                            }
                        )
                    }
                }
            }
        }
    }
}