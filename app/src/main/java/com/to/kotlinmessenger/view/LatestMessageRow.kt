package com.to.kotlinmessenger.view

import android.content.Context
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.to.kotlinmessenger.R
import com.to.kotlinmessenger.model.ChatMessage
import com.to.kotlinmessenger.model.User
import com.to.kotlinmessenger.util.*
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.latest_message_row.view.*

/**
 * Latest Message 画面にて最新メッセージをバインドするItem。
 *
 * @property context コンテキスト
 * @property chatMessage チャットメッセージ
 * @property date メッセージの送信日付
 * @property time メッセージの送信時刻
 */
class LatestMessageRow(
    private val context: Context,
    private val chatMessage: ChatMessage, private val date: String,
    private val time: String
) :
    Item<GroupieViewHolder>() {
    /** チャット相手のユーザー */
    var chatPartnerUser: User? = null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val chatPartnerId =
            if (chatMessage.fromId == FirebaseAuthAccessor.getUid()) chatMessage.toId
            else chatMessage.fromId

        // チャット相手のユーザー情報を取得して画面に表示
        val ref = FirebaseDatabaseAccessor.getUsersRef(chatPartnerId)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java) ?: return

                // プロフィール画像
                val targetImageView = viewHolder.itemView.imageview_latest_message
                val progressBar = viewHolder.itemView.circular_progressbar_latest_message_menu
                loadProfileImageIntoView(
                    user,
                    targetImageView,
                    ProgressBarImageLoadListener(progressBar)
                )
                // ユーザー名
                viewHolder.itemView.username_textview_latest_message.text =
                    getDisplayUserName(context, user)
                // メッセージ
                viewHolder.itemView.message_textview_latest_message.text = chatMessage.text
                // 日付
                viewHolder.itemView.date_textview_latest_message.text = date
                // 時刻
                viewHolder.itemView.time_textview_latest_message.text = time

                chatPartnerUser = user
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }
}