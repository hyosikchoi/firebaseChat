package kr.co.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.make
import kr.co.chat.chat.ChatFragment
import kr.co.chat.databinding.ActivityMainBinding
import kr.co.chat.home.HomeFragment
import kr.co.chat.mypage.MyPageFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /** 뒤로가기 버튼 두번 시간 재기 위한 변수 */
    private var backKeyPressedTime : Long = 0L

    /** 뒤로가기 버튼 한번 누르면 띄우는 toast */
    private lateinit var toast : Toast

    private val homeFragment = HomeFragment()
    private val chatFragment = ChatFragment()
    private val myPageFragment = MyPageFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initBottomNav()
    }

    private fun initBottomNav() {

        /** 초기화면 설정 */
        replaceFragment(homeFragment , "HOME")

        binding.bottomNavi.setOnItemSelectedListener {

            when(it.itemId) {

                R.id.nav_home -> replaceFragment(homeFragment , "HOME")

                R.id.nav_chat -> replaceFragment(chatFragment , "CHAT")

                R.id.nav_mypage -> replaceFragment(myPageFragment , "MY_PAGE")
            }

            true
        }

    }
    private fun replaceFragment(fragment : Fragment , tag : String) {

        supportFragmentManager.beginTransaction().replace(R.id.fragment_container , fragment , tag)
            .commit()

    }

    override fun onBackPressed() {

        val gethomeFragment = supportFragmentManager.findFragmentByTag("HOME")

           /** homeFragment 가 보이고 있다면 */
           if(gethomeFragment != null && gethomeFragment.isVisible) {
                /** back 버튼을 처음 눌렀거나 처음 누르고 2.5초 뒤에 다시 눌렀을 경우 */
                if(System.currentTimeMillis() > backKeyPressedTime + 2500) {

                    backKeyPressedTime = System.currentTimeMillis()
                    toast=  Toast.makeText(this , "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다." , Toast.LENGTH_SHORT)
                    toast.show()
                }
                /** back 버튼을 한번 누르고 2.5초 안에 다시 눌렀을 경우 */
                else  {
//                    moveTaskToBack(true); // 태스크를 백그라운드로 이동
//                    finishAndRemoveTask(); // 액티비티 종료 + 태스크 리스트에서 지우기
//                    android.os.Process.killProcess(android.os.Process.myPid()); // 앱 프로세스 종료
                    this.finishAndRemoveTask()
                    toast.cancel()
                }

            }
            /** homeFragment 가 아닌 다른 fragment가 보이고 있다면 */
            else {
                replaceFragment(homeFragment , "HOME")
            }



    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }
}