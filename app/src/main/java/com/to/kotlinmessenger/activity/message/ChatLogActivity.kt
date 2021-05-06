package com.to.kotlinmessenger.activity.message

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.to.kotlinmessenger.R
import com.to.kotlinmessenger.model.ChatMessage
import com.to.kotlinmessenger.model.User
import com.to.kotlinmessenger.util.*
import com.to.kotlinmessenger.view.ChatFromItem
import com.to.kotlinmessenger.view.ChatToItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*

/**
 * チャットログ画面。
 */
class ChatLogActivity : AppCompatActivity() {

    companion object {
        /** チャット相手のユーザーをIntentから取得する際に使用するキー */
        private const val USER_KEY = "USER_KEY"

        /**
         * `ChatLogActivity`を起動するためのIntentを作成する。
         *
         * @param context コンテキスト
         * @param toUser チャット相手のユーザー
         * @return `ChatLogActivity`を起動するためのIntent
         */
        fun createIntent(context: Context, toUser: User): Intent {
            val intent = Intent(context, ChatLogActivity::class.java)
            intent.putExtra(USER_KEY, toUser)
            return intent
        }
    }

    /** チャットログのRecyclerViewに対応するAdapter */
    private val chatLogAdapter = GroupAdapter<GroupieViewHolder>()

    /**
     * チャットログの相手ユーザー
     *
     * Intent経由で初期化される。
     */
    private lateinit var toUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerview_chat_log.adapter = chatLogAdapter

        val toUser = intent.getParcelableExtra<User>(USER_KEY)
            ?: throw IllegalStateException("User must be passed on Intent")
        this.toUser = toUser

        // アクションバーに相手ユーザー名を表示
        supportActionBar?.title = getDisplayUserName(this, toUser)

        // メッセージを取得
        listenForMessages()

