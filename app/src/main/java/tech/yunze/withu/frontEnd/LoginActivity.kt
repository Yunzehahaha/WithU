package tech.yunze.withu.frontEnd

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import tech.yunze.withu.R


class LoginActivity : AppCompatActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private var myAlphaAnimation: Animation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        img_loading.visibility = View.INVISIBLE
    }
    fun onLogin(view: View) {
        //Toast.makeText(this,"Login clicked",Toast.LENGTH_LONG).show()
        if(emailEditText.text.isNullOrBlank()){
            emailTIL.error = "Email is blank mother fker!"
            emailTIL.isErrorEnabled = true
            return
        }
        if(emailEditPassword.text.isNullOrBlank()){
            passWordTIL.error = "Password is blank mother fker!"
            passWordTIL.isErrorEnabled = true
            return
        }
        myAlphaAnimation = RotateAnimation(
            0f,
            360f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        (myAlphaAnimation as RotateAnimation).setRepeatCount(10)
        (myAlphaAnimation as RotateAnimation).setDuration(1000);
        (myAlphaAnimation as RotateAnimation).setInterpolator(LinearInterpolator())
        img_loading.animation = myAlphaAnimation
        img_loading.visibility = View.VISIBLE
        //progressLayout.visibility = View.VISIBLE
        firebaseAuth.signInWithEmailAndPassword(emailEditText.text.toString(),emailEditPassword.text.toString())
            .addOnCompleteListener{
                if(!it.isSuccessful){
                    progressLayout.visibility = View.INVISIBLE
                    Toast.makeText(applicationContext,"Login error: ${it.exception?.localizedMessage}",Toast.LENGTH_LONG).show()
                    img_loading.animation = null
                    img_loading.visibility = View.INVISIBLE
                }else{
                    progressLayout.visibility = View.INVISIBLE
                    startActivity(MainScreen.newIntent(applicationContext))
                    img_loading.animation = null
                    img_loading.visibility = View.INVISIBLE
                    finish()
                }
            }
            .addOnFailureListener{
                progressLayout.visibility = View.INVISIBLE
                Toast.makeText(applicationContext,"Login failed", Toast.LENGTH_LONG).show()
                img_loading.animation = null
                img_loading.visibility = View.INVISIBLE
            }
    }
    fun onSignUp(view: View){
        startActivity(SignUpActivity.newIntent(applicationContext))
    }
    companion object{
        fun newIntent(context: Context) = Intent(context,LoginActivity::class.java)
    }
}
