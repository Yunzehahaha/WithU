package tech.yunze.withu.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_chats.*

import tech.yunze.withu.R
import tech.yunze.withu.adapters.ChatsAdapter
import tech.yunze.withu.frontEnd.ConversationActivity
import tech.yunze.withu.listeners.ChatClickListener
import tech.yunze.withu.listeners.FailureCallback
import tech.yunze.withu.util.*

class ChatsFragment : Fragment(), ChatClickListener{

    private var chatsAdapter = ChatsAdapter(arrayListOf())
    private val firebaseDB = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var failureListener: FailureCallback? = null
    var clickTime = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (userId.isNullOrEmpty()){
            failureListener?.onUserFailure()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("YunzeOppa", "On View Created")
        chatsAdapter.setOnItemClickListener(this)
        chatsRV.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = chatsAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        firebaseDB.collection(DATA_USERS).document(userId!!).addSnapshotListener{ documentSnapshot, firebaseFirestoreException ->
            if(firebaseFirestoreException == null) {
                refreshChats()
            }
        }
    }

    private fun refreshChats() {
        firebaseDB.collection(DATA_USERS)
            .document(userId!!)
            .get()
            .addOnSuccessListener {
                if (it.contains(DATA_USER_CHATS)) {
                    val parteners = it[DATA_USER_CHATS]
                    val chats = arrayListOf<String>()
                    for (partener in (parteners as HashMap<String, String>).keys){
                        if (parteners[partener] != null) {
                            chats.add(parteners[partener]!!)
                        }
                    }
                    chatsAdapter.updateChats(chats)
                }
            }
    }

    fun newChat(partnerId: String?) {
        firebaseDB.collection(DATA_USERS)
            .document(userId ?: "")
            .get()
            .addOnSuccessListener {
                val userChatParteners = hashMapOf<String, String>()
                if(it[DATA_USER_CHATS] != null && it[DATA_USER_CHATS] is HashMap<*,*>) {
                    val userDocumentMap = it[DATA_USER_CHATS] as HashMap<String,String>
                    if(userDocumentMap.containsKey(partnerId)){
                        return@addOnSuccessListener
                    } else {
                        userChatParteners.putAll(userDocumentMap)
                    }
                }
                firebaseDB.collection(DATA_USERS)
                    .document(partnerId ?: "")
                    .get()
                    .addOnSuccessListener {
                        val partnerChatPartners = hashMapOf<String, String>()
                        if(it[DATA_USER_CHATS] != null && it[DATA_USER_CHATS] is HashMap<*, *>) {
                            val partnerDocumentMap = it[DATA_USER_CHATS] as HashMap<String, String>
                            partnerChatPartners.putAll(partnerDocumentMap)
                        }

                        val chatParticipants = arrayListOf(userId, partnerId)
                        val chat = Chat(chatParticipants)
                        val chatRef = firebaseDB.collection(DATA_CHATS).document()
                        val userRef = firebaseDB.collection(DATA_USERS).document(userId!!)
                        val partnerRef = firebaseDB.collection(DATA_USERS).document(partnerId!!)

                        userChatParteners[partnerId] = chatRef.id
                        partnerChatPartners[userId] = chatRef.id

                        val batch = firebaseDB.batch()
                        batch.set(chatRef,chat)
                        batch.update(userRef, DATA_USER_CHATS, userChatParteners)
                        batch.update(partnerRef, DATA_USER_CHATS, partnerChatPartners)
                        batch.commit()
                    }

            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

    fun setFailureListener(listener: FailureCallback){
        this.failureListener = listener
    }

    override fun onChatClick(
        name: String?,
        otherUserID: String?,
        chatImageUrl: String?,
        chatName: String?
    ) {
        println("YunzeOppa on click $clickTime")
        if(clickTime == 0 ){
            clickTime ++
            println("YunzeOppa on click")
            startActivity(ConversationActivity.newIntent(context!!,name,chatImageUrl,otherUserID,chatName))
        }
    }
}