        edittext_chat_log.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // メッセージが入力されていれば送信ボタン表示
                send_button_chat_log.isVisible = !s.isNullOrBlank()
            }
        })

        send_button_chat_log.setOnClickListener {
            // 送信処理
            performSendMessage()
        }

        scroll_to_bottom_button.setOnClickListener {
            // 最下部へスクロール
            smoothScrollToBottom()
        }

        got_a_new_message_textview.setOnClickListener {
            // 最下部へスクロール
            smoothScrollToBottom()
        }

        recyclerview_chat_log.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (recyclerView.canScrollVertically(1)) {
                    // 最下部へスクロールされていないときのみスクロールボタンを表示する
                    scroll_to_bottom_button.isVisible = true
                } else {
                    scroll_to_bottom_button.isVisible = false
                    got_a_new_message_textview.isVisible = false
                }
            }
        })

        recyclerview_chat_log.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            // キーボード表示時に最下部までスクロールする
            scrollToBottomOnLayoutChange(bottom, oldBottom)
        }
    }

    /**
     * メッセージを取得する。
     */
    private fun listenForMessages() {
        val fromId = requireNotNull(FirebaseAuth.getInstance().uid) { "fromUser must be logged in" }
        val toId = toUser.uid
        val ref = FirebaseDatabaseAccessor.getMessagesRef(fromId, toId)

        ref.addChildEventListener(object : ChildEventListener {
            /** 表示済み日付文字列をメモするリスト */
            private val displayedDateList = mutableSetOf<String>()

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                if (chatMessage == null) {
                    scrollToBottom()
                    return
                }

                logDebug("chatMessage.text=${chatMessage.text}")

                // ChatItemを作成してadapterに追加
                addChatItem(chatMessage)

                if (scroll_to_bottom_button.isVisible) {
                    // スクロールボタンが表示されている場合(チャットログ画面の上部へスクロールしている場合)は
                    // 新規メッセージ受信のメッセージを表示
                    got_a_new_message_textview.isVisible = true
                } else {
                    // それ以外、最下部へスクロール済み、またはチャットログ画面を開いた直後の場合は
                    // 新規メッセージを含めた最下部へ自動スクロール
                    scrollToBottom()
                }
            }

            /**
             * `ChatMessage`の情報を画面にバインドするための`ChatItem`を作成し、Adapterに追加する。
             *
             * @param chatMessage チャットメッセージ
             */
            private fun addChatItem(chatMessage: ChatMessage) {
                // 表示する日付、時刻を取得
                val (date, time) = getFormattedDateTime(this@ChatLogActivity, chatMessage.timestamp)
                // 日付は、表示済みでない場合のみ表示する
                val displayDate =
                    if (!displayedDateList.contains(date)) {
                        displayedDateList.add(date)
                        date
                    } else null

                if (chatMessage.fromId == FirebaseAuthAccessor.getUid()) {
                    // 自分からのメッセージをバインド
                    val currentUser = requireNotNull(LatestMessagesActivity.currentUser) {
                        "currentUser must be initialized in LatestMessagesActivity"
                    }
                    chatLogAdapter.add(
                        ChatFromItem(
                            chatMessage,
                            currentUser,
                            displayDate,
                            time
                        )
                    )

                } else {
                    // 相手からのメッセージをバインド
                    chatLogAdapter.add(
                        ChatToItem(
                            chatMessage,
                            toUser,
                            displayDate,
                            time
                        )
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {}

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}
        })
    }

    /**
     * メッセージを送信する。
     */
    private fun performSendMessage() {
        val text = edittext_chat_log.text.toString()
        val fromId = FirebaseAuthAccessor.getUid() ?: return
        val toId = toUser.uid

        // 自分側の領域にメッセージを保存
        val fromRef = FirebaseDatabaseAccessor.getMessagesRef(fromId, toId).push()
        val chatMessage = ChatMessage(
            fromRef.key!!, text, fromId, toId, nowTimestampSec()
        )
        fromRef.setValue(chatMessage)
            .addOnSuccessListener {
                logDebug("Save out chat message: ${fromRef.key} on $fromId/$toId}")
                edittext_chat_log.text.clear()
                send_button_chat_log.isVisible = false
                scrollToBottom()
            }
        // 最新メッセージ領域にも保存
        val latestMessageFromRef = FirebaseDatabaseAccessor.getLatestMessagesRef(fromId, toId)
        latestMessageFromRef.setValue(chatMessage)

        if (fromId == toId) {
            // 自分自身へのメッセージなら相手側の領域には保存しない
            return
        }

        // 相手側の領域に保存
        val toRef = FirebaseDatabaseAccessor.getMessagesRef(toId, fromId).push()
        toRef.setValue(chatMessage)
            .addOnSuccessListener {
                logDebug("Save out chat message: ${fromRef.key} on $toId/$fromId}")
            }
        val latestMessageToRef = FirebaseDatabaseAccessor.getLatestMessagesRef(toId, fromId)
        latestMessageToRef.setValue(chatMessage)
    }

    /**
     * 画面の一番下までアニメーション無しでスクロールする。
     */
    private fun scrollToBottom() {
        recyclerview_chat_log.scrollToPosition(chatLogAdapter.itemCount - 1)
    }

    /**
     * 画面の一番下までアニメーション有りでスクロールする。
     */
    private fun smoothScrollToBottom() {
        recyclerview_chat_log.smoothScrollToPosition(chatLogAdapter.itemCount - 1)
    }

    /**
     * レイアウト変更時に画面の一番下までアニメーション有りでスクロールする。
     *
     * @param bottom レイアウト変更後の最下部を示す値(Y座標)
     * @param oldBottom レイアウト変更前の最下部を示す値(Y座標)
     */
    private fun scrollToBottomOnLayoutChange(bottom: Int, oldBottom: Int) {
        if (bottom < oldBottom) {
            // レイアウト変更により最下部が上がっている場合は一番下までスクロールする
            recyclerview_chat_log.smoothScrollToPosition(bottom)
        }
    }
}