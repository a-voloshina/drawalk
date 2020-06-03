package ru.nsu.fit.android.drawalk.utils

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import ru.nsu.fit.android.drawalk.model.firebase.ArtPolyLine
import ru.nsu.fit.android.drawalk.model.firebase.GpsArt
import ru.nsu.fit.android.drawalk.model.firebase.PointSettings
import ru.nsu.fit.android.drawalk.model.firebase.UserData
import java.security.SecureRandom
import java.util.*

object FirestoreHolder {
    val DB = Firebase.firestore
    val ARTS = DB.collection("arts")
    val USERS = DB.collection("users")

    fun addStubArt() {
        val id = UUID.randomUUID().toString()
        USERS.document(id)
            .set(UserData(id, "user ${SecureRandom().nextInt(1000)}"))
        ARTS.document(id)
            .set(
                GpsArt(
                    id, id, "name ${SecureRandom().nextInt(1000)}", listOf(
                        ArtPolyLine(
                            listOf(
                                GeoPoint(0.0, 0.0),
                                GeoPoint(1.0, 1.0)
                            ),
                            PointSettings(0, 5)
                        )
                    ), Timestamp.now()
                )
            )
            .addOnFailureListener {
                Log.d("SET ART", "Error: ${it.message}")
            }
            .addOnSuccessListener {
                Log.d("SET ART", "Success")
            }
    }

    fun clearAll() {
        USERS.get().addOnSuccessListener {
            for (document in it.documents) {
                USERS.document(document.id).delete()
            }
        }
        ARTS.get().addOnSuccessListener {
            for (document in it.documents) {
                ARTS.document(document.id).delete()
            }
        }
    }
}