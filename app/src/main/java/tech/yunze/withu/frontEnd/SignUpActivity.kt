package tech.yunze.withu.frontEnd

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_sign_up.*
import tech.yunze.withu.R
import tech.yunze.withu.util.DATA_USERS
import tech.yunze.withu.util.User

class SignUpActivity : AppCompatActivity() {

    private val firebaseDB = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
    }


    companion object{
        fun newIntent(context: Context) = Intent(context,SignUpActivity::class.java)
    }

    fun onSignup(view: View) {

        if(nameET.text.isNullOrBlank()){
            nameTIL.error = "Only bot does not have name"
            nameTIL.isErrorEnabled = true
            return
        }
        if(phoneET.text.isNullOrBlank()){
            phoneTIL.error = "Such a poor man do not have a phone"
            phoneTIL.isErrorEnabled = true
            return
        }
        if(emailEditText.text.isNullOrBlank()){
            emailTIL.error = "Tell me your Email Human!"
            emailTIL.isErrorEnabled = true
            return
        }
        if(emailEditPassword.text.isNullOrBlank()){
            passWordTIL.error = "Do not wanna set a password? Cannot!"
            passWordTIL.isErrorEnabled = true
            return
        }
        Toast.makeText(applicationContext,"Request is sent to server, pls wait..",Toast.LENGTH_SHORT).show()
        firebaseAuth.createUserWithEmailAndPassword(emailEditText.text.toString(),emailEditPassword.text.toString())
            .addOnCompleteListener {
                if(!it.isSuccessful){
                    Toast.makeText(applicationContext,"Sign up error: ${it.exception?.localizedMessage}",Toast.LENGTH_SHORT).show()

                } else if(firebaseAuth !=null) {
                    val email = emailEditText.text.toString()
                    val phone = phoneET.text.toString()
                    val name = nameET.text.toString()
                    val user = User(email,phone,name,"","Hi im new","","")
                    firebaseDB.collection(DATA_USERS).document(firebaseAuth.uid!!).set(user)
                    startActivity(LoginActivity.newIntent(applicationContext))
                    finish()
                    Toast.makeText(applicationContext,"Sign up Successfully",Toast.LENGTH_SHORT).show()
                }
            }
    }
    fun onBackToLogin(view: View) {
        startActivity(LoginActivity.newIntent(applicationContext))
    }
}
