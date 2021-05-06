package com.to.kotlinmessenger.util

import android.content.Context
import com.to.kotlinmessenger.R
import com.to.kotlinmessenger.model.User

/**
 * 画面に表示するユーザー名を取得する。
 *
 * @param context コンテキスト
 * @param user ユーザー
 * @return 画面に表示するユーザー名
 */
fun getDisplayUserName(context: Context, user: User): String {
    val username = user.username
    return if (user.uid == FirebaseAuthAccessor.getUid()) "$username (${context.getString(R.string.myself)})"
    else username
}