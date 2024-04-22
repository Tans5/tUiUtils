package com.tans.tuiutils.mediastore

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.WorkerThread
import java.io.File
import android.provider.MediaStore.Audio.AudioColumns
import android.provider.MediaStore.Images.ImageColumns
import android.provider.MediaStore.MediaColumns
import android.provider.MediaStore.Video.VideoColumns
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.fragment.app.Fragment

@WorkerThread
fun Context.copyAndroidUriToLocalFile(inputUri: Uri, outputFile: File) {
    if (outputFile.isDirectory) {
        error("Wrong output file: ${outputFile.canonicalPath}, need a file.")
    }
    if (!outputFile.exists()) {
        outputFile.createNewFile()
    }
    contentResolver.openInputStream(inputUri)?.let {
        it.use { inputStream ->
            outputFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    } ?: error("Can't open $inputUri.")
}

@WorkerThread
fun Fragment.copyAndroidUriToLocalFile(inputUri: Uri, outputFile: File) {
    val act = this.activity
    com.tans.tuiutils.assert(act != null) { "Fragment's parent activity is null." }
    act!!.copyAndroidUriToLocalFile(inputUri, outputFile)
}

fun Context.insertMediaFilesToMediaStore(filesToMimeType: Map<File, String>) {
    if (filesToMimeType.isEmpty()) {
        return
    }
    val keyValues = filesToMimeType.entries.toList()
    MediaScannerConnection.scanFile(this, keyValues.map { it.key.canonicalPath }.toTypedArray(), keyValues.map { it.value }.toTypedArray(), null)
}

fun Fragment.insertMediaFilesToMediaStore(filesToMimeType: Map<File, String>) {
    val act = this.activity
    com.tans.tuiutils.assert(act != null) { "Fragment's parent activity is null." }
    act!!.insertMediaFilesToMediaStore(filesToMimeType)
}

/**
 * Android SDK 32 need permission [Manifest.permission.READ_EXTERNAL_STORAGE],
 * Android SDK 33 need permission [Manifest.permission.READ_MEDIA_AUDIO]
 */
@WorkerThread
fun Context.queryAudioFromMediaStore(): List<MediaStoreAudio> {
    val queryUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    val protectionList = mutableListOf<String>()
    // Common Cols
    protectionList.add(AudioColumns._ID)
    protectionList.add(AudioColumns.DISPLAY_NAME)
    protectionList.add(AudioColumns.SIZE)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        protectionList.add(AudioColumns.RELATIVE_PATH)
    }
    protectionList.add(AudioColumns.DATE_MODIFIED)
    protectionList.add(AudioColumns.MIME_TYPE)


    // Audio Cols
    protectionList.add(AudioColumns.TITLE)
    protectionList.add(AudioColumns.ALBUM_ID)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        protectionList.add(AudioColumns.ALBUM)
    }
    protectionList.add(AudioColumns.ARTIST_ID)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        protectionList.add(AudioColumns.ARTIST)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        protectionList.add(AudioColumns.DURATION)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        protectionList.add(AudioColumns.BITRATE)
    }
    protectionList.add(AudioColumns.TRACK)
    protectionList.add(AudioColumns.IS_MUSIC)


    val protection = protectionList.toTypedArray()
    val cursor = contentResolver.query(
        queryUri,
        protection,
        null,
        null,
        null
    )
    return if (cursor != null) {
        val audios = mutableListOf<MediaStoreAudio>()
        cursor.use {
            cursor.moveToLast()
            val lastIndex = cursor.position
            for (index in 0 .. lastIndex) {
                cursor.moveToPosition(index)
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(AudioColumns._ID))
                val displayName = cursor.getString(AudioColumns.DISPLAY_NAME)
                val size = cursor.getLong(AudioColumns.SIZE)
                val relativePath = cursor.getRelativePathCompat()
                val mimeType = cursor.getString(AudioColumns.MIME_TYPE)
                val uri = ContentUris.withAppendedId(queryUri, id)
                val dateModified = cursor.getLong(AudioColumns.DATE_MODIFIED)

                val title = cursor.getString(AudioColumns.TITLE)
                val albumId = cursor.getLong(AudioColumns.ALBUM_ID)
                val album = cursor.getAlbumCompat()
                val artistId = cursor.getLong(AudioColumns.ARTIST_ID)
                val artist = cursor.getArtistCompat()
                val duration = cursor.getDurationCompat()
                val bitrate = cursor.getBitrateCompat()
                val track = cursor.getInt(AudioColumns.TRACK)
                val isMusic = cursor.getInt(AudioColumns.IS_MUSIC) == 1

                audios.add(
                    MediaStoreAudio(
                        id = id,
                        displayName = displayName,
                        size = size,
                        relativePath = relativePath,
                        mimeType = mimeType,
                        uri = uri,
                        dateModified = dateModified,

                        title = title,
                        albumId = albumId,
                        album = album,
                        artistId = artistId,
                        artist = artist,
                        duration = duration,
                        bitrate = bitrate,
                        track = track,
                        isMusic = isMusic
                    )
                )
            }
        }
        audios
    } else {
        emptyList()
    }
}

