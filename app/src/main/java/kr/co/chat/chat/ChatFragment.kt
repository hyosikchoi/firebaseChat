package kr.co.chat.chat

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kr.co.chat.DBKey
import kr.co.chat.R
import kr.co.chat.chat.adapter.ChatListAdapter
import kr.co.chat.databinding.FragmentChatBinding
import kr.co.chat.home.entity.ChatRoomItem

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private lateinit var binding: FragmentChatBinding

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    /** auth.currentUser 가 정의 된다음에 받아와야 하므로 lateinit 으로 한다. */
    private lateinit var chatDB: DatabaseReference

    private var chatListAdapter: ChatListAdapter = ChatListAdapter()

    /** 채팅방 리스트 listener */
    private val listListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {

            val chatRoomList = mutableListOf<ChatRoomItem>()

            snapshot.children.forEach {

                val chatRoom = it.getValue(ChatRoomItem::class.java)
                chatRoom ?: return@forEach

                chatRoomList.add(chatRoom)
            }
            Log.d("chat" , "${chatRoomList.size}개")
            chatListAdapter.submitList(chatRoomList)
            chatListAdapter.notifyDataSetChanged()
            Log.d("chat" , "${chatListAdapter.currentList.size}개")
            initView()
        }

        override fun onCancelled(error: DatabaseError) {}
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChatBinding.bind(view)

        context?.let {

            binding.chatListRecyclerView.layoutManager = LinearLayoutManager(it)
            binding.chatListRecyclerView.adapter = chatListAdapter
        }

        auth.currentUser?.let { user ->

            chatDB =
                Firebase.database.reference.child(DBKey.USERS).child(user.uid).child(DBKey.CHAT)
            chatDB.addListenerForSingleValueEvent(listListener)
        }

    }

    private fun initView() = with(binding) {

        /** 로그인 하지 않았다면 */
        if (auth.currentUser == null) {
            chatListRecyclerView.isGone = true
            alertTextView.isGone = false
        }
        /** 로그인 했다면 */
        else {
            chatListRecyclerView.isGone = false
            alertTextView.isGone = true
            Log.d("chat" , "${chatListAdapter.currentList.size}개입니다.")
            /** 하지만 채팅방이 하나도 없다면 */
            // Todo adaoter 의 currentList.size (or itemCount) 가 실시간 동기화 안되는 이유 찾기
//            if (chatListAdapter.currentList.size == 0) {
//                chatListRecyclerView.isGone = true
//                alertTextView.isGone = false
//                alertTextView.text = "현재 채팅방이 없습니다."
//            } else {
//                chatListRecyclerView.isGone = false
//                alertTextView.isGone = true
//            }
        }

    }

    override fun onResume() {
        super.onResume()
        auth.currentUser?.let { user ->

            chatDB.addListenerForSingleValueEvent(listListener)
            chatListAdapter.notifyDataSetChanged()
        }

    }

    override fun onPause() {
        super.onPause()
        auth.currentUser?.let { user ->

            chatDB.removeEventListener(listListener)

        }
    }

}