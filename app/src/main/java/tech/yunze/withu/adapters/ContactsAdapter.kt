package tech.yunze.withu.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tech.yunze.withu.R
import tech.yunze.withu.listeners.ContactsClickListener
import tech.yunze.withu.util.Contact

class ContactsAdapter(private val contacts: ArrayList<Contact>):
    RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>() {
    private var clickListener: ContactsClickListener? = null

    class ContactsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var layout = view.findViewById<LinearLayout>(R.id.contactLayout)
        private var nameTV = view.findViewById<TextView>(R.id.contactNameTV)
        private var phoneTV = view.findViewById<TextView>(R.id.contactNumberTV)

        fun bind(contact: Contact, listener: ContactsClickListener?) {
            nameTV.text = contact.name
            phoneTV.text = contact.phone
            layout.setOnClickListener {
                listener?.onContactClicked(contact.name, contact.phone)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        return ContactsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.irtem_contact,parent,false))
    }

    override fun getItemCount() = contacts.size

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        holder.bind(contacts[position], clickListener)
    }

    fun setOnItemClickListener(listener: ContactsClickListener){
        clickListener = listener
        notifyDataSetChanged()
    }
}