package kr.co.chat.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.co.chat.R
import kr.co.chat.databinding.ItemChatListViewholderBinding
import kr.co.chat.home.entity.ChatRoomItem

class ChatListAdapter : ListAdapter<ChatRoomItem,ChatListAdapter.ChatRoomViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {

        return ChatRoomViewHolder(ItemChatListViewholderBinding.inflate(LayoutInflater.from(parent.context) , parent,false))

    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class ChatRoomViewHolder(val binding: ItemChatListViewholderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chatRoomItem: ChatRoomItem) = with(binding) {
            chatRoomTitleTextView.text = chatRoomItem.title
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