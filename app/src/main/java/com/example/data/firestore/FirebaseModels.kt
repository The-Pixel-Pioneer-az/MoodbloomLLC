package com.example.data.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirestoreUserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val favoriteTruckStop: String = "Arts District Food Yard",
    val bloomRewardPoints: Long = 120L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreFavorite(
    @DocumentId val id: String = "",
    val userId: String = "",
    val menuItemId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val category: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreOrder(
    @DocumentId val id: String = "",
    val userId: String = "",
    val orderCode: String = "",
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val tip: Double = 0.0,
    val total: Double = 0.0,
    val status: String = "RECEIVED",
    val pickupLocation: String = "",
    val summary: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
