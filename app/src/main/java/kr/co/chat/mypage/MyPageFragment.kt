package kr.co.chat.mypage

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kr.co.chat.DBKey.Companion.USERS
import kr.co.chat.R

import kr.co.chat.databinding.FragmentMypageBinding

class MyPageFragment : Fragment(R.layout.fragment_mypage) {

    private lateinit var binding : FragmentMypageBinding

    private val auth : FirebaseAuth by lazy {
        Firebase.auth
    }

    private val userDB : DatabaseReference by lazy {
        Firebase.database.reference.child(USERS)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMypageBinding.bind(view)

        initLogin()
        initIdPwEditText()
        initSignUp()

        if(auth.currentUser != null) {
            setLoginState()
        }


    }

    private fun successSignIn() {
        if(auth.currentUser == null) {
            Toast.makeText(context , "로그인을 실패했습니다. 아이디 및 비밀번호를 확인해주세요.", Toast.LENGTH_LONG).show()
            return
        }

        Toast.makeText(context , "로그인이 되었습니다.", Toast.LENGTH_LONG).show()
        setLoginState()


        /** 해당 유저 uid 를 가져와서 */
        val userId = auth.currentUser?.uid.orEmpty()
        /** Users 밑에 uid 로 신규 유저 생성 */
        userDB.child(userId)

        /** 신규 유저에 userId 컬럼으로 해당 userId 삽입 */
        val user = mutableMapOf<String,Any>()
        user["userId"] = userId

        /** 신규 유저에 userEmail 컬럼으로 해당 email 삽입 */
        val userEmail = auth.currentUser?.email.orEmpty()
        user["userEmail"] = userEmail

        /** Update */
        userDB.child(userId).updateChildren(user)

    }

    private fun setLoginState() = with(binding) {

        loginButton.text = "로그아웃"
        idEditTextView.text?.clear()
        pwEditTextView.text?.clear()
        idEditTextView.isEnabled = false
        pwEditTextView.isEnabled = false
        signUpButton.isEnabled = false
        loginButton.isEnabled = true

    }

    private fun initIdPwEditText() = with(binding) {

        /** 처음에 id , password 공백이므로 버튼 비활성화 */
        signUpButton.isEnabled = false
        loginButton.isEnabled = false

        /** id edittext 글자수 세기 가능여부 */
        idIntputLayout.isCounterEnabled = true
        /** id edittext 글자수 맥시멈 */
        idIntputLayout.counterMaxLength = 20
        /** pw edittext 글자수 세기 가능여부 */
        pwInputLayout.isCounterEnabled = true
        /** pw edittext 글자수 맥시멈 */
        pwInputLayout.counterMaxLength = 20
        /** inputType = textPassword 보였다가 안보였다가 기능(눈알모양) */
        pwInputLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE

        idEditTextView.addTextChangedListener {
            if(auth.currentUser == null) {
                val enable = idEditTextView.text.isNullOrEmpty() or pwEditTextView.text.isNullOrEmpty()
                signUpButton.isEnabled = !enable
                loginButton.isEnabled = !enable
            }
        }

        pwEditTextView.addTextChangedListener {
            if(auth.currentUser == null) {
                val enable = idEditTextView.text.isNullOrEmpty() or pwEditTextView.text.isNullOrEmpty()
                signUpButton.isEnabled = !enable
                loginButton.isEnabled = !enable

            }
        }

    }

    private fun initSignUp() = with(binding) {
        signUpButton.setOnClickListener {

            val id = idEditTextView.text.toString()
            val password = pwEditTextView.text.toString()

            auth.createUserWithEmailAndPassword(id,password).addOnCompleteListener(requireActivity()) { task ->

                if(task.isSuccessful) {
                    context?.let {
                        Toast.makeText(it , "회원가입에 성공했습니다. 로그인 버튼을 눌러 로그인 해주세요." , Toast.LENGTH_SHORT).show()
                    }
                }

                else {
                    context?.let {
                        Toast.makeText(it , "이미 가입한 이메일 이거나 , 회원가입에 실패했습니다." , Toast.LENGTH_SHORT).show()
                   }
                }

            }

        }

    }

    private fun initLogin() = with(binding) {

        loginButton.setOnClickListener {

            /** 로그인 상태가 아니라면 */
            if(auth.currentUser == null) {

                val id = idEditTextView.text.toString()
                val password = pwEditTextView.text.toString()

                auth.signInWithEmailAndPassword(id,password).addOnCompleteListener(requireActivity()) { task ->

                    if(task.isSuccessful) {
                        /** 로그인 성공 */
                        successSignIn()

                    }

                    else {
                        context?.let {

                            Toast.makeText(context , "로그인을 실패했습니다. 아이디 및 비밀번호를 확인해주세요.", Toast.LENGTH_LONG).show()
                        }

                    }

                }

            }
            /** 로그인 상태라면 */
            else {
                auth.signOut()
                loginButton.text = "로그인"
                idEditTextView.isEnabled = true
                pwEditTextView.isEnabled = true
                signUpButton.isEnabled = true
                Toast.makeText(context , "로그아웃 하였습니다.", Toast.LENGTH_LONG).show()

            }

        }


    }

}