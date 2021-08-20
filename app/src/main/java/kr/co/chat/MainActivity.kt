package kr.co.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import kr.co.chat.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initBottomNav()
    }


    private fun initBottomNav() {

        val homeFragment = HomeFragment()
        val chatFragment = ChatFragment()
        val myPageFragment = MyPageFragment()

        binding.bottomNavi.setOnItemSelectedListener {

            when(it.itemId) {

                R.id.nav_home -> replaceFragment(homeFragment)

                R.id.nav_chat -> replaceFragment(chatFragment)

                R.id.nav_mypage -> replaceFragment(myPageFragment)
            }

            true
        }

    }

    private fun replaceFragment(fragment : Fragment) {

        supportFragmentManager.beginTransaction().replace(R.id.fragment_container , fragment)
            .commit()

    }

}