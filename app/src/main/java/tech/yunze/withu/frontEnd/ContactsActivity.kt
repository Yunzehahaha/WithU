package tech.yunze.withu.frontEnd

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_contacts.*
import tech.yunze.withu.R
import tech.yunze.withu.adapters.ContactsAdapter
import tech.yunze.withu.listeners.ContactsClickListener
import tech.yunze.withu.util.Contact

class ContactsActivity : AppCompatActivity() , ContactsClickListener{

    private val contactList = ArrayList<Contact>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)
        getContact()
    }

    private fun getContact(){
        contactList.clear()
        val newList = ArrayList<Contact>()
        val phone = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        while (phone!!.moveToNext()) {
            val name = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            newList.add(Contact(name,phoneNumber))
        }
        contactList.addAll(newList)
        phone.close()

        setupList()
    }

    fun setupList() {
        progressLayout.visibility = View.GONE
        val contactsAdapter = ContactsAdapter(contactList)
        contactsAdapter.setOnItemClickListener(this)
        contactsRV.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = contactsAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    override fun onContactClicked(name: String?, phone: String?) {
        val intent = Intent()
        intent.putExtra(MainScreen.PARAM_NAME,name)
        intent.putExtra(MainScreen.PARAM_PHONE,phone)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
    companion object{
        fun newIntent(context: Context) = Intent(context,ContactsActivity::class.java)
    }


}
