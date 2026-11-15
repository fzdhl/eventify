package com.example.tugasakhirpam.data

import com.example.tugasakhirpam.model.User

object AppContainer {
    val currentUser: User get() = FakeDataSource.currentUser
    val eventRepository: EventRepository by lazy { FakeEventRepository() }
    val registrationRepository: RegistrationRepository by lazy { FakeRegistrationRepository() }
    val reviewRepository: ReviewRepository by lazy { FakeReviewRepository() }
}
