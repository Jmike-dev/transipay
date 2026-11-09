package com.example.transipay.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.transipay.screen.auth.SignInScreen
import com.example.transipay.screen.auth.SignUpScreen
import com.example.transipay.screen.company.CreateCompanyScreen
import com.example.transipay.screen.home.HomeScreen
import com.example.transipay.screen.staff.AddStaffScreen
import com.example.transipay.service.ApiClient
import com.example.transipay.service.auth.Session

object Routes {
    const val CREATE_COMPANY = "create_company"
    const val SIGN_UP = "sign_up/{companyId}/{companyName}"
    const val SIGN_IN = "sign_in"
    const val HOME = "home"
    const val ADD_STAFF = "add_staff"

    fun signUp(companyId: String, companyName: String) =
        "sign_up/${Uri.encode(companyId)}/${Uri.encode(companyName)}"
}

@Composable
fun AppNavigation() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.CREATE_COMPANY) {

        composable(Routes.CREATE_COMPANY) {    CreateCompanyScreen(
            onCompanyCreated = { id, name ->
                nav.navigate(Routes.signUp(id, name)) {
                    popUpTo(Routes.CREATE_COMPANY) { inclusive = true }
                }
            },
            onHaveAccount = { nav.navigate(Routes.SIGN_IN) },
        )
        }

        composable(
            route = Routes.SIGN_UP,
            arguments = listOf(
                navArgument("companyId") { type = NavType.StringType },
                navArgument("companyName") { type = NavType.StringType },
            ),
        ) { entry ->
            SignUpScreen(
                companyId = entry.arguments?.getString("companyId").orEmpty(),
                companyName = entry.arguments?.getString("companyName").orEmpty(),
                onSignedUp = {
                    nav.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } }
                },
                onHaveAccount = { nav.navigate(Routes.SIGN_IN) },
            )
        }

        composable(Routes.SIGN_IN) {
            SignInScreen(
                companyName = "TransiPay",
                onSignedIn = {
                    nav.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } }
                },
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                companyId = Session.companyId.orEmpty(),
                onAddStaff = { nav.navigate(Routes.ADD_STAFF) },
                onSignOut = {
                    Session.clear()
                    ApiClient.authToken = null
                    nav.navigate(Routes.SIGN_IN) { popUpTo(0) { inclusive = true } }
                },
            )
        }

        composable(Routes.ADD_STAFF) {
            AddStaffScreen(onBack = { nav.popBackStack() })
        }
    }
}