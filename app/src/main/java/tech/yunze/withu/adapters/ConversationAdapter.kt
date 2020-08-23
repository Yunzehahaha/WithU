package tech.yunze.withu.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tech.yunze.withu.R
import tech.yunze.withu.listeners.RecycleViewMoveDown
import tech.yunze.withu.util.Message

class ConversationAdapter(var messages: ArrayList<Message>, val userId: String?):
    RecyclerView.Adapter<ConversationAdapter.MessageViewHolder>() {

    class MessageViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        fun bind(message: Message){
            view.findViewById<TextView>(R.id.messageTV).text = message.message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return if (viewType == MESSAGE_CURRENT_USER) {
            MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_current_user_message,parent,false))
        } else {
            MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_otheruser_message,parent,false))
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
         holder.bind(messages[position])
        // Log.d("YunzeOppa", "position $position size ${messages.size}" )

    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].sentBy.equals(userId)) {
            MESSAGE_CURRENT_USER
        } else {
            MESSAGE_OTHER_USER
        }
    }

    fun addMessage(message: Message){
        messages.add(message)
        notifyDataSetChanged()
    }
    companion object {
        val MESSAGE_CURRENT_USER = 1
        val MESSAGE_OTHER_USER = 2
    }
}