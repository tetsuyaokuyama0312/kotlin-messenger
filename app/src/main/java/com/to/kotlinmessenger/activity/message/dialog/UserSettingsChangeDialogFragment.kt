package com.to.kotlinmessenger.activity.message.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.DialogFragment
import com.to.kotlinmessenger.databinding.DialogUserSettingsChangeBinding
import com.to.kotlinmessenger.model.User
import com.to.kotlinmessenger.util.*
import java.util.*

/**
 * ユーザー設定変更ダイアログ。
 */
class UserSettingsChangeDialogFragment : DialogFragment() {

    companion object {
        /** 画像選択のリクエストコード */
        private const val REQUEST_CODE_SELECT_PHOTO = 0
    }

    /** 現在ログイン中のユーザー */
    private lateinit var currentUser: User

    /** ユーザー設定変更イベントのリスナー */
    private lateinit var listener: UserSettingsChangeListener

    private lateinit var binding: DialogUserSettingsChangeBinding

    /** 選択された画像のURI */
    private var selectedPhotoUri: Uri? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity ?: throw IllegalStateException("Activity must not be null")
        val inflater = requireActivity().layoutInflater

        currentUser = arguments?.getParcelable<User>(UserSettingsDialogFragment.CURRENT_USER_KEY)
            ?: throw IllegalStateException("currentUser must be passed")
        listener = arguments?.getParcelable(UserSettingsDialogFragment.LISTENER_KEY)
            ?: UserSettingsChangeListener.NOP

        val binding = DialogUserSettingsChangeBinding.inflate(inflater)
        this.binding = binding

        // 画像選択ボタン
        binding.selectphotoButtonChangeUserSettings.setOnClickListener {
            // 画像選択Activityを起動
            startPhotoSelectionActivity()
        }
        // プロフィール画像
        loadProfileImageIntoView(currentUser, binding.imageviewUserSettingsChange)

        // ユーザー名テキスト
        binding.usernameEdittextUserSettingsChange.setText(currentUser.username)
        binding.usernameEdittextUserSettingsChange.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // 入力されていて、かつ元のユーザー名から変更されているか、画像が選択されている場合は変更ボタンを有効化
                binding.changeButtonUserSettingsChange.isEnabled = !s.isNullOrBlank()
                        && (s.toString() != currentUser.username || selectedPhotoUri != null)
            }
        })

        // 変更ボタン
        binding.changeButtonUserSettingsChange.setOnClickListener {
            // ユーザー設定変更処理
            performChange()
        }
        // キャンセルボタン
        binding.cancelButtonUserSettingsChange.setOnClickListener {
            requireDialog().cancel()
        }

        return Dialog(activity).apply {
            setContentView(binding.root)
        }
    }

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
            loadImageIntoView(selectedPhotoUri!!, binding.imageviewUserSettingsChange)
            // 変更ボタンを有効化
            binding.changeButtonUserSettingsChange.isEnabled = true
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
     * ユーザー設定変更処理。
     */
    private fun performChange() {
        // 変更ボタンを無効化
        binding.changeButtonUserSettingsChange.isEnabled = false

        val newUserName = binding.usernameEdittextUserSettingsChange.text.toString()
        if (newUserName.isNullOrBlank()) {
            // 入力されていなければ変更ボタンが押下できないので、ここを通ることはないはず
            logDebug("newUserName is empty when performChange() is called")
            return
        }

        listener.onStart(currentUser)

        if (selectedPhotoUri == null) {
            // ユーザー名のみ変更がある場合は画像のアップロード無しでデータベースへ保存
            saveUserToFirebaseDatabase(newUserName, null)
        } else {
            // プロフィール画像が変更されている場合は画像をアップロードしてからデータベースへ保存
            uploadImageToFirebaseStorage(newUserName)
        }
        dismissAllDialog()
    }

    /**
     * Firebaseのデータベースにユーザー情報を保存する。
     *
     * @param newUserName 新しいユーザー名
     * @param profileImageUrl ストレージにアップロードしたプロフィール画像のURL
     */
    private fun saveUserToFirebaseDatabase(newUserName: String, profileImageUrl: String? = null) {
        // 更新後のユーザーを作成
        val newUser = if (profileImageUrl == null) currentUser.copy(username = newUserName)
        else
            currentUser.copy(
                username = newUserName,
                profileImageUrl = profileImageUrl
            )

        val ref = FirebaseDatabaseAccessor.getUsersRef(currentUser.uid)
        ref.setValue(newUser)
            .addOnSuccessListener {
                logDebug("Successfully saved new user: oldUser=$currentUser, newUser=$newUser")
                listener.onSuccess(currentUser, newUser)
                listener.onFinish(currentUser, newUser)
            }
            .addOnFailureListener {
                logDebug("Failed to save new user: oldUser=$currentUser, newUser=$newUser, ex=${it}")
                listener.onFailure(it, currentUser, newUser)
                listener.onFinish(currentUser, newUser)
            }
    }

    /**
     * Firebaseのストレージにプロフィール画像をアップロードする。
     *
     * @param newUserName 新しいユーザー名
     */
    private fun uploadImageToFirebaseStorage(newUserName: String) {
        // ファイル名をランダム生成し、アップロード
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorageAccessor.getImagesRef(filename)

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener { task ->
                logDebug("Successfully uploaded image: ${task.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener { uri ->
                    logDebug("File location: $uri")
                    // データベースにユーザを保存
                    saveUserToFirebaseDatabase(newUserName, uri.toString())
                }
            }
            .addOnFailureListener {
                logDebug("Failed to upload image: ${it.message}")
                listener.onFailure(it, currentUser)
                listener.onFinish(currentUser)
            }
    }

    /**
     * すべてのダイアログを閉じる。
     */
    private fun dismissAllDialog() {
        dialog?.dismiss() // すでにダイアログが閉じられている場合は何もしない
        (parentFragment as UserSettingsDialogFragment).dialog?.dismiss()
    }
}