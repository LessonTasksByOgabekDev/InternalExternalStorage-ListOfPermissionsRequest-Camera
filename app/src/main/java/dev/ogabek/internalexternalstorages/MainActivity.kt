package dev.ogabek.internalexternalstorages

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import java.io.*
import java.lang.Exception
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    private val isPersistent: Boolean = true

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
}