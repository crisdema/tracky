package com.crisdema.tracky.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.crisdema.tracky.ui.screens.addtransaction.AddTransactionScreen
import com.crisdema.tracky.ui.screens.auth.AuthScreen
import com.crisdema.tracky.ui.screens.categories.CategoriesScreen
import com.crisdema.tracky.ui.screens.categorytransactions.CategoryTransactionsScreen
import com.crisdema.tracky.ui.screens.settings.SettingsScreen
import com.crisdema.tracky.ui.screens.spaceswitcher.SpaceSwitcherScreen
import com.crisdema.tracky.ui.screens.transactions.TransactionListScreen

object Routes {
    const val AUTH = "auth"
    const val HOME = "home/{spaceId}"
    const val ADD_TRANSACTION = "add_transaction/{spaceId}/{type}"
    const val SETTINGS = "settings/{spaceId}"
    const val CATEGORIES = "categories/{spaceId}"
    const val CATEGORY_TRANSACTIONS = "category_transactions/{spaceId}/{categoryId}/{yearMonth}"
    const val SPACE_SWITCHER = "space_switcher/{spaceId}"

    fun home(spaceId: String) = "home/$spaceId"
    fun addTransaction(spaceId: String, type: String) = "add_transaction/$spaceId/$type"
    fun settings(spaceId: String) = "settings/$spaceId"
    fun categories(spaceId: String) = "categories/$spaceId"
    fun categoryTransactions(spaceId: String, categoryId: String, yearMonth: String) =
        "category_transactions/$spaceId/$categoryId/$yearMonth"

    fun spaceSwitcher(spaceId: String) = "space_switcher/$spaceId"
}

@Composable
fun TrackyNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.AUTH) {

        composable(Routes.AUTH) {
            AuthScreen(
                onAuthenticated = { spaceId ->
                    navController.navigate(Routes.home(spaceId)) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) { backStackEntry ->
            val spaceId = backStackEntry.arguments?.getString("spaceId") ?: return@composable
            TransactionListScreen(
                spaceId = spaceId,
                onAddTransaction = { type -> navController.navigate(Routes.addTransaction(spaceId, type.name)) },
                onOpenSettings = { navController.navigate(Routes.settings(spaceId)) },
                onOpenCategory = { categoryId, month ->
                    navController.navigate(Routes.categoryTransactions(spaceId, categoryId, month.toString()))
                }
            )
        }

        composable(Routes.ADD_TRANSACTION) { backStackEntry ->
            val spaceId = backStackEntry.arguments?.getString("spaceId") ?: return@composable
            AddTransactionScreen(
                spaceId = spaceId,
                onDone = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) { backStackEntry ->
            val spaceId = backStackEntry.arguments?.getString("spaceId") ?: return@composable
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenCategories = { navController.navigate(Routes.categories(spaceId)) },
                onSpaceChanged = { newSpaceId ->
                    navController.navigate(Routes.home(newSpaceId)) {
                        popUpTo(Routes.AUTH) { inclusive = false }
                    }
                },
                onSwitchSpace = { navController.navigate(Routes.spaceSwitcher(spaceId)) },
                onSignedOut = {
                    navController.navigate(Routes.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SPACE_SWITCHER) {
            SpaceSwitcherScreen(
                onBack = { navController.popBackStack() },
                onSpaceSelected = { newSpaceId ->
                    navController.navigate(Routes.home(newSpaceId)) {
                        popUpTo(Routes.AUTH) { inclusive = false }
                    }
                }
            )
        }

        composable(Routes.CATEGORIES) {
            CategoriesScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.CATEGORY_TRANSACTIONS) {
            CategoryTransactionsScreen(onBack = { navController.popBackStack() })
        }


    }
}