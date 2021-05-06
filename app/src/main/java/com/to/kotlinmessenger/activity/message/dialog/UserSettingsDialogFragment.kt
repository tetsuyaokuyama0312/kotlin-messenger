package com.to.kotlinmessenger.activity.message.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.to.kotlinmessenger.activity.account.RegisterActivity
import com.to.kotlinmessenger.databinding.DialogUserSettingsBinding
import com.to.kotlinmessenger.model.User
import com.to.kotlinmessenger.util.FirebaseAuthAccessor
import com.to.kotlinmessenger.util.loadProfileImageIntoView

/**
 * ユーザー設定ダイアログ。
 */
class UserSettingsDialogFragment : DialogFragment() {

    companion object {
        /** 現在ログイン中のユーザーを渡す際のキー */
        const val CURRENT_USER_KEY = "CURRENT_USER"

        /** ユーザー設定変更イベントのリスナーを渡す際のキー */
        const val LISTENER_KEY = "LISTENER"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity ?: throw IllegalStateException("Activity must not be null")
        val inflater = requireActivity().layoutInflater;

        // 現在ログイン中のユーザー
        val currentUser = arguments?.getParcelable<User>(CURRENT_USER_KEY)
            ?: throw IllegalStateException("currentUser must be passed")
        // ユーザー設定変更イベントのリスナー
        val listener = arguments?.getParcelable(LISTENER_KEY)
            ?: UserSettingsChangeListener.NOP

        val binding = DialogUserSettingsBinding.inflate(inflater)
        // プロフィール画像
        loadProfileImageIntoView(currentUser, binding.imageviewUserSettings)
        // ユーザー名
        binding.usernameTextviewUserSettings.text = currentUser.username
        // 変更ボタン
        binding.changeButtonUserSettings.setOnClickListener {
            // ユーザー設定変更ダイアログを起動
            val dialog = UserSettingsChangeDialogFragment()
            dialog.apply {
                arguments = Bundle().apply {
                    putParcelable(CURRENT_USER_KEY, currentUser)
                    putParcelable(LISTENER_KEY, listener)
                }
            }.show(childFragmentManager, dialog::class.simpleName)
        }
        // ログアウトボタン
        binding.logoutButtonUserSettings.setOnClickListener {
            FirebaseAuthAccessor.getAuthInstance().signOut()
            startActivity(RegisterActivity.createIntent(activity))
        }
        // キャンセルボタン
        binding.cancelButtonUserSettings.setOnClickListener {
            requireDialog().cancel()
        }

        return Dialog(activity).apply {
            setContentView(binding.root)
        }
    }
}