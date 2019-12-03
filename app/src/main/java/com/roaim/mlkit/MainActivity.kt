package com.roaim.mlkit

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var iv: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        iv = findViewById(R.id.iv)

        fab.setOnClickListener { view ->
            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
        }

        iv.setOnClickListener {
            recognizeText(it.drawToBitmap())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val bitmap = result.uri
                iv.setImageURI(bitmap)
                recognizeText(iv.drawToBitmap())
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                result.error?.message?.also {
                    Snackbar.make(iv, it, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun recognizeText(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val textRecognizer = FirebaseVision.getInstance()
            .onDeviceTextRecognizer
        textRecognizer.processImage(image)
            .addOnSuccessListener {
                displayRecognizedText(it)
            }
            .addOnFailureListener {
                Snackbar.make(
                    iv,
                    it.message.let { msg -> msg ?: "unknown exception" },
                    Snackbar.LENGTH_LONG
                ).show()
            }
    }

    private fun displayRecognizedText(result: FirebaseVisionText) {
        AlertDialog.Builder(this)
            .setCancelable(true)
            .setMessage(result.text)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
