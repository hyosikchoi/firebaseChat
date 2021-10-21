package kr.co.chat.chat.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.co.chat.R
import kr.co.chat.databinding.ItemChatListViewholderBinding
import kr.co.chat.home.entity.ChatRoomItem

class ChatListAdapter : ListAdapter<ChatRoomItem,ChatListAdapter.ChatRoomViewHolder>(diffUtil) {

    private lateinit var chatRoomClick : (ChatRoomItem) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {

        return ChatRoomViewHolder(ItemChatListViewholderBinding.inflate(LayoutInflater.from(parent.context) , parent,false))

    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    fun setClickListener(chatRoomItemClick : (ChatRoomItem) -> Unit) {
        chatRoomClick = chatRoomItemClick
    }


    inner class ChatRoomViewHolder(val binding: ItemChatListViewholderBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(chatRoomItem: ChatRoomItem) = with(binding) {

            roomNumber.text = "${currentList.indexOf(chatRoomItem) + 1}."
            chatRoomTitleTextView.text = chatRoomItem.title

            root.setOnClickListener { chatRoomClick(chatRoomItem) }

        }

    }

    companion object {

        val diffUtil = object : DiffUtil.ItemCallback<ChatRoomItem>() {
            override fun areItemsTheSame(oldItem: ChatRoomItem, newItem: ChatRoomItem): Boolean {
                return oldItem.key == newItem.key
            }

            override fun areContentsTheSame(oldItem: ChatRoomItem, newItem: ChatRoomItem): Boolean {
                return oldItem == newItem
            }
        }
    }

}