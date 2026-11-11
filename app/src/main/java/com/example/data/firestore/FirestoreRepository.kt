package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.MenuItem
import com.example.model.Order
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
    val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun currentUserId(): String? = auth.currentUser?.uid

    // --- FAVORITES ---
    fun observeFavorites(userId: String): Flow<List<FirestoreFavorite>> {
        val path = "users/$userId/favorites"
        return db.collection("users").document(userId).collection("favorites")
            .snapshots()
            .map { snapshot -> snapshot.toObjects(FirestoreFavorite::class.java) }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w("FirestoreRepo", "Error observing favorites at $path: ${error.message}")
                emit(emptyList())
            }
    }

    suspend fun addFavorite(userId: String, item: MenuItem): Boolean {
        return try {
            val docRef = db.collection("users").document(userId)
                .collection("favorites").document(item.id)
            val payload = mapOf(
                "userId" to userId,
                "menuItemId" to item.id,
                "name" to item.name,
                "price" to item.price,
                "category" to item.category.displayName,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Failed to add favorite: ${e.message}", e)
            false
        }
    }

    suspend fun removeFavorite(userId: String, menuItemId: String): Boolean {
        return try {
            db.collection("users").document(userId)
                .collection("favorites").document(menuItemId)
                .delete().await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Failed to remove favorite: ${e.message}", e)
            false
        }
    }

    // --- ORDERS ---
    fun observeOrders(userId: String): Flow<List<FirestoreOrder>> {
        val path = "users/$userId/orders"
        return db.collection("users").document(userId).collection("orders")
            .snapshots()
            .map { snapshot -> snapshot.toObjects(FirestoreOrder::class.java) }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w("FirestoreRepo", "Error observing orders at $path: ${error.message}")
                emit(emptyList())
            }
    }

    suspend fun saveOrder(userId: String, order: Order): Boolean {
        return try {
            val docRef = db.collection("users").document(userId)
                .collection("orders").document(order.orderId)
            val summary = order.items.joinToString(", ") { "${it.quantity}x ${it.menuItem.name}" }
            val payload = mapOf(
                "userId" to userId,
                "orderCode" to order.orderCode,
                "subtotal" to order.subtotal,
                "tax" to order.tax,
                "tip" to order.tip,
                "total" to order.total,
                "status" to order.status.name,
                "pickupLocation" to order.pickupLocationName,
                "summary" to summary.take(450),
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Failed to save order: ${e.message}", e)
            false
        }
    }

    // --- PROFILE ---
    fun observeProfile(userId: String): Flow<FirestoreUserProfile?> {
        val docRef = db.collection("users").document(userId)
        return docRef.snapshots()
            .map { snapshot ->
                if (snapshot.exists()) snapshot.toObject(FirestoreUserProfile::class.java) else null
            }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w("FirestoreRepo", "Error observing profile: ${error.message}")
                emit(null)
            }
    }

    suspend fun saveProfile(profile: FirestoreUserProfile): Boolean {
        return try {
            val docRef = db.collection("users").document(profile.userId)
            val payload = mapOf(
                "userId" to profile.userId,
                "name" to profile.name,
                "email" to profile.email,
                "phone" to profile.phone,
                "favoriteTruckStop" to profile.favoriteTruckStop,
                "bloomRewardPoints" to profile.bloomRewardPoints,
                "createdAt" to (profile.createdAt ?: FieldValue.serverTimestamp()),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Failed to save profile: ${e.message}", e)
            false
        }
    }

    suspend fun deleteUserData(userId: String): Boolean {
        return try {
            // Delete favorites subcollection
            val favs = db.collection("users").document(userId).collection("favorites").get().await()
            for (doc in favs.documents) {
                doc.reference.delete().await()
            }
            // Delete orders subcollection
            val ords = db.collection("users").document(userId).collection("orders").get().await()
            for (doc in ords.documents) {
                doc.reference.delete().await()
            }
            // Delete user doc
            db.collection("users").document(userId).delete().await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Failed to wipe user data: ${e.message}", e)
            false
        }
    }
}
