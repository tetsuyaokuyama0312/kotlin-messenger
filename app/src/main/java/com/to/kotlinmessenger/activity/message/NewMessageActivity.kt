package com.to.kotlinmessenger.activity.message

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import com.to.kotlinmessenger.R
import com.to.kotlinmessenger.model.User
import com.to.kotlinmessenger.util.FirebaseAuthAccessor
import com.to.kotlinmessenger.util.FirebaseDatabaseAccessor
import com.to.kotlinmessenger.view.UserItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*

/**
 * 新規メッセージ画面。
 */
class NewMessageActivity : AppCompatActivity() {

    companion object {
        /**
         * `NewMessageActivity`を起動するためのIntentを作成する。
         *
         * @param context コンテキスト
         * @return `NewMessageActivity`を起動するためのIntent
         */
        fun createIntent(context: Context) = Intent(context, NewMessageActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        recycleview_newmessage.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        swiperefresh_newmessage.setColorSchemeColors(
            ContextCompat.getColor(
                this,
                R.color.colorAccent
            )
        )

        supportActionBar?.title = getString(R.string.new_message_action_bar_title)

        // ユーザー一覧を取得
        fetchUsers()

        swiperefresh_newmessage.setOnRefreshListener {
            fetchUsers()
        }
    }

    /**
     * ユーザー一覧を取得する。
     */
    private fun fetchUsers() {
        swiperefresh_newmessage.isRefreshing = true

        val ref = FirebaseDatabaseAccessor.getUsersRef()
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()

                snapshot.children
                    .mapNotNull { it.getValue(User::class.java) }
                    .sortedBy { it.username }
                    .forEach {
                        val userItem = UserItem(this@NewMessageActivity, it)
                        if (it.uid == FirebaseAuthAccessor.getUid()) {
                            // 自分は先頭に表示する
                            adapter.add(0, userItem)
                        } else {
                            adapter.add(userItem)
                        }
                    }

                adapter.setOnItemClickListener { item, view ->
                    val userItem = item as UserItem
                    startActivity(ChatLogActivity.createIntent(view.context, userItem.user))
                }

                recycleview_newmessage.adapter = adapter
                swiperefresh_newmessage.isRefreshing = false
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
