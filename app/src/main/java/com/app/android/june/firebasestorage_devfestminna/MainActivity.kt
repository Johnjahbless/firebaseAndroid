package com.app.android.june.firebasestorage_devfestminna

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private var imageView: ImageView? = null
    private var videoView: VideoView? = null
    private var fileUrl: TextView? = null
    private val PICK_FILE_REQUEST = 71
    private var filePath: Uri? = null
    private var fileType: String? = null
    private var file_url:String? = null
    private var storage: FirebaseStorage? = null
    private var storageReference: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        imageView = findViewById(R.id.imageView)
        videoView = findViewById(R.id.videoView)
        fileUrl = findViewById(R.id.fileUrl)
        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference

        fab.setOnClickListener { view ->
            val intent = Intent()
            intent.type = "*/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST)
        }

        startUpload.setOnClickListener { view ->
            upload_To_storage()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK
            && data != null && data.data != null
        ) {
            filePath = data.data
            fileType = contentResolver.getType(filePath)
            ViewData()
        }
    }
    private fun ViewData() {
        if (fileType!!.contains("image")) {
            videoView!!.visibility = View.GONE
            imageView!!.visibility = View.VISIBLE
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                imageView!!.setImageBitmap(bitmap)

            } catch (e: IOException) {
                e.printStackTrace()
            }

        } else if (fileType!!.contains("video")) {
            videoView!!.visibility = View.VISIBLE
            imageView!!.visibility = View.GONE
            videoView!!.setVideoURI(filePath)
            videoView!!.requestFocus()
            videoView!!.start()

        } else {
            videoView!!.visibility = View.GONE
            imageView!!.visibility = View.VISIBLE

        }
    }


    private fun upload_To_storage() {
        if (filePath != null) {

            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            val ref = storageReference!!.child("files/$fileType$filePath")
            val uploadTask = ref.putFile(filePath!!)
            val urlTask = uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                ref.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    file_url = downloadUri!!.toString()
                    progressDialog.dismiss()
                    fileUrl!!.text = file_url

                }
            }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this@MainActivity, "error: " + e.message, Toast.LENGTH_SHORT).show()
                }
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot
                    .totalByteCount
                progressDialog.setMessage("Uploading " + progress.toInt() + "%")
            }
        } else {
            Toast.makeText(this, "No file is selected", Toast.LENGTH_SHORT).show()
        }
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
