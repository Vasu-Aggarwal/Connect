package com.example.connect.Signup

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.connect.MainActivity
import com.example.connect.R
import com.example.connect.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_signup.*

class signup: AppCompatActivity() {

    val storage by lazy {
        FirebaseStorage.getInstance()
    }

    val auth by lazy {
        FirebaseAuth.getInstance()
    }

    val db by lazy {
        FirebaseFirestore.getInstance()
    }

    lateinit var downloadUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        supportActionBar?.hide()

        //select image from the gallery
        userimg.setOnClickListener{
            checkPermissionForImage()
        }

        nextbutton.setOnClickListener {
            nextbutton.isEnabled = false
            val name = nameedt.text.toString()
            if(name.isEmpty()){
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            }
                //make the image selection compulsory
//            else if(!::downloadUrl.isInitialized){
//                Toast.makeText(this, "Please enter the image", Toast.LENGTH_SHORT).show()
//            }
            else{
                val user = User(name, downloadUrl, auth.uid!!)
                db.collection("users").document(auth.uid!!).set(user)
                    .addOnSuccessListener {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Please try again!", Toast.LENGTH_SHORT).show()
                        nextbutton.isEnabled = true
                    }
            }
        }

    }

    private fun checkPermissionForImage() {
        if((checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED)
            && (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)){
            val permission = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            val permissionwrite = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

            requestPermissions(
                permission,
                1001
            )
            requestPermissions(
                permissionwrite,
                1002
            )
        }
        else{
            pickImageFromGallery()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(
            intent,
            1000
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == 1000){
            data?.data?.let {
                userimg.setImageURI(it)
                uploadImage(it)
            }
        }
    }

    private fun uploadImage(it: Uri) {
        nextbutton.isEnabled = false
        val ref = storage.reference.child("uploads/"+auth.uid.toString()) //name the image as the uid of the user
        val uploadTask = ref.putFile(it)
        uploadTask.continueWithTask{ taskId->
            if(!taskId.isSuccessful){
                taskId.exception?.let {
                    throw it
                }
            }
            ref.downloadUrl
        }.addOnCompleteListener {
            taskId->
            nextbutton.isEnabled = true
            if(taskId.isSuccessful){
                downloadUrl = taskId.result.toString()
                Log.i("URL", "downloadURL: "+downloadUrl)
            }
        }
    }
}