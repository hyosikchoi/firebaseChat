package kr.co.chat.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.co.chat.databinding.ItemChatRoomChatsViewholderBinding
import kr.co.chat.home.entity.ChatItem

class ChatRoomChatsAdapter : ListAdapter<ChatItem , ChatRoomChatsAdapter.ChatsViewHolder>(diffUtil) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {
        return ChatsViewHolder(ItemChatRoomChatsViewholderBinding.inflate(LayoutInflater.from(parent.context) , parent , false))
    }

    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {

        holder.bind(currentList[position])

    }


    inner class ChatsViewHolder(val binding : ItemChatRoomChatsViewholderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chatItem: ChatItem) = with(binding) {

            clientIdTextView.text = chatItem.senderEmail
            descriptionTextView.text = chatItem.message
        }

    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ChatItem>() {

            override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
                return oldItem == newItem
            }
        }
    }

}