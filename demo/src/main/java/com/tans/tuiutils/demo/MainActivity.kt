package com.tans.tuiutils.demo

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.tans.tuiutils.demo.databinding.ActivityMainBinding
import com.tans.tuiutils.multimedia.pickImageSuspend
import com.tans.tuiutils.multimedia.takeAPhotoSuspend
import com.tans.tuiutils.systembar.annotation.FitSystemWindow
import com.tans.tuiutils.systembar.annotation.SystemBarStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

@SystemBarStyle
@FitSystemWindow
class MainActivity : AppCompatActivity(), CoroutineScope by CoroutineScope(Dispatchers.Main) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.transparentSystemBarActBt.setOnClickListener {
            startActivity(Intent(this, TransparentSystemBarActivity::class.java))
        }

        viewBinding.fitSystemWindowActBt.setOnClickListener {
            startActivity(Intent(this, FitSystemWindowActivity::class.java))
        }

        viewBinding.fullScreenActBt.setOnClickListener {
            startActivity(Intent(this, FullScreenActivity::class.java))
        }

        viewBinding.centerDialogBt.setOnClickListener {
            CenterDialog().show(supportFragmentManager, "CenterDialog${System.currentTimeMillis()}")
        }

        viewBinding.bottomDialogBt.setOnClickListener {
            BottomDialog().show(supportFragmentManager, "BottomDialog${System.currentTimeMillis()}")
        }

        viewBinding.takeAPhotoBt.setOnClickListener {
            launch {
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
                val isSuccess = takeAPhotoSuspend(outputUri)
                if (!isSuccess) {
                    Toast.makeText(this@MainActivity, "Take photo fail", Toast.LENGTH_SHORT).show()
                } else {
                    val bitmap = withContext(Dispatchers.IO) {
                        BitmapFactory.decodeFile(outputFile.canonicalPath)
                    }
                    viewBinding.displayIv.setImageBitmap(bitmap)
                }
            }
        }

        viewBinding.pickAPictureBt.setOnClickListener {
            launch {
                val pickedUri = pickImageSuspend()
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
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel("Activity closed.")
    }
}