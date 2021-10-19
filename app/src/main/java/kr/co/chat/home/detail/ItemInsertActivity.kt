package kr.co.chat.home.detail

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kr.co.chat.DBKey
import kr.co.chat.DBKey.Companion.ITEMS
import kr.co.chat.R
import kr.co.chat.databinding.ActivityItemInsertBinding
import kr.co.chat.extension.toast
import kr.co.chat.home.entity.ItemEntity

class ItemInsertActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemInsertBinding

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    private val storage: FirebaseStorage by lazy {
        Firebase.storage
    }

    private val itemsDB: DatabaseReference by lazy {
        Firebase.database.reference.child(ITEMS)
    }

    private var selectedUri: Uri? = null

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    private var latitude : Double  = 37.0

    private var longitude : Double = 37.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemInsertBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()

        resultLauncher = with(binding) {
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult(),
                ActivityResultCallback { result ->
                    if (result.resultCode == RESULT_OK) {

                        val uri = result.data?.data

                        if (uri != null) {

                            itemImageView.setImageURI(uri)
                            selectedUri = uri
                        } else {
                            Toast.makeText(
                                this@ItemInsertActivity,
                                "사진을 가져오지 못했습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@ItemInsertActivity,
                            "사진을 가져오지 못했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }

        if (intent.hasExtra(LATITUDE) && intent.hasExtra(LONGITUDE)) {
//            Toast.makeText(
//                this, "latitude : ${intent.getDoubleExtra(LATITUDE, 37.0)}" +
//                        ",longitude : ${intent.getDoubleExtra(LONGITUDE, 37.0)}", Toast.LENGTH_SHORT
//            ).show()

            latitude = intent.getDoubleExtra(LATITUDE, 37.0)
            longitude =  intent.getDoubleExtra(LONGITUDE, 37.0)
        }

    }

    private fun initViews() = with(binding) {

        /** 이미지 등록하기 버튼 클릭 시 */
        itemImageAddButton.setOnClickListener {

            auth.currentUser ?: return@setOnClickListener

            when {
                /** 퍼미션 체크가 되있다면 */
                ContextCompat.checkSelfPermission(
                    this@ItemInsertActivity,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    choiceImage()
                }

                /** 거절을 누른 적 있다면 */
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    /** 퍼미션 관련 팝업을 다시 띄운다. */
                    showPermissionPopUp()
                }

                /** 처음 누른 거라면 퍼미션 체크 요구 */
                else -> {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        PERMISSION_REQUEST_CODE
                    )
                }

            }

        }

        /** 등록하기 버튼 클릭 시 */
        itemAddButton.setOnClickListener {

            val title = idEditTextView.text.toString().orEmpty()
            val price = priceEditTextView.text.toString().orEmpty()
            val sellerId = auth.currentUser?.uid.orEmpty()
            val sellerEmail = auth.currentUser?.email.orEmpty()

            if (selectedUri != null) {
                val photoUri = selectedUri ?: return@setOnClickListener
                showProgress()
                upload(photoUri = photoUri, price = price,
                    successUpload = { downloadUrl ->
                        /** 최종 db 업로드 */
                        uploadItem(sellerId ,sellerEmail , title , price , downloadUrl )
                    },
                    errorUpload = {
                        toast("사진 업로드에 실패했습니다.")
                        hideProgress()
                    }
                )
            }
            else {
                // ToDo no Image 처리
                toast("이미지를 등록하시고 다시 시도해주세요.")
            }
        }

    }

    private fun choiceImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        resultLauncher.launch(intent)

    }

    private fun showPermissionPopUp() {
        AlertDialog.Builder(this)
            .setTitle("권한승인이 필요합니다.")
            .setMessage("앱에서 사진을 불러오기 위한 권한이 필요합니다.")
            .setPositiveButton("확인") { _, _ ->
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("취소") { _, _ -> }
            .create()
            .show()
    }

    /** Storage 에 image upload 후 downloadUrl 받아오기 */
    private fun upload(
        photoUri: Uri,
        price: String,
        successUpload: (String) -> Unit,
        errorUpload: () -> Unit
    ) {
        val fileName = "${price}_${System.currentTimeMillis()}.png"

        storage.reference.child(getString(R.string.storage_path)).child(fileName)
            .putFile(photoUri)
            .addOnCompleteListener { task ->
                /** 업로드 완료 시 */
                if (task.isSuccessful) {

                    /** download url 를 가져온다. */
                    storage.reference.child(getString(R.string.storage_path)).child(fileName).downloadUrl
                        .addOnSuccessListener { downloadUrl ->
                            successUpload(downloadUrl.toString())
                        }
                        .addOnFailureListener {
                            errorUpload()
                        }
                }

                /** 업로드 실패 시 */
                else {
                    errorUpload()
                }

            }

    }

    /** 최종 db 에 entity insert */
    private fun uploadItem(sellerId : String , sellerEmail : String , title : String , price : String , downLoadUrl: String ) {
        /** realtime db 에서 autoIncrement 를 제공하지 않으므로  */
        /** unique 한 id 값을 sellerId+ 현재 시간으로 한다. */
        /** 한 판매자는 동시에 하나의 아이템밖에 등록 못하므로 시간을 더하면 unique 해진다. */
        val itemEntity = ItemEntity(
            id = "${sellerId}${System.currentTimeMillis()}",
            sellerId = sellerId,
            sellerEmail = sellerEmail,
            imageUrl = downLoadUrl,
            price = price,
            title = title,
            createdAt = System.currentTimeMillis(),
            latitude = latitude,
            longitude = longitude
        )

        /** realtime db 에 업데이트 */
        itemsDB.push().setValue(itemEntity)
        hideProgress()
        finish()

    }

    private fun showProgress()  = with(binding) {
        progressBar.isGone = false
    }

    private fun hideProgress() = with(binding) {
        progressBar.isGone = true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    companion object {
        const val LATITUDE = "latitude"

        const val LONGITUDE = "longitude"

        const val PERMISSION_REQUEST_CODE = 1000

    }
}

