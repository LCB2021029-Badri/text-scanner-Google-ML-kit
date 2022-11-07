package com.example.textscanner

import android.graphics.ImageDecoder.ImageInfo
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import com.google.android.material.button.MaterialButton
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    private lateinit var inputImageBtn:MaterialButton
    private lateinit var recognizeTextBtn:MaterialButton
    private lateinit var imageIv: ImageView
    private lateinit var recognizedTextEt:EditText


    //to handle result of camera/gallery permissions in onRequestPermissionResults
    private companion object{
        private const val CAMERA_REQUEST_CODE = 6969
        private const val STORAGE_REQUEST_CODE = 9696
    }
    //uri for the image that we take form camerfa/gallery
    private var imageUri: Uri? = null
    //arrays of permission required to pick image form camera/gallery
    private lateinit var cameraPermission:Array<String>
    private lateinit var storagePermission:Array<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //init UI attributes
        inputImageBtn = findViewById(R.id.inputImageBtn)
        recognizedTextEt = findViewById(R.id.recognizeTextBtn)
        imageIv = findViewById(R.id.imageIv)
        recognizedTextEt = findViewById(R.id.recognizedTextEt)

        //init array of permissions
        cameraPermission = arrayOf(android.Manifest.permission.CAMERA,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermission = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)

        //handle click, showing input image dialog
        inputImageBtn.setOnClickListener {
            showInputImageDialog()
        }
    }

    private fun showInputImageDialog(){

    }

}