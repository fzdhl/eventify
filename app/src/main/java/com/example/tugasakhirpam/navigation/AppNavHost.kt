package com.example.tugasakhirpam.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.tugasakhirpam.model.EventCategory
import com.example.tugasakhirpam.ui.components.BottomNavItem
import com.example.tugasakhirpam.ui.event.EventDetailScreen
import com.example.tugasakhirpam.ui.event.EventFormScreen
import com.example.tugasakhirpam.ui.event.EventListScreen
import com.example.tugasakhirpam.ui.event.FavoritesScreen
import com.example.tugasakhirpam.ui.event.MyEventsScreen
import com.example.tugasakhirpam.ui.home.HomeScreen
import com.example.tugasakhirpam.ui.registration.RegistrationDetailScreen
import com.example.tugasakhirpam.ui.registration.RegistrationFormScreen
import com.example.tugasakhirpam.ui.registration.RegistrationListScreen

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    val goBack: () -> Unit = { navController.popBackStack() }

    NavHost(navController = navController, startDestination = Home, modifier = modifier) {

        composable<Home> { entry ->
            val result by entry.rememberResultMessage()
            HomeScreen(
                resultMessage = result,
                onResultConsumed = { entry.consumeResultMessage() },
                onEventClick = { id -> navController.navigate(EventDetail(id)) },
                onSeeAllEvents = { navController.navigate(EventList()) },
                onCategoryClick = { category -> navController.navigate(EventList(category.name)) },
                onBottomNavClick = navController::navigateToTab,
            )
        }

        composable<Categories> {
            EventListScreen(
                initialCategory = null,
                showAsTab = true,
                onBack = goBack,
                onEventClick = { id -> navController.navigate(EventDetail(id)) },
                onBottomNavClick = navController::navigateToTab,
            )
        }

        composable<Favorites> {
            FavoritesScreen(
                onEventClick = { id -> navController.navigate(EventDetail(id)) },
                onExploreClick = { navController.navigateToTab(BottomNavItem.CATEGORIES) },
                onBottomNavClick = navController::navigateToTab,
            )
        }

        composable<Registrations> { entry ->
            val result by entry.rememberResultMessage()
            RegistrationListScreen(
                resultMessage = result,
                onResultConsumed = { entry.consumeResultMessage() },
                onRegistrationClick = { id -> navController.navigate(RegistrationDetail(id)) },
                onAddRegistrationClick = { navController.navigate(RegistrationForm()) },
                onBottomNavClick = navController::navigateToTab,
            )
        }

        composable<MyEvents> { entry ->
            val result by entry.rememberResultMessage()
            MyEventsScreen(
                resultMessage = result,
                onResultConsumed = { entry.consumeResultMessage() },
                onEventClick = { id -> navController.navigate(EventDetail(id)) },
                onCreateEvent = { navController.navigate(EventForm()) },
                onBottomNavClick = navController::navigateToTab,
            )
        }

        composable<EventList> { entry ->
            val args = entry.toRoute<EventList>()
            EventListScreen(
                initialCategory = EventCategory.entries.firstOrNull { it.name == args.category },
                showAsTab = false,
                onBack = dropUnlessResumed(block = goBack),
                onEventClick = { id -> navController.navigate(EventDetail(id)) },
                onBottomNavClick = navController::navigateToTab,
            )
        }

        composable<EventDetail> { entry ->
            val args = entry.toRoute<EventDetail>()
            val result by entry.rememberResultMessage()
            EventDetailScreen(
                eventId = args.eventId,
                resultMessage = result,
                onResultConsumed = { entry.consumeResultMessage() },
                onBack = dropUnlessResumed(block = goBack),
                onRegisterClick = { eventId -> navController.navigate(RegistrationForm(eventId = eventId)) },
                onViewRegistration = { id -> navController.navigate(RegistrationDetail(id)) { launchSingleTop = true } },
                onEditEvent = { eventId -> navController.navigate(EventForm(eventId)) },
                onDeleted = { message -> navController.popWithResult(message) },
            )
        }

        composable<EventForm> { entry ->
            val args = entry.toRoute<EventForm>()
            EventFormScreen(
                eventId = args.eventId.takeIf { it != NO_ID },
                onBack = dropUnlessResumed(block = goBack),
                onSaved = { message -> navController.popWithResult(message) },
            )
        }

        composable<RegistrationDetail> { entry ->
            val args = entry.toRoute<RegistrationDetail>()
            val result by entry.rememberResultMessage()
            RegistrationDetailScreen(
                registrationId = args.registrationId,
                resultMessage = result,
                onResultConsumed = { entry.consumeResultMessage() },
                onBack = dropUnlessResumed(block = goBack),
                onEdit = { id -> navController.navigate(RegistrationForm(registrationId = id)) },
                onViewEvent = { eventId -> navController.navigate(EventDetail(eventId)) { launchSingleTop = true } },
                onDeleted = { message -> navController.popWithResult(message) },
            )
        }

        composable<RegistrationForm> { entry ->
            val args = entry.toRoute<RegistrationForm>()
            RegistrationFormScreen(
                eventId = args.eventId.takeIf { it != NO_ID },
                registrationId = args.registrationId.takeIf { it != NO_ID },
                onBack = dropUnlessResumed(block = goBack),
                onSaved = { message -> navController.popWithResult(message) },
            )
        }
    }
}

fun NavHostController.navigateToTab(item: BottomNavItem) {
    val route: Any = when (item) {
        BottomNavItem.HOME -> Home
        BottomNavItem.CATEGORIES -> Categories
        BottomNavItem.FAVORITES -> Favorites
        BottomNavItem.REGISTRATIONS -> Registrations
        BottomNavItem.MY_EVENTS -> MyEvents
    }
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavHostController.popWithResult(message: String) {
    previousBackStackEntry?.savedStateHandle?.set(RESULT_MESSAGE_KEY, message)
    popBackStack()
}

@Composable
private fun NavBackStackEntry.rememberResultMessage(): State<String?> {
    val flow = remember(this) { savedStateHandle.getStateFlow<String?>(RESULT_MESSAGE_KEY, null) }
    return flow.collectAsStateWithLifecycle()
}

private fun NavBackStackEntry.consumeResultMessage() {
    savedStateHandle.set<String?>(RESULT_MESSAGE_KEY, null)
}
