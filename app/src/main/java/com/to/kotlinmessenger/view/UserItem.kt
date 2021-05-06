package com.to.kotlinmessenger.view

import android.content.Context
import com.to.kotlinmessenger.R
import com.to.kotlinmessenger.model.User
import com.to.kotlinmessenger.util.ProgressBarImageLoadListener
import com.to.kotlinmessenger.util.getDisplayUserName
import com.to.kotlinmessenger.util.loadProfileImageIntoView
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.user_row_new_message.view.*

/**
 * New Message 画面にてユーザーをバインドするItem。
 *
 * @property user ユーザー
 */
class UserItem(private val context: Context, val user: User) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        // プロフィール画像
        val targetImageView = viewHolder.itemView.image_view_new_message
        val progressBar = viewHolder.itemView.circular_progressbar_new_message
        loadProfileImageIntoView(user, targetImageView, ProgressBarImageLoadListener(progressBar))
        // ユーザー名
        viewHolder.itemView.username_textview_new_message.text = getDisplayUserName(context, user)
    }

    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }
}