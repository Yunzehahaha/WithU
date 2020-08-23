package tech.yunze.withu.frontEnd

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_conversation.*
import tech.yunze.withu.R
import tech.yunze.withu.adapters.ChatsAdapter
import tech.yunze.withu.adapters.ConversationAdapter
import tech.yunze.withu.util.*

class ConversationActivity : AppCompatActivity() {

    private val firebaseDB = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val conversationAdapter = ConversationAdapter(arrayListOf(), userId)
    private var chatId: String? = null
    private var imageUrl: String? = null
    private var otherUserId: String? = null
    private var chatName: String? = null
    private var totalMessageRead = 0
    private var totalMessageSent = 0
        set(value) {
            println("YunzeOppa current message sent $value")
            field = value
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)
        reading = true
        totalMessageSent = 0
        chatId = intent.extras?.getString(PARAM_CHAT_ID)
        imageUrl = intent.extras?.getString(PARAM_IMAGE_URL)
        otherUserId = intent.extras?.getString(PARAM_OTHER_USER_ID)
        chatName = intent.extras?.getString(PARAM_CHAT_NAME)

        if(chatId.isNullOrEmpty() || userId.isNullOrEmpty()){
            finish()
        }

        topNameTV.text = chatName
        populateImage(this,imageUrl,topPhotoIV,R.drawable.default_user)

        messageRV.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.VERTICAL
                //stackFromEnd = true
            }
            adapter = conversationAdapter
        }
        messageRV.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View,
                i: Int,
                i1: Int,
                i2: Int,
                i3: Int,
                i4: Int,
                i5: Int,
                i6: Int,
                i7: Int
            ) {
                messageRV.scrollToPosition(messageRV.adapter!!.itemCount-1)
            }
        })
        firebaseDB.collection(DATA_CHATS)
            .document(chatId!!)
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
                                    val message = change.document.toObject(Message::class.java)
                                    message?.let {
                                        conversationAdapter.addMessage(message)
                                        totalMessageSent ++

                                        println("YunzeOppa, $totalMessageSent")
                                        if (ChatsAdapter.totalMessageSent < totalMessageSent) {
                                            ChatsAdapter.totalMessageSent = totalMessageSent
                                        }
                                        messageRV.scrollToPosition(messageRV.adapter!!.itemCount-1)
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        reading = false
        finish()
    }

    override fun onResume() {
        super.onResume()
       totalMessageSent = 0
    }

    fun onSend(v: View) {
       val currentMessageRead =  HashMap<String, Any>()

        totalMessageRead = totalMessageSent +1
        ChatsAdapter.totalMessageRead = totalMessageRead
        currentMessageRead[userId!!] = totalMessageRead
        if(!messageET.text.isNullOrEmpty()){
            val message = Message(userId, messageET.text.toString(), System.currentTimeMillis())
            firebaseDB.collection(DATA_CHATS)
                .document(chatId!!)
                .collection(DATA_CHAT_MESSAGE)
                .document()
                .set(message)

            firebaseDB.collection(DATA_CHATS)
                .document(chatId!!)
                .update(currentMessageRead)

            messageET.setText("",TextView.BufferType.EDITABLE)
        }
    }

    companion object {
        private val PARAM_CHAT_ID = "Chat id"
        private val PARAM_IMAGE_URL = "Image url"
        private val PARAM_OTHER_USER_ID = "Other user id"
        private val PARAM_CHAT_NAME = "Chat name"
        var reading = false

        fun newIntent(context: Context, chatId: String?, imageUrl: String?, otherUserId: String?, chatName: String?): Intent {
            val intent = Intent(context, ConversationActivity::class.java)
            intent.putExtra(PARAM_CHAT_ID,chatId)
            intent.putExtra(PARAM_IMAGE_URL,imageUrl)
            intent.putExtra(PARAM_OTHER_USER_ID,otherUserId)
            intent.putExtra(PARAM_CHAT_NAME,chatName)
            return intent
        }
    }
}
