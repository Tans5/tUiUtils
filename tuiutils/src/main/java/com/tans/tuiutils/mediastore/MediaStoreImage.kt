package com.tans.tuiutils.mediastore

import android.net.Uri
import java.io.File

data class MediaStoreImage(
    val id: Long,
    val displayName: String,
    val size: Long,
    val relativePath: String,
    val mimeType: String,
    val uri: Uri,
    // seconds
    val dateModified: Long,

    val title: String,
    val width: Int,
    val height: Int,
    val orientation: Int,

    val file: File?
)