@WorkerThread
fun Fragment.queryAudioFromMediaStore(): List<MediaStoreAudio> {
    val act = this.activity
    com.tans.tuiutils.assert(act != null) { "Fragment's parent activity is null." }
    return act!!.queryAudioFromMediaStore()
}

/**
 * Android SDK 32 need permission [Manifest.permission.READ_EXTERNAL_STORAGE],
 * Android SDK 33 need permission [Manifest.permission.READ_MEDIA_AUDIO]
 */
@WorkerThread
fun Context.queryVideoFromMediaStore(): List<MediaStoreVideo> {
    val queryUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

    val protectionList = mutableListOf<String>()
    // Common Cols
    protectionList.add(VideoColumns._ID)
    protectionList.add(VideoColumns.DISPLAY_NAME)
    protectionList.add(VideoColumns.SIZE)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        protectionList.add(VideoColumns.RELATIVE_PATH)
    }
    protectionList.add(VideoColumns.DATE_MODIFIED)
    protectionList.add(VideoColumns.MIME_TYPE)

    // Video Cols
    protectionList.add(VideoColumns.TITLE)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        protectionList.add(VideoColumns.DURATION)
    }
    protectionList.add(VideoColumns.WIDTH)
    protectionList.add(VideoColumns.HEIGHT)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        protectionList.add(VideoColumns.BITRATE)
    }

    val protection = protectionList.toTypedArray()
    val cursor = contentResolver.query(
        queryUri,
        protection,
        null,
        null,
        null
    )
    return if (cursor != null) {
        val videos = mutableListOf<MediaStoreVideo>()
        cursor.use {
            cursor.moveToLast()
            val lastIndex = cursor.position
            for (index in 0 .. lastIndex) {
                cursor.moveToPosition(index)
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(VideoColumns._ID))
                val displayName = cursor.getString(VideoColumns.DISPLAY_NAME)
                val size = cursor.getLong(VideoColumns.SIZE)
                val relativePath = cursor.getRelativePathCompat()
                val mimeType = cursor.getString(VideoColumns.MIME_TYPE)
                val uri = ContentUris.withAppendedId(queryUri, id)
                val dateModified = cursor.getLong(VideoColumns.DATE_MODIFIED)

                val title = cursor.getString(VideoColumns.TITLE)
                val width = cursor.getInt(VideoColumns.WIDTH)
                val height = cursor.getInt(VideoColumns.HEIGHT)
                val duration = cursor.getDurationCompat()
                val bitrate = cursor.getBitrateCompat()

                videos.add(
                    MediaStoreVideo(
                        id = id,
                        displayName = displayName,
                        size = size,
                        relativePath = relativePath,
                        mimeType = mimeType,
                        uri = uri,
                        dateModified = dateModified,

                        title = title,
                        width = width,
                        height = height,
                        duration = duration,
                        bitrate = bitrate
                    )
                )
            }
        }
        videos
    } else {
        emptyList()
    }
}

@WorkerThread
fun Fragment.queryVideoFromMediaStore(): List<MediaStoreVideo> {
    val act = this.activity
    com.tans.tuiutils.assert(act != null) { "Fragment's parent activity is null." }
    return act!!.queryVideoFromMediaStore()
}

