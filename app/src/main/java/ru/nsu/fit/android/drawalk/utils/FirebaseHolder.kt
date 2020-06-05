package ru.nsu.fit.android.drawalk.utils

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import ru.nsu.fit.android.drawalk.model.GpsArtData
import ru.nsu.fit.android.drawalk.model.firebase.ArtPolyLine
import ru.nsu.fit.android.drawalk.model.firebase.GpsArt
import ru.nsu.fit.android.drawalk.model.firebase.PointSettings
import ru.nsu.fit.android.drawalk.model.firebase.UserData
import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import java.util.*

object FirebaseHolder {
    const val USER_ID_FIELD = "id"
    const val USER_NAME_FIELD = "name"

    const val ART_AUTHOR_ID_FIELD = "authorId"
    const val ART_CREATED_FIELD = "created"
    const val ART_NAME_FIELD = "name"

    val DB = Firebase.firestore
    val ARTS = DB.collection("arts")
    val USERS = DB.collection("users")

    val STORAGE = Firebase.storage.reference
    val ART_IMAGES = STORAGE.child("images/art_previews")

    val AUTH = Firebase.auth

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

    fun getArtFullInfo(
        artId: String,
        onSuccess: (GpsArt, String?) -> Unit = {_, _ ->},
        onError: (Exception) -> Unit = {}
    ) {
        ARTS.document(artId).get().addOnFailureListener(onError)
            .addOnSuccessListener { artDoc ->
                val gpsArt = artDoc.toObject(GpsArt::class.java) ?: run {
                    onError(RuntimeException("Cannot deserialize document as GpsArt"))
                    return@addOnSuccessListener
                }
                USERS.document(gpsArt.authorId).get().addOnFailureListener(onError)
                    .addOnSuccessListener { userDoc ->
                        val username = userDoc.data?.get(USER_NAME_FIELD) as? String
                        onSuccess(gpsArt, username)
                    }
            }
    }

    fun getArtsSize(
        userId: String? = null,
        onSuccess: (Int) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val query = if (userId != null) {
            ARTS.whereEqualTo(ART_AUTHOR_ID_FIELD, userId)
        } else {
            ARTS
        }
        query.get().addOnFailureListener(onError).addOnSuccessListener {
            onSuccess(it.size())
        }
    }

    fun getArtsPage(
        limit: Long,
        after: DocumentSnapshot? = null,
        userId: String? = null,
        updateAfter: (DocumentSnapshot) -> Unit = {},
        onSuccess: (List<GpsArtData>) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        var query = ARTS.orderBy(ART_CREATED_FIELD, Query.Direction.DESCENDING)
        userId?.let {
            query = query.whereEqualTo(ART_AUTHOR_ID_FIELD, userId)
        }
        after?.let {
            query = query.startAfter(it)
        }
        query.limit(limit)
            .get().addOnFailureListener(onError)
            .addOnSuccessListener { artResponse ->
                artResponse.documents.lastOrNull()?.let {
                    updateAfter(it)
                }

                val userIds = mutableListOf<String>()
                val arts = try {
                    artResponse.documents
                        .map { doc ->
                            doc.toObject(GpsArt::class.java)?.also {
                                userIds.add(it.authorId)
                            }
                        }.ifEmpty {
                            onSuccess(listOf())
                            return@addOnSuccessListener
                        }.requireNoNulls()
                } catch (ignored: IllegalArgumentException) {
                    onError(RuntimeException("Cannot deserialise document as GpsArt"))
                    return@addOnSuccessListener
                }

                USERS.whereIn(USER_ID_FIELD, userIds)
                    .get().addOnFailureListener(onError)
                    .addOnSuccessListener(fun(userResponse: QuerySnapshot) {
                        val users = try {
                            userResponse.documents
                                .map { doc -> doc.toObject(UserData::class.java) }
                                .requireNoNulls()
                        } catch (ignored: IllegalArgumentException) {
                            onError(RuntimeException("Cannot deserialize document as UserData"))
                            return
                        }
                        onSuccess(arts.map { art ->
                            val authorName = users
                                .filter { it.id == art.authorId }
                                .map { it.name }
                                .firstOrNull() ?: "NOT FOUND"
                            GpsArtData(art, authorName)
                        })
                    })
            }
    }

    fun getUsersSize(
        onSuccess: (Int) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        USERS.get().addOnFailureListener(onError)
            .addOnSuccessListener {
                onSuccess(it.documents.size)
            }
    }

