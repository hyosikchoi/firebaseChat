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
                                "????????? ???????????? ???????????????.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@ItemInsertActivity,
                            "????????? ???????????? ???????????????.",
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

        /** ????????? ???????????? ?????? ?????? ??? */
        itemImageAddButton.setOnClickListener {

            auth.currentUser ?: return@setOnClickListener

            when {
                /** ????????? ????????? ???????????? */
                ContextCompat.checkSelfPermission(
                    this@ItemInsertActivity,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    choiceImage()
                }

                /** ????????? ?????? ??? ????????? */
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    /** ????????? ?????? ????????? ?????? ?????????. */
                    showPermissionPopUp()
                }

                /** ?????? ?????? ????????? ????????? ?????? ?????? */
                else -> {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        PERMISSION_REQUEST_CODE
                    )
                }

            }

        }

        /** ???????????? ?????? ?????? ??? */
        itemAddButton.setOnClickListener {
            showProgress()
            val title = idEditTextView.text.toString().orEmpty()
            val price = priceEditTextView.text.toString().orEmpty()
            val sellerId = auth.currentUser?.uid.orEmpty()

            if (selectedUri != null) {
                val photoUri = selectedUri ?: return@setOnClickListener
                upload(photoUri = photoUri, price = price,
                    successUpload = { downloadUrl ->
                        /** ?????? db ????????? */
                        uploadItem(sellerId , title , price , downloadUrl )
                    },
                    errorUpload = {
                        Toast.makeText(this@ItemInsertActivity , "?????? ???????????? ??????????????????." , Toast.LENGTH_SHORT).show()
                        hideProgress()
                    }
                )
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
            .setTitle("??????????????? ???????????????.")
            .setMessage("????????? ????????? ???????????? ?????? ????????? ???????????????.")
            .setPositiveButton("??????") { _, _ ->
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("??????") { _, _ -> }
            .create()
            .show()
    }

    /** Storage ??? image upload ??? downloadUrl ???????????? */
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
                /** ????????? ?????? ??? */
                if (task.isSuccessful) {

                    /** download url ??? ????????????. */
                    storage.reference.child(getString(R.string.storage_path)).child(fileName).downloadUrl
                        .addOnSuccessListener { downloadUrl ->
                            successUpload(downloadUrl.toString())
                        }
                        .addOnFailureListener {
                            errorUpload()
                        }
                }

                /** ????????? ?????? ??? */
                else {
                    errorUpload()
                }

            }

    }

    /** ?????? db ??? entity insert */
    private fun uploadItem(sellerId : String , title : String , price : String , downLoadUrl: String ) {
        /** realtime db ?????? autoIncrement ??? ???????????? ????????????  */
        /** unique ??? id ?????? sellerId+ ?????? ???????????? ??????. */
        /** ??? ???????????? ????????? ????????? ??????????????? ?????? ???????????? ????????? ????????? unique ?????????. */
        val itemEntity = ItemEntity(
            id = "${sellerId}${System.currentTimeMillis()}",
            sellerId = sellerId,
            imageUrl = downLoadUrl,
            price = price,
            title = title,
            createdAt = System.currentTimeMillis(),
            latitude = latitude,
            longitude = longitude
        )

        /** realtime db ??? ???????????? */
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