/**
 * Android SDK 32 need permission [Manifest.permission.READ_EXTERNAL_STORAGE],
 * Android SDK 33 need permission [Manifest.permission.READ_MEDIA_AUDIO]
 */
@WorkerThread
fun Context.queryImageFromMediaStore(): List<MediaStoreImage> {
    val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val protectionList = mutableListOf<String>()
    // Common Cols
    protectionList.add(ImageColumns._ID)
    protectionList.add(ImageColumns.DISPLAY_NAME)
    protectionList.add(ImageColumns.SIZE)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        protectionList.add(ImageColumns.RELATIVE_PATH)
    }
    protectionList.add(ImageColumns.DATE_MODIFIED)
    protectionList.add(ImageColumns.MIME_TYPE)

    // Image Cols
    protectionList.add(ImageColumns.TITLE)
    protectionList.add(ImageColumns.WIDTH)
    protectionList.add(ImageColumns.HEIGHT)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        protectionList.add(ImageColumns.ORIENTATION)
    }

    val protection = protectionList.toTypedArray()
    val cursor = contentResolver.query(
        queryUri,
        protection,
        null,
        null,
        null
    )
    return if (cursor != null) {
        val images = mutableListOf<MediaStoreImage>()
        cursor.use {
            cursor.moveToLast()
            val lastIndex = cursor.position
            for (index in 0 .. lastIndex) {
                cursor.moveToPosition(index)
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(ImageColumns._ID))
                val displayName = cursor.getString(ImageColumns.DISPLAY_NAME)
                val size = cursor.getLong(ImageColumns.SIZE)
                val relativePath = cursor.getRelativePathCompat()
                val mimeType = cursor.getString(ImageColumns.MIME_TYPE)
                val uri = ContentUris.withAppendedId(queryUri, id)
                val dateModified = cursor.getLong(ImageColumns.DATE_MODIFIED)

                val title = cursor.getString(ImageColumns.TITLE)
                val width = cursor.getInt(ImageColumns.WIDTH)
                val height = cursor.getInt(ImageColumns.HEIGHT)
                val orientation = cursor.getOrientationCompat()

                images.add(
                    MediaStoreImage(
                        id = id,
                        displayName = displayName,
                        size = size,
                        relativePath = relativePath,
                        mimeType = mimeType,
                        uri = uri,
                        dateModified = dateModified,

                        title = title,
                        width = width,
                        height = height,
                        orientation = orientation
                    )
                )
            }
        }
        images
    } else {
        emptyList()
    }
}

@WorkerThread
fun Fragment.queryImageFromMediaStore(): List<MediaStoreImage> {
    val act = this.activity
    com.tans.tuiutils.assert(act != null) { "Fragment's parent activity is null." }
    return act!!.queryImageFromMediaStore()
}

private fun Cursor.getRelativePathCompat(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        this.getString(MediaColumns.RELATIVE_PATH)
    } else {
        // TODO:
        ""
    }
}

private fun Cursor.getAlbumCompat(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        this.getString(AudioColumns.ALBUM)
    } else {
        "Unknown"
    }
}

private fun Cursor.getArtistCompat(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        this.getString(AudioColumns.ARTIST)
    } else {
        "Unknown"
    }
}

private fun Cursor.getDurationCompat(): Long {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        getLong(MediaColumns.DURATION)
    } else {
        0L
    }
}

private fun Cursor.getBitrateCompat(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        getInt(MediaColumns.BITRATE)
    } else {
        0
    }
}

private fun Cursor.getOrientationCompat(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        getInt(MediaColumns.ORIENTATION)
    } else {
        0
    }
}

private fun Cursor.getLong(colName: String): Long {
    return getLongOrNull(getColumnIndex(colName)) ?: -1L
}

private fun Cursor.getString(colName: String): String {
    return getStringOrNull(getColumnIndex(colName)).orEmpty()
}

private fun Cursor.getInt(colName: String): Int {
    return getIntOrNull(getColumnIndex(colName)) ?: -1
}