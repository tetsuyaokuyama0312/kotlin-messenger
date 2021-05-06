package com.to.kotlinmessenger.activity.message.dialog

import android.os.Parcelable
import com.to.kotlinmessenger.model.User
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * ユーザー設定変更イベントに対応して呼び出されるコールバックリスナ。
 */
// 本来はインタフェースにしたいところだが、Parcelableにするためopen classにしている
@Parcelize
open class UserSettingsChangeListener : EventListener, Parcelable {

    /**
     * ユーザー設定変更処理の開始時に呼び出される。
     *
     * @param oldUser 設定変更前のユーザー
     */
    open fun onStart(oldUser: User) {}

    /**
     * ユーザー設定変更処理の終了時に呼び出される。
     *
     * @param oldUser 設定変更前のユーザー
     * @param newUser 設定変更後のユーザー
     */
    open fun onFinish(oldUser: User, newUser: User? = null) {}

    /**
     * ユーザー設定変更処理の成功時に呼び出される。
     *
     * @param oldUser 設定変更前のユーザー
     * @param newUser 設定変更後のユーザー
     */
    open fun onSuccess(oldUser: User, newUser: User) {}

    /**
     * ユーザー設定変更処理の失敗時に呼び出される。
     *
     * @param ex 設定変更処理にて発生した例外
     * @param oldUser 設定変更前のユーザー
     * @param newUser 設定変更後のユーザー
     */
    open fun onFailure(ex: Exception, oldUser: User, newUser: User? = null) {}

    companion object {
        /** `UserSettingsChangeListener`のNOP */
        val NOP = object : UserSettingsChangeListener() {
            override fun onStart(oldUser: User) {}
            override fun onFinish(oldUser: User, newUser: User?) {}
            override fun onSuccess(oldUser: User, newUser: User) {}
            override fun onFailure(ex: Exception, oldUser: User, newUser: User?) {}
        }
    }
}