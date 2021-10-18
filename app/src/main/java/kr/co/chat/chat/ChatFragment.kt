package kr.co.chat.chat

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kr.co.chat.R
import kr.co.chat.databinding.FragmentChatBinding

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private lateinit var binding : FragmentChatBinding

    private val auth : FirebaseAuth by lazy {
        Firebase.auth
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChatBinding.bind(view)

        initView()

    }

    private fun initView() = with(binding) {

        /** 로그인 하지 않았다면 */
        if(auth.currentUser == null) {
            chatListRecyclerView.isGone = true
            alertTextView.isGone = false
        }
        /** 로그인 했다면 */
        else {
            chatListRecyclerView.isGone = false
            alertTextView.isGone = true
        }

    }

}