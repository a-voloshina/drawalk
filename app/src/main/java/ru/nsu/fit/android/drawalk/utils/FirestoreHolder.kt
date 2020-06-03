package ru.nsu.fit.android.drawalk.utils

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import ru.nsu.fit.android.drawalk.model.firebase.ArtPolyLine
import ru.nsu.fit.android.drawalk.model.firebase.GpsArt
import ru.nsu.fit.android.drawalk.model.firebase.PointSettings
import ru.nsu.fit.android.drawalk.model.firebase.UserData
import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import java.util.*

object FirestoreHolder {
    val DB = Firebase.firestore
    val ARTS = DB.collection("arts")
    val USERS = DB.collection("users")

    val STORAGE = Firebase.storage.reference
    val ART_IMAGES = STORAGE.child("images/art_previews")

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

    /**
     * Uploads image (art preview) to firebase storage, image will be saved as '/images/art_previews/art_{artId}_prewiev.jpg'
     *
     * @param image Bitmap to upload
     * @param artId ID of art
     * @param onSuccess callback to get URI of uploaded image
     * @param onError callback to handle errors
     * @param compressionQuality JPEG compression quality (0-100), 0 - small size, 100 - high quality
     * @return UploadTask to manage uploading process
     * **/
    fun uploadImage(
        image: Bitmap,
        artId: String,
        onSuccess: (Uri?) -> Unit = {},
        onError: (Exception?) -> Unit = {},
        compressionQuality: Int = 100
    ): UploadTask {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, compressionQuality, baos)
        val data = baos.toByteArray()

        val ref = ART_IMAGES.child("art_${artId}_preview.jpg")
        val uploadTask = ref.putBytes(data)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess(task.result)
            } else {
                onError(task.exception)
            }
        }

        return uploadTask
    }
}