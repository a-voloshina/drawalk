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

object FirebaseHolder {
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
                            PointSettings(0, 5.0)
                        )
                    ), Timestamp.now(),
                    SecureRandom().nextInt(5).let {
                        when (it) {
                            0 -> null
                            1 -> "fdsgsdafvdscvwd"
                            2 -> "https://img-fotki.yandex.ru/get/71249/287605011.879/0_184676_96b79ecb_orig.Jpg"
                            3 -> "https://lifeimg.pravda.com/images/doc/3/a/3a408d7-gps-paint-velo--4-.jpg"
                            4 -> "https://bugaga.ru/uploads/posts/2016-02/1455714186_risunki-na-velike-9.jpg"
                            else -> null
                        }
                    }
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

    /**
     * Creates new GPS art and uploads it to Firebase
     *
     * @param preview art preview image
     * @param name art name
     * @param polylines art polylines
     * @param onSuccess success callback, gets ID of created art
     * @param onError error callback
     * @param compressionQuality JPEG compression quality (0-100), 0 - small size, 100 - high quality
     * @return preview UploadTask to manage preview uploading process
     * **/
    fun createNewArt(
        preview: Bitmap,
        name: String,
        polylines: List<ArtPolyLine>,
        onSuccess: (String) -> Unit = {},
        onError: (Exception?) -> Unit = {},
        compressionQuality: Int = 100
    ): UploadTask {
        val userId = UUID.randomUUID().toString()   //TODO: get current user's ID
        val artId = UUID.randomUUID().toString()

        return uploadImage(
            preview,
            artId,
            { uri ->
                if (uri == null) {
                    onError(RuntimeException("Got null image URI"))
                } else {
                    val gpsArt = GpsArt(artId, userId, name, polylines, previewUrl = uri.toString())
                    ARTS.document(artId).set(gpsArt).addOnFailureListener {
                        onError(it)
                    }.addOnSuccessListener {
                        onSuccess(artId)
                    }
                }
            },
            {
                onError(it)
            }, compressionQuality
        )
    }
}