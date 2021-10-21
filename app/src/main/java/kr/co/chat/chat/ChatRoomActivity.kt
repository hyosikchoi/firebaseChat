package kr.co.chat.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kr.co.chat.DBKey
import kr.co.chat.R
import kr.co.chat.chat.adapter.ChatRoomChatsAdapter
import kr.co.chat.databinding.ActivityChatRoomBinding
import kr.co.chat.extension.toast
import kr.co.chat.home.entity.ChatItem

class ChatRoomActivity : AppCompatActivity(R.layout.activity_chat_room) {

    private lateinit var binding : ActivityChatRoomBinding

    private val auth : FirebaseAuth by lazy {
        Firebase.auth
    }
    private lateinit var chatsDB : DatabaseReference

    /** 채팅방 seq번호 */
    private val chatRoomKey : String by lazy {
        intent.getStringExtra(ROOM_KEY).toString()
    }

    private val chatRoomChatsAdapter = ChatRoomChatsAdapter()

    private val chatList = mutableListOf<ChatItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatsDB = Firebase.database.reference.child(DBKey.CHATS).child(chatRoomKey)

        /** child 에 이벤트 발생시 실시간 callback 받아온다. */
        chatsDB.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                val chatsItem = snapshot.getValue(ChatItem::class.java)
                Log.d("chatItem" , chatsItem?.message ?: "")
                chatsItem ?: return

                chatList.add(chatsItem)
                chatRoomChatsAdapter.submitList(chatList) {
                    chatRoomChatsAdapter.notifyDataSetChanged()
                }

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        })

        initViews()

    }

    private fun initViews() = with(binding) {

        chatRecyclerView.layoutManager = LinearLayoutManager(this@ChatRoomActivity)
        chatRecyclerView.adapter = chatRoomChatsAdapter

        /** 전송 버튼 클릭 시 */
        chatSendButton.setOnClickListener {

            if(chatEditTextView.text.toString().trim() != "") {

                val chatItem = ChatItem(
                    senderId = auth.currentUser?.uid.orEmpty(),
                    senderEmail = auth.currentUser?.email.orEmpty(),
                    message = chatEditTextView.text.toString()
                )
                /** 해당 채팅방 db 에 채팅메시지 전송 */
                /** 전송 됬다면 childEventListener 통해서 callback 을 통해 실시간 동기화  */
                chatsDB.push().setValue(chatItem)
                chatEditTextView.setText("")
            }

            else {
                toast("메시지를 입력해주세요.")
            }

        }

    }

    companion object {

        const val ROOM_KEY = "key"

        fun getIntent(context: Context , roomKey : String) = Intent(context, ChatRoomActivity::class.java).apply {
            putExtra(ROOM_KEY , roomKey)
        }

    }

}