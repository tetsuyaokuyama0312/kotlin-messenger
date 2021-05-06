package com.to.kotlinmessenger.activity.account

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.to.kotlinmessenger.R
import com.to.kotlinmessenger.activity.message.LatestMessagesActivity
import com.to.kotlinmessenger.util.FirebaseAuthAccessor
import com.to.kotlinmessenger.util.MultiTextWatcher
import com.to.kotlinmessenger.util.logDebug
import kotlinx.android.synthetic.main.activity_login.*

/**
 * ログイン画面。
 */
class LoginActivity : AppCompatActivity() {

    companion object {

        /**
         * `LoginActivity`を起動するためのIntentを作成する。
         *
         * @param context コンテキスト
         * @return `LoginActivity`を起動するためのIntent
         */
        fun createIntent(context: Context): Intent = Intent(context, LoginActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_button_login.setOnClickListener {
            // ログイン処理
            performLogin()
        }

        back_to_registration_text_view.setOnClickListener {
            finish()
        }

        scrollview_login.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            // キーボード表示時に最下部までスクロールする
            scrollToBottomOnLayoutChange(bottom, oldBottom)
        }

        // Eメールアドレス、パスワードがともに入力済みのときのみログインボタンを活性化
        val textWatcher =
            MultiTextWatcher(email_edittext_login, password_edittext_login) { editTexts ->
                login_button_login.isEnabled = editTexts.all { !it.text.isNullOrBlank() }
            }
        email_edittext_login.addTextChangedListener(textWatcher)
        password_edittext_login.addTextChangedListener(textWatcher)
    }

    /**
     * ログイン処理。
     */
    private fun performLogin() {
        val email = email_edittext_login.text.toString()
        val password = password_edittext_login.text.toString()

        logDebug("Attempt login with email/pw: $email/***")

        if (email.isEmpty() || password.isEmpty()) {
            // Eメールアドレス、パスワードが入力されていないと登録ボタンが活性化しないので、ここを通ることはないはず
            Toast.makeText(this, R.string.enter_email_pw_message, Toast.LENGTH_LONG)
                .show()
            return
        }

        // ログイン
        FirebaseAuthAccessor.getAuthInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val msg = getString(R.string.login_success)
                logDebug("msg=$msg, email=$email")
                // 最新メッセージ画面へ遷移
                startActivity(LatestMessagesActivity.createIntent(this))
            }
            .addOnFailureListener {
                logDebug(it.toString())
                val msg = getString(R.string.login_fail)
                logDebug("msg=$msg, email=$email, ex=${it.message}")
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            }
    }

    /**
     * レイアウト変更時に画面の一番下までアニメーション有りでスクロールする。
     *
     * @param bottom レイアウト変更後の最下部を示す値(Y座標)
     * @param oldBottom レイアウト変更前の最下部を示す値(Y座標)
     */
    private fun scrollToBottomOnLayoutChange(bottom: Int, oldBottom: Int) {
        if (bottom < oldBottom) {
            scrollview_login.post {
                scrollview_login.smoothScrollTo(0, bottom)
            }
        }
    }
}