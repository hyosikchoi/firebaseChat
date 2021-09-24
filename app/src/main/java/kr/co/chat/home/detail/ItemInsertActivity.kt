package kr.co.chat.home.detail

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kr.co.chat.R
import kr.co.chat.databinding.ActivityItemInsertBinding

class ItemInsertActivity : AppCompatActivity() {

    private lateinit var binding : ActivityItemInsertBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemInsertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(intent.hasExtra(LATITUDE) && intent.hasExtra(LONGITUDE)) {
            Toast.makeText(this , "latitude : ${intent.getDoubleExtra(LATITUDE , 37.0)}" +
                    ",longitude : ${intent.getDoubleExtra(LONGITUDE , 37.0)}" , Toast.LENGTH_SHORT).show()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left , R.anim.slide_out_right)
    }

    companion object {
        const val LATITUDE = "latitude"

        const val LONGITUDE = "longitude"
    }

}