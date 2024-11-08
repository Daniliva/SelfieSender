package com.example.selfiesender

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity


class MainActivity : ComponentActivity() {
    private lateinit var selfieImageView: ImageView
    private lateinit var takeSelfieButton: Button
    private lateinit var sendSelfieButton: Button
    private var selfieUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        selfieImageView = findViewById(R.id.imageView)
        takeSelfieButton = findViewById(R.id.btnTakeSelfie)
        sendSelfieButton = findViewById(R.id.btnSendSelfie)

        takeSelfieButton.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }

        sendSelfieButton.setOnClickListener {
            selfieUri?.let { uri -> sendEmailWithSelfie(uri) }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            selfieImageView.setImageBitmap(imageBitmap)
            selfieUri = saveImageToGallery(imageBitmap)
        }
    }

    private fun saveImageToGallery(bitmap: Bitmap): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "selfie.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyApp")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val uri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            contentResolver.openOutputStream(uri).use { outStream ->
                if (outStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
                }
            }
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0) // Снимаем флаг "в процессе"
            contentResolver.update(uri, contentValues, null, null)
        }

        return uri
    }


    private fun sendEmailWithSelfie(imageUri: Uri) {

        try {
            val emailIntent = Intent(Intent.ACTION_SEND)


            emailIntent.type = "plain/text"
            val to = arrayOf("dapashlivy9966@gmail.com")
            emailIntent.putExtra(Intent.EXTRA_EMAIL, to)

            emailIntent.putExtra(Intent.EXTRA_STREAM, imageUri)

            // the mail subject
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "DigiJED Іващев Даніїл")
            //email body
            emailIntent.putExtra(Intent.EXTRA_TEXT, "https://github.com/Daniliva/SelfieSender.git")
            startActivity(Intent.createChooser(emailIntent, "Send email using..."))
        } catch (t: Throwable) {
            Toast.makeText(this, "Request failed try again: $t", Toast.LENGTH_LONG).show()
        }


        /*  val recipient = "dapashlivy9966@gmail.com"
          var subject = "DigiJED "
          val message =""
          val uriBuilder = StringBuilder("mailto:" + Uri.encode(recipient))
          uriBuilder.append("?subject=" + Uri.encode(subject))
          uriBuilder.append("&body=" + Uri.encode(message))
          val uriString = uriBuilder.toString()

          var intent = Intent(Intent.ACTION_SENDTO, Uri.parse(uriString))
          if (intent.resolveActivity(packageManager) != null) {
              startActivity(Intent.createChooser(intent,"Send email using..."))
          }  else {

          }*/

    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
    }
}