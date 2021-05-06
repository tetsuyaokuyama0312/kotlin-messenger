package com.to.kotlinmessenger.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * ユーザーを表すデータクラス。
 *
 * @property uid ユーザーID
 * @property username ユーザー名
 * @property profileImageUrl プロフィール画像のURL
 */
@Parcelize
data class User(val uid: String, val username: String, val profileImageUrl: String) : Parcelable {
    constructor() : this("", "", "")
}