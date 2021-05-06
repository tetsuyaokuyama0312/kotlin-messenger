package com.to.kotlinmessenger.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

/**
 * FirebaseAuthへのアクセサ。
 */
object FirebaseAuthAccessor {
    /**
     * FirebaseAuthのインスタンスを取得する。
     *
     * @return FirebaseAuthのインスタンス
     */
    fun getAuthInstance() = FirebaseAuth.getInstance()

    /**
     * 現在ログイン中のユーザーのUIDを取得する。
     *
     * @return ログイン中のユーザーのUID(ログインしていない場合はnull)
     */
    fun getUid() = getAuthInstance().uid
}

/**
 * FirebaseDatabaseへのアクセサ。
 */
object FirebaseDatabaseAccessor {
    /** ユーザーテーブルのパス */
    private const val USERS_PATH = "/users"

    /** メッセージテーブルのパス */
    private const val MESSAGES_PATH = "/user-messages"

    /** 最新メッセージテーブルのパス */
    private const val LATEST_MESSAGES_PATH = "/latest-messages"

    /** パスの区切り文字 */
    private const val PATH_SEPARATOR = "/"

    /**
     * FirebaseDatabaseのユーザーテーブルへの参照を取得する。
     *
     * @return ユーザーテーブルへの参照
     */
    fun getUsersRef() = FirebaseDatabase.getInstance().getReference(USERS_PATH)

    /**
     * 指定されたUIDに紐づく、FirebaseDatabaseのユーザーテーブルへの参照を取得する。
     *
     * @param uid UID
     * @return ユーザーテーブルへの参照
     */
    fun getUsersRef(uid: String) =
        FirebaseDatabase.getInstance().getReference("$USERS_PATH/$uid")

    /**
     * 指定されたUIDに紐づく、FirebaseDatabaseのメッセージテーブルへの参照を取得する。
     * UIDが複数指定された場合、再帰的にパス階層を形成してメッセージテーブルへアクセスする。
     * ```
     * 例:
     * uid1, uid2が指定された場合、メッセージテーブル/uid1/uid2 への参照を取得する。
     * ```
     *
     * @param uids UID
     * @return メッセージテーブルへの参照
     */
    fun getMessagesRef(vararg uids: String): DatabaseReference {
        val path = buildPathFromUid(*uids)
        return FirebaseDatabase.getInstance().getReference(MESSAGES_PATH + path)
    }

    /**
     * 指定されたUIDに紐づく、FirebaseDatabaseの最新メッセージテーブルへの参照を取得する。
     * UIDが複数指定された場合、再帰的にパス階層を形成してメッセージテーブルへアクセスする。
     * ```
     * 例:
     * uid1, uid2が指定された場合、最新メッセージテーブル/uid1/uid2 への参照を取得する。
     * ```
     *
     * @param uids UID
     * @return 最新メッセージテーブルへの参照
     */
    fun getLatestMessagesRef(vararg uids: String): DatabaseReference {
        val path = buildPathFromUid(*uids)
        return FirebaseDatabase.getInstance().getReference(LATEST_MESSAGES_PATH + path)
    }

    private fun buildPathFromUid(vararg uids: String) = uids.joinToString(
        separator = PATH_SEPARATOR,
        prefix = PATH_SEPARATOR
    )
}

/**
 * FirebaseStorageへのアクセサ。
 */
object FirebaseStorageAccessor {
    /** 画像ストレージへのパス */
    private const val IMAGES_PATH = "/images"

    /**
     * 指定されたファイル名に紐づく、FirebaseStorageの画像保存領域への参照を取得する。
     *
     * @param filename ファイル名
     */
    fun getImagesRef(filename: String) =
        FirebaseStorage.getInstance().getReference("$IMAGES_PATH/$filename")
}
