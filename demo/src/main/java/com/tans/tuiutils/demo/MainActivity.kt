package com.tans.tuiutils.demo

import android.Manifest
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import com.tans.tuiutils.activity.BaseCoroutineStateActivity
import com.tans.tuiutils.clicks.clicks
import com.tans.tuiutils.demo.databinding.ActivityMainBinding
import com.tans.tuiutils.demo.mediastore.MediaStoreActivity
import com.tans.tuiutils.demo.myfragment.MyFragmentActivity
import com.tans.tuiutils.multimedia.pickImageSuspend
import com.tans.tuiutils.multimedia.takeAPhotoSuspend
import com.tans.tuiutils.permission.permissionsRequestSuspend
import com.tans.tuiutils.systembar.annotation.FitSystemWindow
import com.tans.tuiutils.systembar.annotation.SystemBarStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

@SystemBarStyle
@FitSystemWindow
class MainActivity : BaseCoroutineStateActivity<Unit>(Unit) {

    override val layoutId: Int = R.layout.activity_main
    override fun CoroutineScope.firstLaunchInitDataCoroutine() {

    }

    override fun CoroutineScope.bindContentViewCoroutine(contentView: View) {
        val viewBinding = ActivityMainBinding.bind(contentView)
        viewBinding.transparentSystemBarActBt.clicks(this) {
            startActivity(Intent(this@MainActivity, TransparentSystemBarActivity::class.java))
        }

        viewBinding.fitSystemWindowActBt.clicks(this) {
            startActivity(Intent(this@MainActivity, FitSystemWindowActivity::class.java))
        }

        viewBinding.fullScreenActBt.clicks(this) {
            startActivity(Intent(this@MainActivity, FullScreenActivity::class.java))
        }

        viewBinding.yesOrNoDialogBt.clicks(this) {
           runCatching {
                this@MainActivity.supportFragmentManager.showYesOrNoDialogSuspend()
           }.onSuccess {
               Toast.makeText(this@MainActivity, if (it) "Yes" else "No", Toast.LENGTH_SHORT).show()
           }.onFailure {
               Toast.makeText(this@MainActivity, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
           }
        }

        viewBinding.bottomDialogBt.clicks(this) {
            BottomDialog().show(supportFragmentManager, "BottomDialog${System.currentTimeMillis()}")
        }

        viewBinding.takeAPhotoBt.clicks(this) {
            val (outputUri, outputFile) = withContext(Dispatchers.IO) {
                val parentDir = File(filesDir, "take_photos")
                if (!parentDir.exists()) {
                    parentDir.mkdirs()
                }
                val photoFile = File(parentDir, "${System.currentTimeMillis()}")
                if (!photoFile.exists()) {
                    photoFile.createNewFile()
                }
                FileProvider.getUriForFile(this@MainActivity, "${packageName}.provider", photoFile) to photoFile
            }
            val result = runCatching {
                takeAPhotoSuspend(outputUri)
            }
            if (result.isSuccess) {
                if (result.getOrThrow()) {
                    val bitmap = withContext(Dispatchers.IO) {
                        BitmapFactory.decodeFile(outputFile.canonicalPath)
                    }
                    viewBinding.displayIv.setImageBitmap(bitmap)
                } else {
                    outputFile.delete()
                    Toast.makeText(this@MainActivity, "Take photo cancel", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@MainActivity, "Take photo error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
            }
        }

        viewBinding.pickAPictureBt.clicks(this) {
            val result = runCatching { pickImageSuspend() }
            if (result.isSuccess) {
                val pickedUri = result.getOrThrow()
                if (pickedUri == null) {
                    Toast.makeText(this@MainActivity, "Pick image canceled.", Toast.LENGTH_SHORT).show()
                } else {
                    val bitmap = withContext(Dispatchers.IO) {
                        val bytes = contentResolver.openInputStream(pickedUri)?.use { inputStream ->
                            ByteArrayOutputStream().use { outputStream ->
                                inputStream.copyTo(outputStream)
                                outputStream.toByteArray()
                            }
                        }
                        if (bytes != null) {
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        } else {
                            null
                        }
                    }
                    viewBinding.displayIv.setImageBitmap(bitmap)
                }
            } else {
                Toast.makeText(this@MainActivity, "Pick image error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
            }
        }

        viewBinding.fragmentActBt.clicks(this) {
            startActivity(Intent(this@MainActivity, MyFragmentActivity::class.java))
        }

        viewBinding.mediaStoreActBt.clicks(this) {
            val mediaStorePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            val permissionResult = runCatching { permissionsRequestSuspend(*mediaStorePermissions) }
            if (permissionResult.isSuccess) {
                val (granted, _) = permissionResult.getOrThrow()
                if (granted.isNotEmpty()) {
                    startActivity(Intent(this@MainActivity, MediaStoreActivity::class.java))
                } else {
                    Toast.makeText(this@MainActivity, "No MediaStore read permission.", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }
}