    fun getUsersPage(
        limit: Long,
        after: DocumentSnapshot? = null,
        excludeMe: Boolean = true,
        updateAfter: (DocumentSnapshot) -> Unit = {},
        onSuccess: (List<UserData>) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        var query = USERS.orderBy(USER_ID_FIELD)
        after?.let {
            query = query.startAfter(it)
        }
        query.limit(limit)
            .get().addOnFailureListener(onError)
            .addOnSuccessListener { response ->
                val uid = AUTH.currentUser?.uid
                response.documents.lastOrNull()?.let {
                    updateAfter(it)
                }
                try {
                    var users = response.documents
                        .map { it.toObject(UserData::class.java) }
                        .requireNoNulls()
                    if (excludeMe) {
                        users = users.filter { it.id != uid }
                    }
                    onSuccess(users)
                } catch (ex: IllegalArgumentException) {
                    onError(RuntimeException("Cannot deserialize document as UserData"))
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
        onSuccess: (Uri) -> Unit = {},
        onError: (Exception) -> Unit = {},
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
        }.addOnFailureListener(onError).addOnSuccessListener(onSuccess)

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
        onError: (Exception) -> Unit = {},
        compressionQuality: Int = 100
    ): UploadTask? {
        val userId = AUTH.currentUser?.uid ?: run {
            onError(RuntimeException("User is not authenticated"))
            return null
        }
        val artId = UUID.randomUUID().toString()

        return uploadImage(
            preview, artId,
            { uri ->
                val gpsArt = GpsArt(artId, userId, name, polylines, previewUrl = uri.toString())
                ARTS.document(artId).set(gpsArt).addOnFailureListener(onError)
                    .addOnSuccessListener {
                        onSuccess(artId)
                    }
            }, onError, compressionQuality
        )
    }

    fun tryToAddNewUserData(
        id: String,
        name: String,
        imageUrl: String?,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val user = USERS.document(id)
        user.get().addOnFailureListener(onError).addOnSuccessListener {
            if (!it.exists()) {
                user.set(UserData(id, name, imageUrl)).addOnFailureListener(onError)
                    .addOnSuccessListener {
                        onSuccess()
                    }
            }
        }
    }

    fun changeUsername(
        name: String,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val user = AUTH.currentUser ?: run {
            onError(RuntimeException("User is not authenticated"))
            return
        }
        user.updateProfile(userProfileChangeRequest {
            displayName = name
        }).addOnFailureListener(onError)
            .addOnSuccessListener {
                USERS.document(user.uid).update(USER_NAME_FIELD, name).addOnFailureListener(onError)
                    .addOnSuccessListener {
                        onSuccess()
                    }
            }
    }

    fun deleteAccount(
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {},
        infoDeleted: () -> Unit = {}
    ) {
        val user = AUTH.currentUser ?: run {
            onError(RuntimeException("User is not authenticated"))
            return
        }
        val uid = user.uid

        ARTS
            .whereEqualTo(ART_AUTHOR_ID_FIELD, uid)
            .get().addOnFailureListener(onError)
            .addOnSuccessListener { userArts ->
                DB.runBatch { batch ->
                    for (document in userArts.documents) {
                        document?.let {
                            batch.delete(it.reference)
                        }
                    }
                    batch.delete(USERS.document(uid))
                }.addOnFailureListener(onError)
                    .addOnSuccessListener {
                        infoDeleted()
                        user.delete().addOnFailureListener(onError)
                            .addOnSuccessListener {
                                onSuccess()
                            }
                    }
            }
    }

    fun deleteArt(artId: String, onSuccess: () -> Unit = {}, onError: (Exception) -> Unit = {}) {
        val userId = AUTH.currentUser?.uid ?: run {
            onError(RuntimeException("User is not authenticated"))
            return
        }
        val art = ARTS.document(artId)
        art.get().addOnSuccessListener {
            val data = it.toObject(GpsArt::class.java)
            if (data == null) {
                onError(RuntimeException("Cannot cast data to GpsArt"))
                return@addOnSuccessListener
            }
            if (data.authorId != userId) {
                onError(RuntimeException("You are not author of this art"))
            } else {
                art.delete().addOnFailureListener(onError)
                    .addOnSuccessListener {
                        onSuccess()
                    }
            }
        }
    }

    fun renameArt(
        artId: String,
        newName: String,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val userId = AUTH.currentUser?.uid ?: run {
            onError(RuntimeException("User is not authenticated"))
            return
        }
        val art = ARTS.document(artId)
        art.get().addOnSuccessListener {
            val data = it.toObject(GpsArt::class.java)
            if (data == null) {
                onError(RuntimeException("Cannot cast data to GpsArt"))
                return@addOnSuccessListener
            }
            if (data.authorId != userId) {
                onError(RuntimeException("You are not author of this art"))
            } else {
                art.update(ART_NAME_FIELD, newName).addOnFailureListener(onError)
                    .addOnSuccessListener {
                        onSuccess()
                    }
            }
        }
    }
}