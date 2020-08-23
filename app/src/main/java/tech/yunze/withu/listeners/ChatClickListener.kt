package tech.yunze.withu.listeners

interface ChatClickListener {
    fun onChatClick(name: String?, otherUserID: String?, chatImageUrl: String?, chatName: String?)
}