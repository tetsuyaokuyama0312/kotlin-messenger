package com.to.kotlinmessenger.util

import android.content.Context
import com.to.kotlinmessenger.R
import com.to.kotlinmessenger.util.FirebaseExceptionHandler.FirebaseAuthExceptionErrorCode.EMAIL_ALREADY_IN_USE
import com.to.kotlinmessenger.util.FirebaseExceptionHandler.FirebaseAuthExceptionErrorCode.INVALID_EMAIL
import com.to.kotlinmessenger.util.FirebaseExceptionHandler.FirebaseAuthExceptionErrorCode.WEAK_PASSWORD
import com.google.firebase.auth.FirebaseAuthException

/**
 * Firebase関連の例外ハンドラ。
 */
object FirebaseExceptionHandler {
    /**
     * FirebaseAuthExceptionのエラーコード。
     */
    private object FirebaseAuthExceptionErrorCode {
        /** 不正なEメールアドレス */
        const val INVALID_EMAIL = "ERROR_INVALID_EMAIL"

        /** Eメールアドレスがすでに使われている */
        const val EMAIL_ALREADY_IN_USE = "ERROR_EMAIL_ALREADY_IN_USE"

        /** パスワードが弱い */
        const val WEAK_PASSWORD = "ERROR_WEAK_PASSWORD"
    }

    /**
     * 例外オブジェクトを元にエラーメッセージを取得する。
     *
     * @param context
     * @param ex
     * @return エラーメッセージ
     */
    fun getErrorMessage(context: Context, ex: Exception): String {
        if (ex is FirebaseAuthException) {
            return getErrorMessageFromAuthEx(context, ex)
        }
        return ex.message ?: ""
    }

    /**
     * FirebaseAuthExceptionからエラーメッセージを取得する。
     *
     * @param context
     * @param ex
     * @return エラーメッセージ
     */
    private fun getErrorMessageFromAuthEx(context: Context, ex: FirebaseAuthException) =
        context.run {
            when (ex.errorCode) {
                // 不正なEメールアドレス
                INVALID_EMAIL -> getString(R.string.email_address_badly_message)
                // Eメールアドレスがすでに使われている
                EMAIL_ALREADY_IN_USE -> getString(R.string.email_address_already_in_use_message)
                // パスワードが弱い
                WEAK_PASSWORD -> getString(R.string.password_weak_message)
                // いずれにも該当しない場合は例外メッセージを返却
                else -> ex.message ?: ""
            }
        }
}