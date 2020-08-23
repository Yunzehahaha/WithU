package tech.yunze.withu.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_profile.*
import tech.yunze.withu.R
import tech.yunze.withu.frontEnd.ConversationActivity
import tech.yunze.withu.listeners.ChatClickListener
import tech.yunze.withu.util.*

class ChatsAdapter(val chats: ArrayList<String>) :
    RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder>() {

    private var clickListener: ChatClickListener? = null

    fun setOnItemClickListener(listener: ChatClickListener){
        clickListener = listener
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {
        //To change body of created functions use File | Settings | File Templates.
        return ChatsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false))
    }

    override fun getItemCount() = chats.size

    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {
        //To change body of created functions use File | Settings | File Templates.
        holder.bind(chats[position], clickListener)
    }

    class ChatsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val firebaseDB = FirebaseFirestore.getInstance()
        private val userId = FirebaseAuth.getInstance().currentUser?.uid
        private val layout = view.findViewById<RelativeLayout>(R.id.chatLayout)
        private val progressLayout = view.findViewById<LinearLayout>(R.id.progressLayout)
        private var partenerId : String? = null
        private var chatImageUrl: String? = null
        private var chatName: String? = null

        private var chatIV = view.findViewById<ImageView>(R.id.chatIV)
        private var chatNameTV = view.findViewById<TextView>(R.id.chatTV)
        private var notificationTV = view.findViewById<TextView>(R.id.notificationTV)


        @SuppressLint("ClickableViewAccessibility")
        fun bind(chatId: String, listener: ChatClickListener?){
            progressLayout.visibility = View.VISIBLE
            progressLayout.setOnTouchListener {
                v, event ->  true
            }
            firebaseDB.collection(DATA_CHATS)
                .document(chatId)
                .get()
                .addOnSuccessListener {
                    val chatParticipants = it[DATA_CHAT_PARTICIPANTS]
                    if(chatParticipants != null) {
                        for (participant in chatParticipants as ArrayList<String>){
                            if (!participant.equals(userId)) {
                                partenerId = participant
                                firebaseDB.collection(DATA_USERS)
                                    .document(partenerId!!)
                                    .get()
                                    .addOnSuccessListener {
                                        val user = it.toObject(User::class.java)
                                        chatImageUrl = user?.imageUrl
                                        chatName = user?.name
                                        chatNameTV.text = user?.name
                                        populateImage(chatIV.context, user?.imageUrl, chatIV, R.drawable.default_user)
                                        progressLayout.visibility = View.GONE
                                    }
                                    .addOnFailureListener {
                                        it.printStackTrace()
                                        progressLayout.visibility = View.GONE
                                    }
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    progressLayout.visibility = View.GONE
                }
            layout.setOnClickListener {
                listener?.onChatClick(chatId,partenerId,chatImageUrl,chatName)

                if (totalMessageRead ?:0 < totalMessageSent) {

                    val currentMessageRead =  HashMap<String, Any>()
                    currentMessageRead[userId!!] = totalMessageSent

                    firebaseDB.collection(DATA_CHATS)
                        .document(chatId!!)
                        .update(currentMessageRead)
                    notificationTV.visibility = View.INVISIBLE
                }
            }

            firebaseDB.collection(DATA_CHATS)
                .document(chatId)
                .collection(DATA_CHAT_MESSAGE)
                .orderBy(DATA_CHAT_MESSAGE_TIME)
                .addSnapshotListener { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                    if (firebaseFirestoreException != null) {
                        firebaseFirestoreException.printStackTrace()
                        return@addSnapshotListener
                    } else {
                        if(querySnapshot != null) {
                            for(change in querySnapshot.documentChanges) {
                                when(change.type) {
                                    DocumentChange.Type.ADDED -> {
                                        totalMessageSent ++
                                        if (totalMessageRead == 0) {
                                            firebaseDB.collection(DATA_CHATS)
                                                .document(chatId)
                                                .get()
                                                .addOnSuccessListener {
                                                    val getNumber = it[userId!!]
                                                    getNumber?.let {
                                                        totalMessageRead = getNumber.toString().toInt()
                                                    }

                                                    if (totalMessageRead ?:0 < totalMessageSent) {
                                                        notificationTV.visibility = View.VISIBLE
                                                        notificationTV.text = (totalMessageSent - totalMessageRead!!).toString()
                                                    }

                                                }
                                        }
                                        else if (ConversationActivity.reading) {
                                            if (ConversationActivity.reading) {
                                                val currentMessageRead =  HashMap<String, Any>()
                                                currentMessageRead[userId!!] = totalMessageSent
                                                firebaseDB.collection(DATA_CHATS)
                                                    .document(chatId!!)
                                                    .update(currentMessageRead)

                                                notificationTV.visibility = View.INVISIBLE
                                            }
                                        }
                                        else {
                                            if (totalMessageRead ?:0 < totalMessageSent) {
                                                notificationTV.visibility = View.VISIBLE
                                                notificationTV.text = (totalMessageSent - totalMessageRead!!).toString()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        }

    }

    fun updateChats(updateChats: ArrayList<String>) {
        chats.clear()
        chats.addAll(updateChats)
        notifyDataSetChanged()
    }

    companion object {
         var totalMessageSent = 0
         var totalMessageRead: Int? =  0
    }
}