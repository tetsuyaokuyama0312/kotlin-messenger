package com.to.kotlinmessenger.util

import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.to.kotlinmessenger.model.User
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

/**
 * 画像をロードしてImageViewに表示する。
 *
 * @param imageUri 画像ファイルのURI
 * @param targetImageView 表示対象のImageView
 */
fun loadImageIntoView(
    imageUri: String,
    targetImageView: ImageView,
    listener: ImageLoadListener = ImageLoadListener.NOP
) {
    listener.onStart(imageUri, targetImageView)

    Glide.with(targetImageView.context.applicationContext)
        .load(imageUri)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                listener.onFailure(e, imageUri, targetImageView)
                listener.onFinish(imageUri, targetImageView)
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                listener.onSuccess(imageUri, targetImageView)
                listener.onFinish(imageUri, targetImageView)
                return false
            }
        })
        .into(targetImageView)
}

/**
 * 画像をロードしてImageViewに表示する。
 *
 * @param imageUri 画像ファイルのURI
 * @param targetImageView 表示対象のImageView
 */
fun loadImageIntoView(
    imageUri: Uri, targetImageView: ImageView, listener: ImageLoadListener = ImageLoadListener.NOP
) {
    listener.onStart(imageUri, targetImageView)

    Glide.with(targetImageView.context.applicationContext)
        .load(imageUri)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                listener.onFailure(e, imageUri, targetImageView)
                listener.onFinish(imageUri, targetImageView)
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                listener.onSuccess(imageUri, targetImageView)
                listener.onFinish(imageUri, targetImageView)
                return false
            }
        })
        .into(targetImageView)
}

/**
 * ユーザーのプロフィール画像をロードしてImageViewに表示する。
 *
 * @param user ユーザー
 * @param targetImageView 表示対象のImageView
 */
fun loadProfileImageIntoView(
    user: User, targetImageView: ImageView, listener: ImageLoadListener = ImageLoadListener.NOP
) {
    loadImageIntoView(user.profileImageUrl, targetImageView, listener)
}