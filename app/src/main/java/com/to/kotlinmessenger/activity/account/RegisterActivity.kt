package com.to.kotlinmessenger.activity.account

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.to.kotlinmessenger.R
import com.to.kotlinmessenger.activity.message.LatestMessagesActivity
import com.to.kotlinmessenger.model.User
import com.to.kotlinmessenger.util.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

/**
 * ユーザー登録画面。
 */
class RegisterActivity : AppCompatActivity() {

    companion object {
        /** 画像選択のリクエストコード */
        private const val REQUEST_CODE_SELECT_PHOTO = 0

        /** NoImage画像のリソースID */
        private const val NO_IMAGE_RESOURCE_ID = R.drawable.noimage

        /**
         * `RegisterActivity`を起動するためのIntentを作成する。
         *
         * @param context コンテキスト
         * @return `RegisterActivity`を起動するためのIntent
         */
        fun createIntent(context: Context): Intent {
            val intent = Intent(context, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            return intent
        }
    }

    /** 全ユーザー名リスト */
    private val allUserNames = mutableSetOf<String>()

    /** 選択された画像のURI */
    private var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // 全ユーザー名を取得
        fetchAllUserNames()

        register_button_register.setOnClickListener {
            // アカウント登録処理を行う
            performRegister()
        }

        already_have_account_text_view.setOnClickListener {
            // ログインアクティビティを起動
            startActivity(LoginActivity.createIntent(this))
        }

        selectphoto_button_register.setOnClickListener {
            // 画像選択Activityを起動
            startPhotoSelectionActivity()
        }

        scrollview_register.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            // キーボード表示時に最下部までスクロールする
            scrollToBottomOnLayoutChange(bottom, oldBottom)
        }

        // ユーザー名、Eメールアドレス、パスワードがすべて入力済みのときのみ登録ボタンを活性化
        val textWatcher =
            MultiTextWatcher(
                username_edittext_regiter,
                email_edittext_register,
                password_edittext_register
            ) { editTexts ->
                register_button_register.isEnabled = editTexts.all { !it.text.isNullOrBlank() }
            }
        username_edittext_regiter.addTextChangedListener(textWatcher)
        email_edittext_register.addTextChangedListener(textWatcher)
        password_edittext_register.addTextChangedListener(textWatcher)
    }

    // 画像選択Activityから戻ったタイミングで呼び出される
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SELECT_PHOTO && resultCode == Activity.RESULT_OK && data != null) {
            // 画像が選択された場合
            logDebug("Photo was selected")

            // 選択された画像のURIを保存
            selectedPhotoUri = data.data
            if (selectedPhotoUri == null) {
                logDebug("cannot get selected photo uri")
                return
            }

            // 画像を取得して表示
            loadImageIntoView(selectedPhotoUri!!, selectphoto_imageview_register)
            selectphoto_button_register.alpha = 0f
        }
    }

    /**
     * 全ユーザー名を取得する。
     */
    private fun fetchAllUserNames() {
        val ref = FirebaseDatabaseAccessor.getUsersRef()

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children
                    .mapNotNull { it.getValue(User::class.java)?.username }
                    .forEach { allUserNames.add(it) }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /**
     * ユーザーを登録する。
     */
    private fun performRegister() {
        val username = username_edittext_regiter.text.toString()
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            // ユーザー名、Eメールアドレス、パスワードが入力されていないと登録ボタンが活性化しないので、ここを通ることはないはず
            Toast.makeText(this, R.string.enter_username_email_pw_message, Toast.LENGTH_LONG)
                .show()
            return
        }

        if (usernameIsAlreadyInUse(username)) {
            // ユーザー名がすでに使われている場合は作成しない
            val msg =
                "${getString(R.string.create_user_fail)}: ${getString(R.string.username_already_in_use_message)}"
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            logDebug("$msg: username=$username")
            return
        }

        // ユーザー作成
        FirebaseAuthAccessor.getAuthInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) {
                    return@addOnCompleteListener
                }

                val msg = getString(R.string.create_user_success)
                logDebug("$msg with uid: ${it.result?.user?.uid}")
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

                // ストレージに画像をアップロード
                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                logDebug(it.toString())
                val msg =
                    "${getString(R.string.create_user_fail)}: ${
                        FirebaseExceptionHandler.getErrorMessage(
                            this,
                            it
                        )
                    }"
                logDebug(msg)
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            }
    }

    /**
     * ユーザー名がすでに使われているかどうか判定する。
     *
     * @param username ユーザー名
     * @return ユーザー名がすでに使われているならば`true`
     */
    private fun usernameIsAlreadyInUse(username: String): Boolean {
        return allUserNames.contains(username)
    }

    /**
     * Firebaseのストレージにプロフィール画像をアップロードする。
     */
    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) {
            // 画像が選択されていない場合はnoimageを使用する
            selectedPhotoUri = getNoImageUri()
        }

        // ファイル名をランダム生成し、アップロード
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorageAccessor.getImagesRef(filename)

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener { task ->
                logDebug("Successfully uploaded image: ${task.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener { uri ->
                    logDebug("File location: $uri")
                    // データベースにユーザを保存
                    saveUserToFirebaseDatabase(uri.toString())
                }
            }
            .addOnFailureListener {
                logDebug("Failed to upload image: ${it.message}")
            }
    }

    /**
     * NoImage画像のURIを取得する。
     */
    private fun getNoImageUri(): Uri {
        return resources.run {
            Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(getResourcePackageName(NO_IMAGE_RESOURCE_ID))
                .appendPath(getResourceTypeName(NO_IMAGE_RESOURCE_ID))
                .appendPath(getResourceEntryName(NO_IMAGE_RESOURCE_ID))
                .build()
        }
    }

    /**
     * Firebaseのデータベースにユーザー情報を保存する。
     *
     * @param profileImageUrl ストレージにアップロードしたプロフィール画像のURL
     */
    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuthAccessor.getUid() ?: ""
        val ref = FirebaseDatabaseAccessor.getUsersRef(uid)

        val user = User(uid, username_edittext_regiter.text.toString(), profileImageUrl)
        ref.setValue(user)
            .addOnSuccessListener {
                logDebug("Finally we saved the user to Firebase Database")
                // 最新メッセージ画面へ遷移
                startActivity(LatestMessagesActivity.createIntent(this))
            }
            .addOnFailureListener {
                logDebug(it.toString())
            }
    }

    /**
     * 画像選択Activityを起動する。
     */
    private fun startPhotoSelectionActivity() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_SELECT_PHOTO)
    }

    /**
     * レイアウト変更時に画面の一番下までアニメーション有りでスクロールする。
     *
     * @param bottom レイアウト変更後の最下部を示す値(Y座標)
     * @param oldBottom レイアウト変更前の最下部を示す値(Y座標)
     */
    private fun scrollToBottomOnLayoutChange(bottom: Int, oldBottom: Int) {
        if (bottom < oldBottom) {
            scrollview_register.post {
                scrollview_register.smoothScrollTo(0, bottom)
            }
        }
    }
}
