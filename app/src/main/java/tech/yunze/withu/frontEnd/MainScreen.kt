package tech.yunze.withu.frontEnd

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main_screen.*
import kotlinx.android.synthetic.main.fragment_main.view.*
import tech.yunze.withu.R
import tech.yunze.withu.adapters.ChatsAdapter
import tech.yunze.withu.fragments.ChatsFragment
import tech.yunze.withu.fragments.StatusFragment
import tech.yunze.withu.fragments.StatusUpdateFragment
import tech.yunze.withu.listeners.FailureCallback
import tech.yunze.withu.util.DATA_USERS
import tech.yunze.withu.util.DATA_USER_PHONE
import tech.yunze.withu.util.PERMISSION_REQUEST_CONTACT
import tech.yunze.withu.util.REQUEST_NEW_CHAT
import java.util.jar.Manifest

class MainScreen : AppCompatActivity(), FailureCallback {

    private var mSectionsPagerAdapter: SectionPagerAdapter? = null
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDB = FirebaseFirestore.getInstance()
    private val chatsFragment = ChatsFragment()
    private val statusUpdateFragment = StatusUpdateFragment()
    private val statusFragment = StatusFragment()

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)
        setSupportActionBar(Toolbar)

        chatsFragment.setFailureListener(this)
        statusUpdateFragment.setFailureListener(this)
        statusFragment.setFailureListener(this)

        mSectionsPagerAdapter = SectionPagerAdapter(supportFragmentManager)
        container.adapter = mSectionsPagerAdapter
        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tab))
        tab.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
        resizeTab()
        tab.getTabAt(1)?.select()
        tab.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {

            }

            override fun onTabSelected(p0: TabLayout.Tab?) {
               when(p0?.position){
                   0->{fab.hide()}
                   1->{fab.show()}
                   2->{fab.hide()}
               }
            }
        }
        )
    }

    fun onNewChat(view: View) {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_CONTACTS)){
                AlertDialog.Builder(this)
                    .setTitle("Contacts permission")
                    .setMessage("Thia app requires access to your contacts to initiate a conversation")
                    .setPositiveButton("Ask me") { _, _ -> requestContactsPermission() }
                    .setNegativeButton("No"){dialog, which ->  }
                    .show()
            } else {
                requestContactsPermission()
            }
        } else {
            startNewActivity()
        }
    }

    private fun requestContactsPermission(){
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_CONTACTS), PERMISSION_REQUEST_CONTACT)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            PERMISSION_REQUEST_CONTACT ->{
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startNewActivity()
                }
            }
        }
    }

    private fun startNewActivity(){
        startActivityForResult(ContactsActivity.newIntent(this), REQUEST_NEW_CHAT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_NEW_CHAT -> {
                    val name = data?.getStringExtra(PARAM_NAME) ?: ""
                    val phone = data?.getStringExtra(PARAM_PHONE) ?: ""
                    checkNewChatUser(name, phone)
                }
            }
        }
    }

    private fun checkNewChatUser(name: String, phone: String) {
        if(!name.isEmpty() && !phone.isEmpty()) {
            firebaseDB.collection(DATA_USERS)
                .whereEqualTo(DATA_USER_PHONE, phone.replace("\\s".toRegex(), ""))
                .get()
                .addOnSuccessListener {
                    if(it.documents.size > 0) {
                        chatsFragment.newChat(it.documents[0].id)
                    } else {
                        AlertDialog.Builder(this)
                            .setTitle("User not found ${phone.replace("\\s".toRegex(), "")}")
                            .setMessage("$name does not have an account. Send them an SMS to install this app.")
                            .setPositiveButton("OK") { dialog, which ->
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.data = Uri.parse("sms:$phone")
                                intent.putExtra("sms_body", "Hi I am using WithU App, come and download it. I want use it with you")
                                startActivity(intent)
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "An error happened, Pls try again later", Toast.LENGTH_SHORT).show()
                    it.printStackTrace()
                }
        }
    }

    private fun resizeTab(){
        val layout = (tab.getChildAt(0) as LinearLayout).getChildAt(0) as
        LinearLayout
        val layoutParcs = layout.layoutParams as LinearLayout.LayoutParams
        layoutParcs.weight = 0.4f
        layout.layoutParams = layoutParcs

    }

    override fun onResume() {
        super.onResume()
        if(firebaseAuth.currentUser == null){
            startActivity(LoginActivity.newIntent(this))
            finish()
        }
        chatsFragment.clickTime = 0
        ChatsAdapter.totalMessageRead = 0
        ChatsAdapter.totalMessageSent = 0
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //return super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_profile -> onProfile()
            R.id.action_logout -> onLogOut()
        }
        val id = item?.itemId
        if(id == R.id.action_logout){
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    private fun onProfile(){
        startActivity(ProfileActivity.newIntent(this))

    }    private fun onLogOut(){
        firebaseAuth.signOut()
        startActivity(LoginActivity.newIntent(this))
        finish()

    }

    inner class SectionPagerAdapter(fn: FragmentManager): FragmentPagerAdapter(fn){
        override fun getItem(position: Int): Fragment {
            return when(position){
                0 -> statusUpdateFragment
                1 -> chatsFragment
                else -> statusFragment
            }
        }

        override fun getCount(): Int {
            return 3
        }


    }
    companion object{
        val PARAM_NAME = "Param name"
        val PARAM_PHONE = "Param phone"
        fun newIntent(context: Context) = Intent(context,MainScreen::class.java)
    }

    override fun onUserFailure() {
        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
        startActivity(LoginActivity.newIntent(this))
        finish()
    }

}
