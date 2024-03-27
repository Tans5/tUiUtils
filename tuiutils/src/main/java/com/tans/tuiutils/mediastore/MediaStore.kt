package com.tans.tuiutils.mediastore

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
import java.io.File
import android.provider.MediaStore.Audio.AudioColumns
import android.provider.MediaStore.Video.VideoColumns
import android.provider.MediaStore.Images.ImageColumns
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull

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
        // Audio Cols
        AudioColumns.ALBUM_ID,
        AudioColumns.ALBUM,
        AudioColumns.ARTIST_ID,
        AudioColumns.ARTIST,
        AudioColumns.DURATION,
        AudioColumns.BITRATE,
        AudioColumns.TRACK
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

                val albumId = cursor.getLong(AudioColumns.ALBUM_ID)
                val album = cursor.getString(AudioColumns.ALBUM)
                val artistId = cursor.getLong(AudioColumns.ARTIST_ID)
                val artist = cursor.getString(AudioColumns.ARTIST)
                val duration = cursor.getLong(AudioColumns.DURATION)
                val bitrate = cursor.getInt(AudioColumns.BITRATE)
                val track = cursor.getInt(AudioColumns.TRACK)

                audios.add(
                    MediaStoreAudio(
                        id = id,
                        displayName = displayName,
                        size = size,
                        relativePath = relativePath,
                        mimeType = mimeType,
                        uri = uri,
                        albumId = albumId,
                        album = album,
                        artistId = artistId,
                        artist = artist,
                        duration = duration,
                        bitrate = bitrate,
                        track = track
                    )
                )
            }
        }
        audios
    } else {
        emptyList()
    }
}

private fun Cursor.getLong(colName: String): Long {
    return getLongOrNull(getColumnIndex(colName)) ?: -1L
}

private fun Cursor.getString(colName: String): String {
    return getStringOrNull(getColumnIndex(colName)).orEmpty()
}

private fun Cursor.getInt(colName: String): Int {
    return getIntOrNull(getColumnIndex(colName)) ?: 0
}