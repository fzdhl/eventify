package com.example.tugasakhirpam.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.tugasakhirpam.ui.category.CategoryDetailScreen
import com.example.tugasakhirpam.ui.category.CategoryDetailViewModel
import com.example.tugasakhirpam.ui.category.CategoryFormScreen
import com.example.tugasakhirpam.ui.category.CategoryFormViewModel
import com.example.tugasakhirpam.ui.category.CategoryListScreen
import com.example.tugasakhirpam.ui.category.CategoryListViewModel
import com.example.tugasakhirpam.ui.components.BottomNavItem
import com.example.tugasakhirpam.ui.event.EventDetailScreen
import com.example.tugasakhirpam.ui.event.EventDetailViewModel
import com.example.tugasakhirpam.ui.event.EventFormScreen
import com.example.tugasakhirpam.ui.event.EventFormViewModel
import com.example.tugasakhirpam.ui.event.EventListScreen
import com.example.tugasakhirpam.ui.event.EventListSource
import com.example.tugasakhirpam.ui.event.EventListViewModel
import com.example.tugasakhirpam.ui.event.MyEventsScreen
import com.example.tugasakhirpam.ui.favorites.FavoritesScreen
import com.example.tugasakhirpam.ui.favorites.FavoritesViewModel
import com.example.tugasakhirpam.ui.home.HomeScreen
import com.example.tugasakhirpam.ui.home.HomeViewModel
import com.example.tugasakhirpam.ui.registration.RegistrationDetailScreen
import com.example.tugasakhirpam.ui.registration.RegistrationDetailViewModel
import com.example.tugasakhirpam.ui.registration.RegistrationFormScreen
import com.example.tugasakhirpam.ui.registration.RegistrationFormViewModel
import com.example.tugasakhirpam.ui.registration.RegistrationListScreen
import com.example.tugasakhirpam.ui.registration.RegistrationListViewModel
import com.example.tugasakhirpam.ui.review.ReviewFormScreen
import com.example.tugasakhirpam.ui.review.ReviewFormViewModel
import com.example.tugasakhirpam.ui.review.ReviewListScreen
import com.example.tugasakhirpam.ui.review.ReviewListViewModel

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    val goBack: () -> Unit = { navController.popBackStack() }

    NavHost(navController = navController, startDestination = Home, modifier = modifier) {

        // ============================ TAB ============================
        composable<Home> { entry ->
            val viewModel: HomeViewModel = viewModel(entry)
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val result by entry.rememberResultMessage()

            LaunchedEffect(Unit) { viewModel.loadEvents() }

            HomeScreen(
                state = state,
                resultMessage = result,
                onResultConsumed = { entry.consumeResultMessage() },
                onEventClick = { id -> navController.navigate(EventDetail(id)) },
                onSeeAllEvents = { navController.navigate(EventList()) },
                onCategoryClick = { category -> navController.navigate(EventList(category.id)) },
                onBottomNavClick = navController::navigateToTab,
                onRetry = viewModel::loadEvents,
            )
        }

        composable<Categories> { entry ->
            val viewModel: CategoryListViewModel = viewModel(entry)
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val result by entry.rememberResultMessage()

            LaunchedEffect(Unit) { viewModel.load() }

            CategoryListScreen(
                state = state,
                resultMessage = result,
                onResultConsumed = { entry.consumeResultMessage() },
                onCategoryClick = { id -> navController.navigate(CategoryDetail(id)) },
                onAddCategoryClick = { navController.navigate(CategoryForm()) },
                onBottomNavClick = navController::navigateToTab,
                onRefresh = viewModel::refresh,
                onRetry = { viewModel.load() },
            )
        }

        composable<Favorites> { entry ->
            val viewModel: FavoritesViewModel = viewModel(entry)
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) { viewModel.loadFavorites() }

            FavoritesScreen(
                state = state,
                onEventClick = { id -> navController.navigate(EventDetail(id)) },
                onExploreClick = { navController.navigateToTab(BottomNavItem.CATEGORIES) },
                onBottomNavClick = navController::navigateToTab,
                onToggleFavorite = viewModel::toggleFavorite,
                onRefresh = viewModel::refresh,
                onRetry = viewModel::loadFavorites,
                onSnackbarDismissed = viewModel::snackbarShown,
            )
        }

        composable<Registrations> { entry ->
            val viewModel: RegistrationListViewModel = viewModel(entry)
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val result by entry.rememberResultMessage()

            LaunchedEffect(Unit) { viewModel.loadRegistrations() }

            RegistrationListScreen(
                state = state,
                resultMessage = result,
                onResultConsumed = { entry.consumeResultMessage() },
                onTabSelect = viewModel::selectTab,
                onRegistrationClick = { id -> navController.navigate(RegistrationDetail(id)) },
                onAddRegistrationClick = { navController.navigate(RegistrationForm()) },
                onBottomNavClick = navController::navigateToTab,
                onRefresh = viewModel::refresh,
                onRetry = viewModel::loadRegistrations,
                onErrorDismissed = viewModel::errorShown,
            )
        }

        composable<MyEvents> { entry ->
            val viewModel: EventListViewModel = viewModel(entry)
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val result by entry.rememberResultMessage()

            LaunchedEffect(Unit) { viewModel.start(EventListSource.MY_EVENTS) }

            MyEventsScreen(
                state = state,
                resultMessage = result,
                onResultConsumed = { entry.consumeResultMessage() },
                onEventClick = { id -> navController.navigate(EventDetail(id)) },
                onCreateEvent = { navController.navigate(EventForm()) },
                onBottomNavClick = navController::navigateToTab,
                onRefresh = viewModel::refresh,
                onRetry = { viewModel.start(EventListSource.MY_EVENTS) },
            )
        }

        // ====================== FITUR KATEGORI ======================
        composable<CategoryDetail> { entry ->
            val args = entry.toRoute<CategoryDetail>()
            val viewModel: CategoryDetailViewModel = viewModel(entry)
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val result by entry.rememberResultMessage()

            LaunchedEffect(args.categoryId) { viewModel.load(args.categoryId) }

            CategoryDetailScreen(
                state = state,
                resultMessage = result,
                onResultConsumed = { entry.consumeResultMessage() },
                onBack = dropUnlessResumed(block = goBack),
                onEditCategory = { id -> navController.navigate(CategoryForm(id)) },
                onEventClick = { id -> navController.navigate(EventDetail(id)) },
                onDelete = viewModel::delete,
                onSnackbarDismissed = viewModel::errorShown,
                onDeleted = { message -> navController.popWithResult(message) },
                onRetry = { viewModel.load(args.categoryId) },
            )
        }

        composable<CategoryForm> { entry ->
            val args = entry.toRoute<CategoryForm>()
            val viewModel: CategoryFormViewModel = viewModel(entry)
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            val categoryId = args.categoryId.takeIf { it != NO_ID }
            LaunchedEffect(categoryId) { viewModel.init(categoryId) }

            CategoryFormScreen(
                state = state,
                onBack = dropUnlessResumed(block = goBack),
                onNameChange = viewModel::onNameChange,
                onDescriptionChange = viewModel::onDescriptionChange,
                onSubmit = viewModel::submit,
                onErrorDismissed = viewModel::errorShown,
                onSaved = { message -> navController.popWithResult(message) },
            )
        }

        // ======================= FITUR EVENT =======================
        composable<EventList> { entry ->
            val args = entry.toRoute<EventList>()
            val viewModel: EventListViewModel = viewModel(entry)
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            val categoryId = args.categoryId.takeIf { it != NO_ID }
            LaunchedEffect(args.categoryId) { viewModel.start(EventListSource.ALL, categoryId) }

            EventListScreen(
                state = state,
                showAsTab = false,
                onBack = dropUnlessResumed(block = goBack),
                onEventClick = { id -> navController.navigate(EventDetail(id)) },
                onBottomNavClick = navController::navigateToTab,
                onCategorySelect = viewModel::selectCategory,
                onToggleFavorite = viewModel::toggleFavorite,
                onRefresh = viewModel::refresh,
                onRetry = { viewModel.start(EventListSource.ALL, categoryId) },
                onSnackbarDismissed = viewModel::snackbarShown,
            )
        }

        composable<EventDetail> { entry ->
            val args = entry.toRoute<EventDetail>()
            val viewModel: EventDetailViewModel = viewModel(entry)
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val result by entry.rememberResultMessage()

            LaunchedEffect(args.eventId) { viewModel.load(args.eventId) }

            EventDetailScreen(
                state = state,
                resultMessage = result,
                onResultConsumed = { entry.consumeResultMessage() },
                onBack = dropUnlessResumed(block = goBack),
                onToggleFavorite = viewModel::toggleFavorite,
                onRegisterClick = { eventId -> navController.navigate(RegistrationForm(eventId = eventId)) },
                onViewRegistration = { id -> navController.navigate(RegistrationDetail(id)) { launchSingleTop = true } },
                onEditEvent = { eventId -> navController.navigate(EventForm(eventId)) },
                onViewReviews = { eventId -> navController.navigate(Reviews(eventId)) },
                onDeleteEvent = viewModel::deleteEvent,
                onUpdateParticipantStatus = viewModel::updateParticipantStatus,
                onSnackbarDismissed = viewModel::snackbarShown,
                onDeleted = { message -> navController.popWithResult(message) },
                onRetry = { viewModel.load(args.eventId) },
            )
        }

        composable<EventForm> { entry ->
            val args = entry.toRoute<EventForm>()
            val viewModel: EventFormViewModel = viewModel(entry)
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            val eventId = args.eventId.takeIf { it != NO_ID }
            LaunchedEffect(eventId) { viewModel.init(eventId) }

            EventFormScreen(
                state = state,
                onBack = dropUnlessResumed(block = goBack),
                onPosterSelected = viewModel::onPosterSelected,
                onTitleChange = viewModel::onTitleChange,
                onCategorySelected = viewModel::onCategorySelected,
                onDescriptionChange = viewModel::onDescriptionChange,
                onDateSelected = viewModel::onDateSelected,
                onStartTimeSelected = viewModel::onStartTimeSelected,
                onEndTimeSelected = viewModel::onEndTimeSelected,
                onLocationChange = viewModel::onLocationChange,
                onQuotaChange = viewModel::onQuotaChange,
                onSubmit = viewModel::submit,
                onErrorDismissed = viewModel::errorShown,
                onSaved = { message -> navController.popWithResult(message) },
            )
        }

        // ===================== FITUR REGISTRASI =====================
        composable<RegistrationDetail> { entry ->
            val args = entry.toRoute<RegistrationDetail>()
            val viewModel: RegistrationDetailViewModel = viewModel(entry)
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val result by entry.rememberResultMessage()

            LaunchedEffect(args.registrationId) { viewModel.load(args.registrationId) }

            RegistrationDetailScreen(
                state = state,
                resultMessage = result,
                onResultConsumed = { entry.consumeResultMessage() },
                onBack = dropUnlessResumed(block = goBack),
                onEdit = { id -> navController.navigate(RegistrationForm(registrationId = id)) },
                onViewEvent = { eventId -> navController.navigate(EventDetail(eventId)) { launchSingleTop = true } },
                onViewReviews = { eventId -> navController.navigate(Reviews(eventId)) },
                onCancel = viewModel::cancel,
                onReRegister = viewModel::reRegister,
                onDelete = viewModel::delete,
                onSnackbarDismissed = viewModel::snackbarShown,
                onDeleted = { message -> navController.popWithResult(message) },
                onRetry = { viewModel.load(args.registrationId) },
            )
        }

        composable<RegistrationForm> { entry ->
            val args = entry.toRoute<RegistrationForm>()
            val viewModel: RegistrationFormViewModel = viewModel(entry)
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            val eventId = args.eventId.takeIf { it != NO_ID }
            val registrationId = args.registrationId.takeIf { it != NO_ID }
            LaunchedEffect(eventId, registrationId) { viewModel.init(eventId, registrationId) }

            RegistrationFormScreen(
                state = state,
                onBack = dropUnlessResumed(block = goBack),
                onEventSelected = viewModel::onEventSelected,
                onFullNameChange = viewModel::onFullNameChange,
                onEmailChange = viewModel::onEmailChange,
                onPhoneChange = viewModel::onPhoneChange,
                onSubmit = viewModel::submit,
                onErrorDismissed = viewModel::errorShown,
                onSaved = { message -> navController.popWithResult(message) },
            )
        }

        // ======================= FITUR ULASAN =======================
        composable<Reviews> { entry ->
            val args = entry.toRoute<Reviews>()
            val viewModel: ReviewListViewModel = viewModel(entry)
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val result by entry.rememberResultMessage()

            LaunchedEffect(args.eventId) { viewModel.load(args.eventId) }

            ReviewListScreen(
                state = state,
                resultMessage = result,
                onResultConsumed = { entry.consumeResultMessage() },
                onBack = dropUnlessResumed(block = goBack),
                onRatingSelect = viewModel::selectRating,
                onWriteReview = { navController.navigate(ReviewForm(eventId = args.eventId)) },
                onEditReview = { reviewId -> navController.navigate(ReviewForm(args.eventId, reviewId)) },
                onDeleteReview = viewModel::deleteReview,
                onSnackbarDismissed = viewModel::snackbarShown,
                onRetry = { viewModel.load(args.eventId) },
            )
        }

        composable<ReviewForm> { entry ->
            val args = entry.toRoute<ReviewForm>()
            val viewModel: ReviewFormViewModel = viewModel(entry)
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            val reviewId = args.reviewId.takeIf { it != NO_ID }
            LaunchedEffect(args.eventId, reviewId) { viewModel.init(args.eventId, reviewId) }

            ReviewFormScreen(
                state = state,
                onBack = dropUnlessResumed(block = goBack),
                onRatingChange = viewModel::onRatingChange,
                onCommentChange = viewModel::onCommentChange,
                onSubmit = viewModel::submit,
                onErrorDismissed = viewModel::errorShown,
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
