package tech.yunze.withu.frontEnd

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile.nameET
import kotlinx.android.synthetic.main.activity_profile.phoneET
import tech.yunze.withu.R
import tech.yunze.withu.util.*

class ProfileActivity : AppCompatActivity() {


    private val firebaseStorage = FirebaseStorage.getInstance().reference
    private val firebaseDB = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var imageUrl: String? = null
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        if(userId.isNullOrEmpty()){

            finish()

        }

        photo.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,REQUEST_CODE_PHOTO)
        }



        populateInfo()
    }


    fun populateInfo(){

        firebaseDB.collection(DATA_USERS)
            .document(userId!!)
            .get()
            .addOnSuccessListener {
                val user = it.toObject(User::class.java)
                imageUrl = user?.imageUrl
                nameET.setText(user?.name,TextView.BufferType.EDITABLE)
                emailET.setText(user?.email, TextView.BufferType.EDITABLE)
                phoneET.setText(user?.phone, TextView.BufferType.EDITABLE)
                if(imageUrl != null){
                    populateImage(this, imageUrl,photo,
                        R.drawable.default_photo)
                }

            }
            .addOnFailureListener{
                it.printStackTrace()
                finish()
            }
    }
    fun onSubmit(view: View) {

        val name = nameET.text.toString()
        val phone = phoneET.text.toString()
        val email = emailET.text.toString()

        val map = HashMap<String, Any>()
        map[DATA_USER_NAME] = name
        map[DATA_USER_EMAIL] = email
        map[DATA_USER_PHONE] = phone

        firebaseDB.collection(DATA_USERS)
            .document(userId!!)
            .update(map)
            .addOnSuccessListener {
                Toast.makeText(this, "Submit successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener{
                Toast.makeText(this, "${it.toString()}", Toast.LENGTH_SHORT).show()
                finish()
            }

    }
    fun onDelete(view: View) {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("This will delete your profile information, are you fking sure")
            .setPositiveButton("Yes"){ _, _ ->
                Toast.makeText(this,"Profile delete", Toast.LENGTH_SHORT).show()
                firebaseDB.collection(DATA_USERS).document(userId!!).delete().addOnSuccessListener {
                    firebaseStorage.child(DATA_IMAGES).child(userId).delete()
                        .addOnSuccessListener {
                            firebaseAuth.currentUser?.delete()
                                ?.addOnSuccessListener {
                                    finish()
                                }?.addOnFailureListener{
                                    finish()
                                }
                        }
                }.addOnFailureListener{
                    finish()
                }



            }
            .setNegativeButton("No"){_, _ ->
                //do nothing
            }
            .show()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PHOTO)
            storeImage(data?.data)
    }
    private fun storeImage(imageUri: Uri?){
        Toast.makeText(this,"Uploading your profile...", Toast.LENGTH_SHORT).show()
        val filepath = firebaseStorage.child(DATA_IMAGES).child(userId!!)

        imageUri?.let {
            filepath.putFile(it)
                .addOnSuccessListener {
                    filepath.downloadUrl
                        .addOnSuccessListener { taskSnapshot ->
                            val url = taskSnapshot.toString()
                            firebaseDB.collection(DATA_USERS)
                                .document(userId)
                                .update(DATA_USER_IMAGE_URL,url)
                                .addOnSuccessListener {
                                    this.imageUrl = url
                                    populateImage(this,this.imageUrl,photo,R.drawable.default_photo)
                                    Toast.makeText(this,"Update successfully", Toast.LENGTH_SHORT).show()
                                }
                        }
                }

                .addOnFailureListener{
                    Toast.makeText(this,"Opps, something is wrong", Toast.LENGTH_SHORT).show()
                }
        }


    }
    companion object{
        fun newIntent(context: Context) = Intent(context,ProfileActivity::class.java)
    }
}
