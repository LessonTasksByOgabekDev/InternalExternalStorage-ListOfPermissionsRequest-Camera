package dev.ogabek.internalexternalstorages

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.core.content.ContextCompat
import java.io.*
import java.lang.Exception
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private val isPersistent: Boolean = true
    private val isInternal: Boolean = false

    private var readPermissionGranted = false
    private var writePermissionGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermission()

        checkStoragePaths()
        createInternalFile()

        initViews()

    }

    private fun initViews() {
        val saveToInternal = findViewById<Button>(R.id.btn_save_internal)
        saveToInternal.setOnClickListener {
            saveToInternal("OgabekDev is Future Mobile Developer")
        }

        val readToInternal = findViewById<Button>(R.id.btn_read_internal)
        readToInternal.setOnClickListener {
            readFromInternal()
        }

        val saveToExternal = findViewById<Button>(R.id.btn_save_external)
        saveToExternal.setOnClickListener {
            saveExternalFile("OgabekDev is Future Mobile Developer")
        }

        val readToExternal = findViewById<Button>(R.id.btn_read_external)
        readToExternal.setOnClickListener {

            readExternalFile()
        }

        val savePicture = findViewById<Button>(R.id.take_photo)
        savePicture.setOnClickListener {
            takePhoto.launch()
        }

    }

    // Internal & External Paths
    private fun checkStoragePaths() {
        val internal_m1 = getDir("custom", 0)
        val internal_m2 = filesDir

        val external_m1 = getExternalFilesDir(null)
        val external_m2 = externalCacheDir
        val external_m3 = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        Log.d("StorageActivity", internal_m1.absolutePath)
        Log.d("StorageActivity", internal_m2.absolutePath)
        Log.d("StorageActivity", external_m1!!.absolutePath)
        Log.d("StorageActivity", external_m2!!.absolutePath)
        Log.d("StorageActivity", external_m3!!.absolutePath)
    }

    // Create File
    private fun createInternalFile() {
        val fileName = "pdp_internal.txt"

        val file = if (isPersistent) {
            File(filesDir, fileName)
        } else {
            File(cacheDir, fileName)
        }

        if (!file.exists()) {
            try {
                file.createNewFile()
                Toast.makeText(this, String.format("File %s has been created", fileName), Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Toast.makeText(this, String.format("File %s creation failed", fileName), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, String.format("File %s already exist", fileName), Toast.LENGTH_SHORT).show()
        }

    }

    // Save data to Internal Database
    private fun saveToInternal(data: String) {
        val fileName = "pdp_internal.txt"
        try {
            val fileOutStream = if (isPersistent) {
                openFileOutput(fileName, MODE_PRIVATE)
            } else {
                val file = File(cacheDir, fileName)
                FileOutputStream(file)
            }

            fileOutStream.write(data.toByteArray(Charset.forName("UTF-8")))
            Toast.makeText(this, String.format("Write to %s successful", fileName), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, String.format("Write to file %s failed", fileName), Toast.LENGTH_SHORT).show()
        }
    }

    // Read data from Internal Database
    private fun readFromInternal() {
        val fileName = "pdp_internal.txt"
        try {
            val fileInputStream = if (isPersistent) {
                openFileInput(fileName)
            } else {
                val file = File(cacheDir, fileName)
                FileInputStream(file)
            }
            val inputStreamReader = InputStreamReader(fileInputStream, Charset.forName("UTF-8"))
            val lines: MutableList<String> = ArrayList()
            val reader = BufferedReader(inputStreamReader)
            var line  = reader.readLine()
            while (line != null) {
                lines.add(line)
                line = reader.readLine()
            }
            val readText = TextUtils.join("\n", lines)
            Toast.makeText(this, readText, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, String.format("Read to file %s failed", fileName), Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestPermission() {
        val hasReadPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29

        val permissionToRequest = mutableListOf<String>()
        if (!readPermissionGranted) {
            permissionToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (!writePermissionGranted) {
            permissionToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissionToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionToRequest.toTypedArray())
        }

    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {permission ->
        readPermissionGranted = permission[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermissionGranted
        writePermissionGranted = permission[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: writePermissionGranted

        if (readPermissionGranted) Toast.makeText(this, "Read Permission Granted", Toast.LENGTH_SHORT).show()
        if (writePermissionGranted) Toast.makeText(this, "Write Permission Granted", Toast.LENGTH_SHORT).show()

    }

    private fun saveExternalFile(data: String) {
        val fileName = "pdp_external.txt"

        val file = if (isPersistent) {
            File(getExternalFilesDir(null), fileName)
        } else {
            File(externalCacheDir, fileName)
        }

        try {
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(data.toByteArray(Charset.forName("UTF-8")))
            Toast.makeText(this, String.format("Write to %s successful", fileName), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, String.format("Write to file %s failed", fileName), Toast.LENGTH_SHORT).show()
        }

    }

    private fun readExternalFile() {
        val fileName = "pdp_external.txt"

        val file = if (isPersistent) {
            File(getExternalFilesDir(null), fileName)
        } else {
            File(externalCacheDir, fileName)
        }

        try {
            val fileInputStream = FileInputStream(file)
            val inputStreamReader = InputStreamReader(fileInputStream, Charset.forName("UTF-8"))
            val lines = mutableListOf<String>()
            val reader = BufferedReader(inputStreamReader)
            var line = reader.readLine()
            while (line != null) {
                lines.add(line)
                line = reader.readLine()
            }
            val readText = TextUtils.join("\n", lines)
            Toast.makeText(this, readText, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, String.format("Read from file %s failed", fileName), Toast.LENGTH_SHORT).show()
        }
    }

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->

        val fileName = UUID.randomUUID().toString()

        val isPhotoSaved = if (isInternal) {
            savePhotoToInternalStorage(fileName, bitmap!!)
        } else {
            if (writePermissionGranted) {
                savePhotoToExternalStorage(fileName, bitmap!!)
            } else {
                false
            }
        }



    }

    private fun savePhotoToInternalStorage(fileName: String, bmp: Bitmap): Boolean {
        return try {
            openFileOutput("$fileName.jpg", MODE_PRIVATE).use { steam ->
                if (!bmp.compress(Bitmap.CompressFormat.JPEG, 100, steam)) {
                    throw IOException("Couldn't save bitmap.")
                }
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private fun savePhotoToExternalStorage(fileName: String, bmp: Bitmap): Boolean {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.WIDTH, bmp.width)
            put(MediaStore.Images.Media.HEIGHT, bmp.height)
        }

        return try {
            contentResolver.insert(collection, contentValues)?.also { uri ->
                contentResolver.openOutputStream(uri).use { outputStream ->
                    if (!bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
                        throw IOException("Couldn't save Bitmap")
                    }
                }
            } ?: throw IOException("Couldn't create Media Store entry")
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }

    }

}