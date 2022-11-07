package com.example.textscanner

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageDecoder.ImageInfo
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ContentView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.time.temporal.ValueRange
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

    //for progress dialog
    private lateinit var progressDialog: ProgressDialog

    //for text recognizer
    private lateinit var textRecognizer : TextRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //init UI attributes
        inputImageBtn = findViewById(R.id.inputImageBtn)
        recognizeTextBtn = findViewById(R.id.recognizeTextBtn)
        imageIv = findViewById(R.id.imageIv)
        recognizedTextEt = findViewById(R.id.recognizedTextEt)

        //init array of permissions
        cameraPermission = arrayOf(android.Manifest.permission.CAMERA,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermission = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)

        //init text recognizer
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        //handle click, showing input image dialog
        inputImageBtn.setOnClickListener {
            showInputImageDialog()
        }

        //progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...!")
        progressDialog.setCanceledOnTouchOutside(false)

        //setting recognition button
        recognizeTextBtn.setOnClickListener {
            if(imageUri == null){
                Toast.makeText(this,"Pick image first...!",Toast.LENGTH_SHORT).show()
            }
            else{
                recognizeTextFromImage()
            }
        }
    }

    //using all permissions here
    private fun showInputImageDialog(){
        val popupMenu = PopupMenu(this,inputImageBtn)
        popupMenu.menu.add(Menu.NONE,1,1,"CAMERA")
        popupMenu.menu.add(Menu.NONE,2,2,"GALLERY")
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { menuItem ->
            val id = menuItem.itemId
            if(id ==1){
                //if camera is clicked,check if camera permissions are granted or not
                if(checkCameraPermission()){
                    pickImageCamera()
                }
                else{
                    requestCameraPermission()
                }
            }
            else if(id == 2){
                //if gallery button is clicked,check if gallery permissions are granted or not
                if(checkStoragePermission()){
                    pickImageGallery()
                }
                else{
                    requestStoragePermission()
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    //to store the taken image in mediastore
    private fun pickImageCamera(){
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE,"Sample Title")
        values.put(MediaStore.Images.Media.DESCRIPTION,"Sample Description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri)
        cameraActivityResultLauncher.launch(intent)
    }
    private fun pickImageGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncger.launch(intent)
    }
    private val galleryActivityResultLauncger =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == Activity.RESULT_OK){
                val data = result.data
                imageUri = data!!.data

                imageIv.setImageURI(imageUri)
            }
            else{
                Toast.makeText(this,"Cancelled....!",Toast.LENGTH_SHORT).show()
            }
        }
    private val cameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if(result.resultCode == Activity.RESULT_OK){
                imageIv.setImageURI(imageUri)
            }
            else{
                Toast.makeText(this,"Cancelled...!",Toast.LENGTH_SHORT).show()
            }
        }
    private fun checkStoragePermission(): Boolean{
        return ContextCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
    private fun checkCameraPermission(): Boolean{
        val cameraResult = ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val storageResult = ContextCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        return cameraResult && storageResult
    }
    private fun requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermission, STORAGE_REQUEST_CODE)
    }
    private fun requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermission, CAMERA_REQUEST_CODE)
    }
    //handles permission(s) result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            CAMERA_REQUEST_CODE ->{
                if(grantResults.isNotEmpty()){
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if(cameraAccepted && storageAccepted){
                        pickImageCamera()
                    }
                    else{
                        Toast.makeText(this,"camera and storage permissions are required...!",Toast.LENGTH_SHORT).show()
                    }
                }
            }
            STORAGE_REQUEST_CODE ->{
                if(grantResults.isNotEmpty()){
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if(storageAccepted){
                        pickImageGallery()
                    }
                    else{
                        Toast.makeText(this,"storage permission is required...!",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    //function for text recognition
    private fun recognizeTextFromImage(){
        //set message and show progress dialog
        progressDialog.setMessage("Preparing Image...Please wait....")
        progressDialog.show()

        try{
            val inputImage = InputImage.fromFilePath(this,imageUri!!)
            progressDialog.setMessage("Recognizing text...!")
            val textTaskResult = textRecognizer.process(inputImage)
                .addOnSuccessListener {text ->
                    progressDialog.dismiss()
                    //getting recognized text
                    val recognizedText = text.text
                    //set the recognized text to edit text
                    recognizedTextEt.setText(recognizedText)
            }
                .addOnFailureListener {e->
                    progressDialog.dismiss()
                    Toast.makeText(this,"Failed to recognize image due to ${e.message}",Toast.LENGTH_SHORT).show()
                }
        }catch(e:java.lang.Exception){
            progressDialog.dismiss()
            Toast.makeText(this,"Failed to prepare image due to ${e.message}",Toast.LENGTH_SHORT).show()
        }
    }

}