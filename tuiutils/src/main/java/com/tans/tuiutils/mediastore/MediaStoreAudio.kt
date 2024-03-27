package com.tans.tuiutils.mediastore

import android.net.Uri

data class MediaStoreAudio(
    val id: Long,
    val displayName: String,
    val size: Long,
    val relativePath: String,
    val mimeType: String,
    val uri: Uri,

    val albumId: Long,
    val album: String,
    val artistId: Long,
    val artist: String,
    val duration: Long,
    val bitrate: Int,
    val track: Int
)
