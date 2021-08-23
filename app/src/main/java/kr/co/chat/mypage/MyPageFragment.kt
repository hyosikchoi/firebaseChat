package kr.co.chat.mypage

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kr.co.chat.R
import kr.co.chat.databinding.FragmentMypageBinding

class MyPageFragment : Fragment(R.layout.fragment_mypage) {

    private lateinit var binding : FragmentMypageBinding

    private val auth : FirebaseAuth by lazy {
        Firebase.auth
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMypageBinding.bind(view)

        binding.loginButton.setOnClickListener {
            val id = binding.idEditTextView.text.toString()
            val password = binding.pwEditTextView.text.toString()

            auth.signInWithEmailAndPassword(id,password).addOnCompleteListener(requireActivity()) { task ->

                if(task.isSuccessful) {
                    // todo 로그인 성공
                }

                else {
                    context?.let {

                        Toast.makeText(context , "로그인을 실패했습니다. 아이디 및 비밀번호를 확인해주세요.", Toast.LENGTH_LONG).show()
                    }

                }

            }

        }

    }

}