package com.to.kotlinmessenger.view

import androidx.core.view.isVisible
import com.to.kotlinmessenger.R
import com.to.kotlinmessenger.model.ChatMessage
import com.to.kotlinmessenger.model.User
import com.to.kotlinmessenger.util.loadProfileImageIntoView
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

/**
 * Chat Log 画面にて自分からのメッセージをバインドするItem。
 *
 * @property chatMessage チャットメッセージ
 * @property user 自ユーザー
 * @property date メッセージの送信日付(表示しない場合はnull)
 * @property time メッセージの送信時刻
 */
class ChatFromItem(
    private val chatMessage: ChatMessage,
    private val user: User,
    private val date: String?,
    private val time: String
) :
    Item<GroupieViewHolder>() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        // プロフィール画像
        loadProfileImageIntoView(user, viewHolder.itemView.image_view_chat_from_row)
        // 日付
        if (date != null) {
            viewHolder.itemView.date_textview_from_row.text = date
            viewHolder.itemView.date_textview_from_row.isVisible = true
        } else {
            viewHolder.itemView.date_textview_from_row.isVisible = false
        }
        // テキスト
        viewHolder.itemView.message_textview_from_row.text = chatMessage.text
        // 時刻
        viewHolder.itemView.time_textview_from_row.text = time
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

/**
 * Chat Log 画面にて相手からのメッセージをバインドするItem。
 *
 * @property chatMessage チャットメッセージ
 * @property user チャットの相手ユーザー
 * @property date メッセージの送信日付(表示しない場合はnull)
 * @property time メッセージの送信時刻
 */
class ChatToItem(
    private val chatMessage: ChatMessage,
    private val user: User,
    private val date: String?,
    private val time: String
) :
    Item<GroupieViewHolder>() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        // プロフィール画像
        loadProfileImageIntoView(user, viewHolder.itemView.image_view_chat_to_row)
        // 日付
        if (date != null) {
            viewHolder.itemView.date_textview_to_row.text = date
            viewHolder.itemView.date_textview_to_row.isVisible = true
        } else {
            viewHolder.itemView.date_textview_to_row.isVisible = false
        }
        // テキスト
        viewHolder.itemView.message_textview_to_row.text = chatMessage.text
        // 時刻
        viewHolder.itemView.time_textview_to_row.text = time
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}