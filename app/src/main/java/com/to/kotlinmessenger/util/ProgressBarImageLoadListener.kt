package com.to.kotlinmessenger.util

import android.view.MenuItem
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.view.isVisible

/**
 * ロード開始時にプログレスバーを表示し、ロード終了時にプログレスバーを非表示にする`ImageLoadListener`実装。
 *
 * なおロード成功時は何もせず、ロード失敗時はログ出力する。
 *
 * @property progressBar 表示対象のプログレスバー(nullの場合は何もしない)
 * @property progressBarItem 表示対象のプログレスバーのメニューアイテム(nullの場合は何もしない)
 */
class ProgressBarImageLoadListener(
    private val progressBar: ProgressBar?,
    private val progressBarItem: MenuItem?
) : ImageLoadListener {
    constructor(progressBar: ProgressBar) : this(progressBar, null)
    constructor(progressBarItem: MenuItem) : this(null, progressBarItem)

    override fun onStart(imageUri: Any, targetImageView: ImageView) {
        progressBar?.isVisible = true
        progressBarItem?.isVisible = true
    }

    override fun onFinish(imageUri: Any, targetImageView: ImageView) {
        progressBar?.isVisible = false
        progressBarItem?.isVisible = false
    }

    override fun onFailure(ex: Exception?, imageUri: Any, targetImageView: ImageView) {
        logDebug("Failed to load image: uri=$imageUri, ex=$ex, imageView=$targetImageView")
    }
}