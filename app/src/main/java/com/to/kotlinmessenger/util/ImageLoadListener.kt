package com.to.kotlinmessenger.util

import android.widget.ImageView
import java.util.*

/**
 * 画像ファイルのロードイベントに対応して呼び出されるコールバックリスナ。
 */
interface ImageLoadListener : EventListener {

    /**
     * 画像ファイルのロード開始時に呼び出される。
     *
     * @param imageUri 画像ファイルのURI
     * @param targetImageView ロードした画像ファイルを表示するView
     */
    fun onStart(imageUri: Any, targetImageView: ImageView) {}

    /**
     * 画像ファイルのロード終了時に呼び出される。
     *
     * @param imageUri 画像ファイルのURI
     * @param targetImageView ロードした画像ファイルを表示するView
     */
    fun onFinish(imageUri: Any, targetImageView: ImageView) {}

    /**
     * 画像ファイルのロード成功時に呼び出される。
     *
     * @param imageUri 画像ファイルのURI
     * @param targetImageView ロードした画像ファイルを表示するView
     */
    fun onSuccess(imageUri: Any, targetImageView: ImageView) {}

    /**
     * 画像ファイルのロード失敗時に呼び出される。
     *
     * @param ex ロード時に発生した例外
     * @param imageUri 画像ファイルのURI
     * @param targetImageView ロードした画像ファイルを表示するView
     */
    fun onFailure(ex: Exception?, imageUri: Any, targetImageView: ImageView) {}

    companion object {
        /** `ImageLoadListener`のNOP */
        val NOP = object : ImageLoadListener {
            override fun onStart(imageUri: Any, targetImageView: ImageView) {}
            override fun onFinish(imageUri: Any, targetImageView: ImageView) {}
            override fun onSuccess(imageUri: Any, targetImageView: ImageView) {}
            override fun onFailure(ex: Exception?, imageUri: Any, targetImageView: ImageView) {}
        }
    }
}