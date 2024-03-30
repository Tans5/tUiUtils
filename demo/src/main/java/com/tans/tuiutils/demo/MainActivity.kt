package com.tans.tuiutils.demo

import android.content.Intent
import android.graphics.BitmapFactory
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import com.tans.tuiutils.activity.BaseViewModelFieldActivity
import com.tans.tuiutils.clicks.clicks
import com.tans.tuiutils.demo.databinding.ActivityMainBinding
import com.tans.tuiutils.multimedia.pickImageSuspend
import com.tans.tuiutils.multimedia.takeAPhotoSuspend
import com.tans.tuiutils.systembar.annotation.FitSystemWindow
import com.tans.tuiutils.systembar.annotation.SystemBarStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

@SystemBarStyle
@FitSystemWindow
class MainActivity : BaseViewModelFieldActivity(), CoroutineScope by CoroutineScope(Dispatchers.Main) {

    override val layoutId: Int = R.layout.activity_main

    override fun firstLaunchInitData() {
        println("firstLaunchInitData()")
    }


    override fun bindContentView(contentView: View) {
        onBackPressedDispatcher.addCallback(onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
        val viewBinding = ActivityMainBinding.bind(contentView)
        viewBinding.transparentSystemBarActBt.clicks(this) {
            startActivity(Intent(this, TransparentSystemBarActivity::class.java))
        }

        viewBinding.fitSystemWindowActBt.clicks(this) {
            startActivity(Intent(this, FitSystemWindowActivity::class.java))
        }

        viewBinding.fullScreenActBt.clicks(this) {
            startActivity(Intent(this, FullScreenActivity::class.java))
        }

        viewBinding.centerDialogBt.clicks(this) {
            CenterDialog().show(supportFragmentManager, "CenterDialog${System.currentTimeMillis()}")
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
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel("Activity closed.")
        println("onDestroy()")
    }

    override fun onViewModelCleared() {
        super.onViewModelCleared()
        println("onViewModelCleared()")
    }
}