package com.tans.tuiutils.mediastore

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.media.MediaScannerConnection
import android.net.Uri
import android.provider.MediaStore
import androidx.annotation.WorkerThread
import java.io.File
import android.provider.MediaStore.Audio.AudioColumns
import android.provider.MediaStore.Images.ImageColumns
import android.provider.MediaStore.Video.VideoColumns
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.fragment.app.Fragment
import com.tans.tuiutils.actresult.startActivityResult

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
    val protection = arrayOf(
        // Common Cols
        AudioColumns._ID,
        AudioColumns.DISPLAY_NAME,
        AudioColumns.SIZE,
        AudioColumns.RELATIVE_PATH,
        AudioColumns.DATE_MODIFIED,
        AudioColumns.MIME_TYPE,
        AudioColumns.DATE_MODIFIED,

        // Audio Cols
        AudioColumns.TITLE,
        AudioColumns.ALBUM_ID,
        AudioColumns.ALBUM,
        AudioColumns.ARTIST_ID,
        AudioColumns.ARTIST,
        AudioColumns.DURATION,
        AudioColumns.BITRATE,
        AudioColumns.TRACK,
        AudioColumns.IS_MUSIC
    )
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
                val relativePath = cursor.getString(AudioColumns.RELATIVE_PATH)
                val mimeType = cursor.getString(AudioColumns.MIME_TYPE)
                val uri = ContentUris.withAppendedId(queryUri, id)
                val dateModified = cursor.getLong(AudioColumns.DATE_MODIFIED)

                val title = cursor.getString(AudioColumns.TITLE)
                val albumId = cursor.getLong(AudioColumns.ALBUM_ID)
                val album = cursor.getString(AudioColumns.ALBUM)
                val artistId = cursor.getLong(AudioColumns.ARTIST_ID)
                val artist = cursor.getString(AudioColumns.ARTIST)
                val duration = cursor.getLong(AudioColumns.DURATION)
                val bitrate = cursor.getInt(AudioColumns.BITRATE)
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
    val protection = arrayOf(
        // Common Cols
        VideoColumns._ID,
        VideoColumns.DISPLAY_NAME,
        VideoColumns.SIZE,
        VideoColumns.RELATIVE_PATH,
        VideoColumns.DATE_MODIFIED,
        VideoColumns.MIME_TYPE,
        VideoColumns.DATE_MODIFIED,

        // Video Cols
        VideoColumns.TITLE,
        VideoColumns.DURATION,
        VideoColumns.WIDTH,
        VideoColumns.HEIGHT,
        VideoColumns.BITRATE,
    )
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
                val relativePath = cursor.getString(VideoColumns.RELATIVE_PATH)
                val mimeType = cursor.getString(VideoColumns.MIME_TYPE)
                val uri = ContentUris.withAppendedId(queryUri, id)
                val dateModified = cursor.getLong(VideoColumns.DATE_MODIFIED)

                val title = cursor.getString(VideoColumns.TITLE)
                val width = cursor.getInt(VideoColumns.WIDTH)
                val height = cursor.getInt(VideoColumns.HEIGHT)
                val duration = cursor.getLong(VideoColumns.DURATION)
                val bitrate = cursor.getInt(VideoColumns.BITRATE)

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
    val protection = arrayOf(
        // Common Cols
        ImageColumns._ID,
        ImageColumns.DISPLAY_NAME,
        ImageColumns.SIZE,
        ImageColumns.RELATIVE_PATH,
        ImageColumns.DATE_MODIFIED,
        ImageColumns.MIME_TYPE,
        ImageColumns.DATE_MODIFIED,

        // Image Cols
        ImageColumns.TITLE,
        ImageColumns.WIDTH,
        ImageColumns.HEIGHT,
        ImageColumns.ORIENTATION
    )
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
                val relativePath = cursor.getString(ImageColumns.RELATIVE_PATH)
                val mimeType = cursor.getString(ImageColumns.MIME_TYPE)
                val uri = ContentUris.withAppendedId(queryUri, id)
                val dateModified = cursor.getLong(ImageColumns.DATE_MODIFIED)

                val title = cursor.getString(ImageColumns.TITLE)
                val width = cursor.getInt(ImageColumns.WIDTH)
                val height = cursor.getInt(ImageColumns.HEIGHT)
                val orientation = cursor.getInt(ImageColumns.ORIENTATION)

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

private fun Cursor.getLong(colName: String): Long {
    return getLongOrNull(getColumnIndex(colName)) ?: -1L
}

private fun Cursor.getString(colName: String): String {
    return getStringOrNull(getColumnIndex(colName)).orEmpty()
}

private fun Cursor.getInt(colName: String): Int {
    return getIntOrNull(getColumnIndex(colName)) ?: -1
}